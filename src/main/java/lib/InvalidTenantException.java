package lib;

/**
 * Created by anandhi on 02/06/15.
 */
public class InvalidTenantException extends Exception {

    public InvalidTenantException(String tenantConfiguration){
        super("Tenant Configuration : " + tenantConfiguration + " is Invalid or Undefined");
    }

    public InvalidTenantException(String tenantConfiguration, String errorMessage){
        super("Tenant Configuration Resolution failed : " + errorMessage);
    }

}
