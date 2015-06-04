# dw-tenant-resolver
Dropwizard bundle which helps to achieve multi-tenancy in dropwizard application

<b>Configuration:</b>

  Add the database configuration for each tenant, database configuration accepts all the properties which dropwizard
  <code>DatabaseConfiguration</code> object accepts - .
  
  Shown in below  example.
  
```yaml
multiTenantDataSourceConfiguration:
  tenantHeaderName: X_TENANT_ID
  enforceTenantHeaderInAllRequests: true
  databaseConfigurations:
    tenant_1:
      driverClass: com.mysql.jdbc.Driver
      user: root
      password:
      url: jdbc:mysql://db_server_1:port/database_tenant_1
    tenant_2:
      driverClass: com.mysql.jdbc.Driver
      user: root
      password:
      url: jdbc:mysql://db_server_2:port/databse_tenant_2
```  
Above configuration accepts, <code>tenantHeaderName</code> - which is been used the resolve the tenant for each request and initializes the necessary things to connect to defined datasource.

and <code> enforceTenantHeaderInAllRequests </code> defines - whether headerName is optional or mandatory in all requests. If value is true and <code>tenantHeaderName</code> is missing, then request will be rejected, and  400 response will be sent with the message <code>Invalid Tenant Nil</code>

<b>Adding the tenant-resolver bundle to the Application:</b>

During the initialization of dropwizard application, add tenant-resolver bundle, as mentioned below - 

```java
@Override
public void initialize(Bootstrap<DropwizardServiceConfiguration> bootstrap) {
    
    bootstrap.addBundle(new MultiTenantResolverBundle<DropwizardServiceConfiguration>() {
        @Override
        protected MultiTenantDataSourceConfiguration getMultiTenantDataSourceConfiguration(
                                                        DropwizardServiceConfiguration configuration) {
            return configuration.getMultiTenantDataSourceConfiguration();
        }

        @Override
        protected List<String> getPackagesToScan() {
            List<String> packagesToScan  = new LinkedList<String>();
            packagesToScan.add("com.sample.service.model");
            packagesToScan.add("com.sample.service.extensions");
            return packagesToScan;
        }
    });
```
 and define the Configuration in Service Configuration like -
 
 ```java
@Valid
private MultiTenantDataSourceConfiguration multiTenantDataSourceConfiguration = 
                                              new MultiTenantDataSourceConfiguration();
```
 
 and define the provider for the same like -
 
 ```java
 @Provides
 @Singleton
 MultiTenantDataSourceConfiguration providesMultiTenantDsConfigurations(
                            Provider<DropwizardServiceConfiguration> provider) {
     return provider.get().getMultiTenantDataSourceConfiguration();
 }
 ```
 
<b> How to use </b>

If you want to access the entityManager, where this snippet is getting ececuted in the scope of request and request
has tenantHeader present -

```java
TenantResolver.getEntityManager()
```
and if you explicitly want to use the specific tenant's entity manager - 

```java
TenantResolver.getEntityManager(tenantName)
```
