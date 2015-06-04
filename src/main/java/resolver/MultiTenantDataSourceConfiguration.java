package resolver;

import com.yammer.dropwizard.db.DatabaseConfiguration;
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

    private Map<String, DatabaseConfiguration> databaseConfigurations ;
}
