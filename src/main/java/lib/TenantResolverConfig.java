package lib;

import lombok.Getter;
import resolver.MultiTenantDataSourceConfiguration;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by anandhi on 03/06/15.
 */
public class TenantResolverConfig {
    @Getter
    private static Boolean enforceTenantHeaderInAllRequests;
    @Getter
    private static String tenantHeaderName;
    @Getter
    private static Map<String, List<List<List<String>>>> buAuxiliaryConfigurationPivots;
    @Getter
    private static List<String> whitelistedBUsForAuxConf;
    @Getter
    private static List<String> validTenantConfigurations = new LinkedList<String>();

    public static void initialize(MultiTenantDataSourceConfiguration configuration){
        enforceTenantHeaderInAllRequests = configuration.getEnforceTenantHeaderInAllRequests();
        tenantHeaderName = configuration.getTenantHeaderName();
        buAuxiliaryConfigurationPivots = configuration.getBuAuxiliaryConfigurationPivots();
        whitelistedBUsForAuxConf = configuration.getWhitelistedBUsForAuxConf();
        validTenantConfigurations.addAll(configuration.getDatabaseConfigurations().keySet());
    }

}
