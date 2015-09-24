package resolver;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.lifecycle.Managed;
import lib.TenantResolverConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;


/**
 * Created by anandhi on 01/06/15.
 */
public class MultiTenantDataSourceManaged implements Managed {

    private MultiTenantDataSourceConfiguration multiTenantDataSourceConfiguration;
    private List<String> packagesToScan;

    public MultiTenantDataSourceManaged(MultiTenantDataSourceConfiguration multiTenantDsConfiguration, List<String> packagesToScan) {
        this.multiTenantDataSourceConfiguration = multiTenantDsConfiguration;
        this.packagesToScan = packagesToScan;
    }

    @Override
    public void start() throws Exception {
        TenantResolverConfig.initialize(multiTenantDataSourceConfiguration);
        TenantDataSourceFactory.initialize(multiTenantDataSourceConfiguration, packagesToScan);
    }

    @Override
    public void stop() throws Exception {
        TenantDataSourceFactory.tearDown();
    }
}
