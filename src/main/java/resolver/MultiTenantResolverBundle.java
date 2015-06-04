package resolver;

import com.yammer.dropwizard.ConfiguredBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

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
        environment.manage(new MultiTenantDataSourceManaged(multiTenantDsConfiguration,
                                                            getPackagesToScan()));

        if(multiTenantDsConfiguration.getEnforceTenantHeaderInAllRequests()){
            environment.addFilter(new TenantResolverFilter(), "/*");
        }
    }

    protected abstract MultiTenantDataSourceConfiguration getMultiTenantDataSourceConfiguration(T configuration);

    protected abstract List<String> getPackagesToScan();
}
