package resolver;

import com.yammer.dropwizard.db.DatabaseConfiguration;
import lib.InvalidTenantException;
import lib.MultiTenantJpaProperties;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by anandhi on 02/06/15.
 */
public class TenantDataSourceFactory {
    final static Logger logger = LoggerFactory.getLogger(TenantDataSourceFactory.class);

    private static Map<String, EntityManagerFactory> emfCollection = new HashMap<String, EntityManagerFactory>();

    public static void initialize(MultiTenantDataSourceConfiguration multiTenantDataSourceConfiguration,
                                   List<String> packagesToScan){
        for(String tenant : multiTenantDataSourceConfiguration.getDatabaseConfigurations().keySet()){
            logger.info("Initializing the Entity Manager Factory for Tenant: " + tenant);
            DatabaseConfiguration configuration = multiTenantDataSourceConfiguration.getDatabaseConfigurations().get(tenant);
            MultiTenantJpaProperties properties = new MultiTenantJpaProperties(configuration);
            properties.addProperty("dynamicPersistenceProvider.packagesToScan", packagesToScan);
            EntityManagerFactory emf = Persistence.createEntityManagerFactory(tenant, properties.get());
            addTenantDataSource(tenant, emf);
        }
    }

    public static void addTenantDataSource(String tenant, EntityManagerFactory emf){
        emfCollection.put(tenant, emf);
    }

    public static void tearDown(){
        for(String tenant : emfCollection.keySet()){
            emfCollection.get(tenant).close();
        }
    }

    public static EntityManager createEntityManager(String tenant) throws InvalidTenantException {
        if(StringUtils.isNotBlank(tenant) && emfCollection.containsKey(tenant.toLowerCase())){
            return emfCollection.get(tenant).createEntityManager();
        }else{
            throw new InvalidTenantException(tenant);
        }
    }

}
