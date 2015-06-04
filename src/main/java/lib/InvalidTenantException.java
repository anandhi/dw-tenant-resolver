package lib;

/**
 * Created by anandhi on 02/06/15.
 */
public class InvalidTenantException extends Exception {

    public InvalidTenantException(String tenant){
        super("Tenant " + tenant + " is Invalid");
    }

}
