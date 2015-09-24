package resolver;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.List;

/**
 * Created by anandhi on 03/06/15.
 */
public abstract class MultiTenantResolverBundle<T> implements ConfiguredBundle<T> {

    @Override
    public void initialize(Bootstrap<?> bootstrap) {

    }

    @Override
    public void run(T configuration, Environment environment) {
        MultiTenantDataSourceConfiguration multiTenantDsConfiguration =
                getMultiTenantDataSourceConfiguration(configuration);
        environment.lifecycle().manage(new MultiTenantDataSourceManaged(multiTenantDsConfiguration,
                                                            getPackagesToScan()));

        if(multiTenantDsConfiguration.getEnforceTenantHeaderInAllRequests()){
            environment.servlets().addFilter( "/*", new TenantResolverFilter());
        }
    }

    protected abstract MultiTenantDataSourceConfiguration getMultiTenantDataSourceConfiguration(T configuration);

    protected abstract List<String> getPackagesToScan();
}
