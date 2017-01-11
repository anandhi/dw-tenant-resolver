package jpa;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import lib.SearchQueryBuilder;
import resolver.TenantResolver;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by anupama.agarwal on 06/01/17.
 */
public class JpaRepository<T, ID extends Serializable>
        implements IJpaRepository<T, ID> {
    private final Class<T> entityClass;


    @Inject
    public JpaRepository() {
        this.entityClass = getEntityClass();
    }

    Class<T> getEntityClass() {
        ParameterizedType genericSuperclass = (ParameterizedType) getGenericSuperClass();
        return (Class<T>) genericSuperclass
                .getActualTypeArguments()[0];
    }

    private Type getGenericSuperClass() {
        // Handle case where the class gets extended (due to proxying)
        Class klass = getClass();
        // Get the immediate subclass of SimpleJpaGenericRepository
        while (klass != null
                && klass.getSuperclass() != null
                && !klass.getSuperclass().isAssignableFrom(JpaRepository.class)) {
            klass = klass.getSuperclass();
        }
        Preconditions.checkNotNull(klass);
        return klass.getGenericSuperclass();
    }


    public void persist(T entity) {
        getEntityManager().persist(entity);
    }

    public void persist(Iterable<? extends T> entities) {
        for (T entity : entities) {
            persist(entity);
        }
    }

    public <S extends T> S merge(S entity) {
        return getEntityManager().merge(entity);
    }

    public <S extends T> Iterable<S> merge(Iterable<S> entities) {
        List<S> result = Lists.newArrayList();
        for (S entity : entities) {
            result.add(merge(entity));
        }
        return result;
    }

    public List<T> findAll() {
        return findAll(entityClass.getSimpleName());
    }

    public List<T> findAll(String entityName) {
        return getEntityManager()
                .createQuery("select x from " + entityName + " x")
                .getResultList();

    }

    public Page<T> findAll(PageRequest pageRequest) {
        return findAll(pageRequest, entityClass.getSimpleName());
    }

    public Page<T> findAll(PageRequest pageRequest, String entityName){
        Long count = (Long) getEntityManager()
                .createQuery("select count(x) from " + entityName + " x")
                .getSingleResult();
        List<T> content = getEntityManager()
                .createQuery("select x from " + entityName + " x")
                .setFirstResult(pageRequest.getOffset())
                .setMaxResults(pageRequest.getPageSize())
                .getResultList();

        boolean hasMore = pageRequest.getPageSize() == content.size();
        return new Page<T>(count, content, hasMore);

    }
    public Page<T> findAllByNamedQuery(String queryName, Map<String, Object> parameters, PageRequest pageRequest) {
        Query countQuery = getEntityManager().createNamedQuery(queryName + ".count");

        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }
        Long count = (Long) countQuery
                .getSingleResult();

        Query query = getEntityManager().createNamedQuery(queryName);

        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        List<T> content = query
                .setFirstResult(pageRequest.getOffset())
                .setMaxResults(pageRequest.getPageSize())
                .getResultList();

        boolean hasMore = pageRequest.getPageSize() == content.size();
        return new Page<T>(count, content, hasMore);
    }
    public Optional<T> findOneByNamedQuery(String queryName, Map<String, Object> parameters) {
        Query query = getEntityManager().createNamedQuery(queryName);

        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }

        try {
            return Optional.of((T) query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.absent();
        }

    }

    public Optional<T> findOne(ID id) {
        return Optional.fromNullable(getEntityManager().find(entityClass, id));
    }


    public void delete(T t) {
        getEntityManager().remove(t);
    }

    public void delete(Iterable<? extends T> entities) {
        for (T entity : entities) {
            delete(entity);
        }

    }



    public Page<T> findByQuery(HashMap<String, Object> parameters, PageRequest pageRequest) {
        return findByQuery(parameters, pageRequest, entityClass.getSimpleName());
    }
    public Page<T> findByQuery(HashMap<String, Object> parameters, PageRequest pageRequest, String entityName) {
        return findByQuery(parameters, pageRequest, "id", "asc", entityName);
    }

    public Page<T> findByQuery(HashMap<String, Object> parameters, PageRequest pageRequest, String orderBy, String order) {
        return findByQuery(parameters, pageRequest, orderBy, order, entityClass.getSimpleName());
    }

    public Page<T> findByQuery(HashMap<String, Object> parameters, PageRequest pageRequest, String orderBy, String order, String entityName) {
        SearchQueryBuilder searchQueryBuilder = new SearchQueryBuilder();
        String whereClause = " where " + searchQueryBuilder.construct(parameters);
        Long count = (Long) getEntityManager()
                .createQuery("select count(x) from " + entityName + " x " + whereClause)
                .getSingleResult();
        whereClause += " order by x." + orderBy + " " + order;
        List<T> content = getEntityManager()
                .createQuery("select x from " + entityName + " x " + whereClause)
                .setFirstResult(pageRequest.getOffset())
                .setMaxResults(pageRequest.getPageSize())
                .getResultList();

        boolean hasMore = pageRequest.getPageSize() == content.size();
        return new Page<T>(count, content, hasMore);
    }

    public Optional<T> findOneByQuery(HashMap<String,Object> parameters){
        return findOneByQuery(parameters, entityClass.getSimpleName());
    }

    public Optional<T> findOneByQuery(HashMap<String,Object> parameters, String entityName){
        PageRequest pageRequest = PageRequest.builder().pageSize(1).pageNumber(0).build();
        Page<T> page = findByQuery(parameters, pageRequest, entityName);
        List<T> content = page.getContent();
        if(content.size() == 0){
            return Optional.absent();
        }
        return Optional.fromNullable(content.get(0));
    }

    public void flushAndClear() {
        getEntityManager().flush();
        getEntityManager().clear();
    }

    protected EntityManager getEntityManager() {
        return TenantResolver.getEntityManager();
    }
}
