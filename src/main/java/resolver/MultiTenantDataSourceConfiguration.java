package resolver;

import io.dropwizard.db.DataSourceFactory;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by anandhi on 01/06/15.
 */

@Data
public class MultiTenantDataSourceConfiguration {
    /**
     * defines the headerName by which tenant will be resolved
     */
    private String tenantHeaderName;

    /**
     * defines the additional configuration keys,
     * which paired with tenantHeaderName, will define the tenantConfiguration
     */
    private List<List<List<String>>> auxiliaryConfigurationPivots;

    /**
     * defines the whitelisted business units for which the auxiliary configuration will hold true
     */
    private List<String> whitelistedBUsForAuxConf;

    /**
     * If true, then universal filter will added which expects
     * header with tenantHeaderName should be present
     */
    private Boolean enforceTenantHeaderInAllRequests = true;

    /**
     * defines the database configurations,
     * with each key corresponding to combination of expected values of tenantHeaderName & auxiliaryConfigurationPivots
     */
    private Map<String, DataSourceFactory> databaseConfigurations ;
}
