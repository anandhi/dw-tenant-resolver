package lib;

import com.yammer.dropwizard.db.DatabaseConfiguration;
import org.hibernate.cfg.ImprovedNamingStrategy;

import java.util.Properties;

/**
 * Created by anandhi on 03/06/15.
 */
public class MultiTenantJpaProperties {
    private Properties properties = new Properties();
    private DatabaseConfiguration dbConfiguration;

    public enum JpaConfigConstants {
        MIN_POOL_SIZE(10),
        MAX_POOL_SIZE(100),
        TIME_OUT(1000),
        IDLE_TEST_PERIOD(2000);

        private int value;

        private JpaConfigConstants(int value){
            this.value = value;
        }

        public int getDefault(){
            return this.value;
        }

        public int get(int overriddenValue){
            if(overriddenValue <= 0){
                return getDefault();
            }
            return overriddenValue;
        }

    }

    public MultiTenantJpaProperties(DatabaseConfiguration configuration){
        dbConfiguration = configuration;
        properties.put("javax.persistence.jdbc.url", configuration.getUrl());
        properties.put("javax.persistence.jdbc.user", configuration.getUser());
        properties.put("javax.persistence.jdbc.password", configuration.getPassword());
        properties.put("javax.persistence.jdbc.driver", configuration.getDriverClass());

        properties.putAll(configuration.getProperties());
        properties.put("hibernate.connection.provider_class", "org.hibernate.service.jdbc.connections.internal.C3P0ConnectionProvider");
        properties.put("hibernate.c3p0.min_size", JpaConfigConstants.MIN_POOL_SIZE.get(configuration.getMinSize()));
        properties.put("hibernate.c3p0.max_size", JpaConfigConstants.MAX_POOL_SIZE.get(configuration.getMaxSize()));
        properties.put("hibernate.c3p0.timeout", configuration.getMaxWaitForConnection().toSeconds());
        properties.put("hibernate.c3p0.idle_test_period", configuration.getCloseConnectionIfIdleFor().toSeconds());
        properties.put("hibernate.ejb.naming_strategy", ImprovedNamingStrategy.INSTANCE);
    }

    public void addProperty(String propName, Object propValue){
        properties.put(propName, propValue);
    }

    public Properties get(){

        return properties;
    }
}
