package resolver;

import io.dropwizard.db.DataSourceFactory;
import lib.InvalidTenantException;
import lib.MultiTenantJpaProperties;
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
        for(String tenantConfiguration : multiTenantDataSourceConfiguration.getDatabaseConfigurations().keySet()){
            logger.info("Initializing the Entity Manager Factory for Tenant Configuration : " + tenantConfiguration);
            DataSourceFactory configuration = multiTenantDataSourceConfiguration.getDatabaseConfigurations().get(tenantConfiguration);
            MultiTenantJpaProperties properties = new MultiTenantJpaProperties(configuration);
            properties.addProperty("dynamicPersistenceProvider.packagesToScan", packagesToScan);
            EntityManagerFactory emf = Persistence.createEntityManagerFactory(tenantConfiguration, properties.get());
            addTenantDataSource(tenantConfiguration, emf);
        }
    }

    public static void addTenantDataSource(String tenantConfiguration, EntityManagerFactory emf) {
        emfCollection.put(tenantConfiguration, emf);
    }

    public static void tearDown(){
        for(String tenantConfiguration : emfCollection.keySet()) {
            emfCollection.get(tenantConfiguration).close();
        }
    }

    public static EntityManager createEntityManager(String tenantConfiguration) throws InvalidTenantException {
        if(tenantConfiguration != null && emfCollection.containsKey(tenantConfiguration)) {
            return emfCollection.get(tenantConfiguration).createEntityManager();
        } else {
            throw new InvalidTenantException(tenantConfiguration);
        }
    }

}
