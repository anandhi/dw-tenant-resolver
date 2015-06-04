package lib;

import lombok.Getter;
import resolver.MultiTenantDataSourceConfiguration;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by anandhi on 03/06/15.
 */
public class TenantResolverConfig {
    @Getter
    private static String tenantHeaderName;
    @Getter
    private static List<String> validTenants = new LinkedList<String>();

    public static void initialize(MultiTenantDataSourceConfiguration configuration){
        tenantHeaderName = configuration.getTenantHeaderName();
        validTenants.addAll(configuration.getDatabaseConfigurations().keySet());
    }

}
