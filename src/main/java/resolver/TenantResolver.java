package resolver;

import lib.InvalidTenantException;
import lib.TenantResolverConfig;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by anandhi on 02/06/15.
 */
public class TenantResolver {

    private static ThreadLocal<EntityManager> tenantEntityManager = new ThreadLocal<EntityManager>();
    private static ThreadLocal<Map<String, EntityManager>> tenantEntityManagerMap = new ThreadLocal<Map<String, EntityManager>>();


    public static void setEntityManagerForTenant(String tenant) throws InvalidTenantException {
        if(!TenantResolverConfig.getValidTenants().contains(tenant)){
            throw new InvalidTenantException(tenant);
        }

        if(tenantEntityManagerMap.get() == null){
            tenantEntityManagerMap.set(new HashMap<String, EntityManager>());
        }

        if(tenantEntityManagerMap.get().get(tenant) != null) {
            tenantEntityManagerMap.get().get(tenant).close();
        }
            EntityManager em = TenantDataSourceFactory.createEntityManager(tenant);
            tenantEntityManagerMap.get().put(tenant, em);
            tenantEntityManager.set(em);

    }

    public static EntityManager getEntityManager(){
        return tenantEntityManager.get();
    }

    public static EntityManager getEntityManager(String tenantName) throws InvalidTenantException {
        setEntityManagerForTenant(tenantName);
        return tenantEntityManager.get();
    }

}
