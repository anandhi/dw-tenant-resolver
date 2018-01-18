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


    public static void setEntityManagerForTenant(String tenantConfiguration) throws InvalidTenantException {
        if (tenantConfiguration != null) {
            tenantConfiguration = tenantConfiguration.toLowerCase();
        }
        if(!TenantResolverConfig.getValidTenantConfigurations().contains(tenantConfiguration)){
            throw new InvalidTenantException(tenantConfiguration);
        }

        if(tenantEntityManagerMap.get() == null){
            tenantEntityManagerMap.set(new HashMap<String, EntityManager>());
        }

        if(tenantEntityManagerMap.get().get(tenantConfiguration) != null) {
            if(tenantEntityManagerMap.get().get(tenantConfiguration).isOpen()){
                tenantEntityManagerMap.get().get(tenantConfiguration).close();
            }
        }
            EntityManager em = TenantDataSourceFactory.createEntityManager(tenantConfiguration);
            tenantEntityManagerMap.get().put(tenantConfiguration, em);
            tenantEntityManager.set(em);
    }

    public static EntityManager getEntityManager(){
        return tenantEntityManager.get();
    }

    public static EntityManager getEntityManager(String tenantName) throws InvalidTenantException {
        if (tenantName != null) {
            tenantName = tenantName.toLowerCase();
        }
        setEntityManagerForTenant(tenantName);
        return tenantEntityManager.get();
    }

}
