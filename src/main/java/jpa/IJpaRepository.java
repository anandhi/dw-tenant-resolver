package jpa;

import com.google.common.base.Optional;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by anupama.agarwal on 06/01/17.
 */
public interface IJpaRepository<T, ID extends Serializable> {

        public void persist(T entity);

        public void persist(Iterable<? extends T> entities);

        public <S extends T> S merge(S entity);

        public <S extends T> Iterable<S> merge(Iterable<S> entities);

        public List<T> findAll();

        public List<T> findAll(String entityName);

        public Page<T> findAll(PageRequest pageRequest);

        public Page<T> findAllByNamedQuery(String queryName,
                                           Map<String, Object> parameters,
                                           PageRequest pageRequest);

        public Optional<T> findOneByNamedQuery(String queryName,
                                               Map<String, Object> parameters);

        public Optional<T> findOne(ID id);

        public Page<T> findByQuery(HashMap<String, Object> parameters, PageRequest pageRequest);

        public Page<T> findByQuery(HashMap<String, Object> parameters, PageRequest pageRequest, String entityName);

        public Page<T> findByQuery(HashMap<String, Object> parameters, PageRequest pageRequest, String orderBy, String order);

        public Page<T> findByQuery(HashMap<String, Object> parameters, PageRequest pageRequest, String orderBy, String order, String entityName);

        public Optional<T> findOneByQuery(HashMap<String,Object> parameters);

        public Optional<T> findOneByQuery(HashMap<String,Object> parameters, String entityName);

        public void delete(T t);

        public void delete(Iterable<? extends T> entities);

        @Deprecated
        /**
         * Use entity manager instead.
         *
         * Will be removed in the 2.0 release
         */
        public void flushAndClear();


}
