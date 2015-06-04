# dw-tenant-resolver
Dropwizard bunlde which helps to achieve multi-tenancy in dropwizard application

<b>Configuration:</b>

  Add the database configuration for each tenant, whatever details which dropwizard DatabaseConfiguration accepts.
  Given below the example.
  
```yaml
multiTenantDataSourceConfiguration:
  tenantHeaderName: X_TENANT_ID
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
Above configuration accepts, header name - which is been used the resolve the tenant for each request and initializes
the necessary things to connect to defined datasource.

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

Wherver you want an access of entity manager, which is been executed while serving the request - 
```java
TenantResolver.getEntityManager()
```
and if you explicitly want to use the specific tenant's entity manager - 

```java
TenantResolver.getEntityManager(tenantName)
```
Note: This bundle, also registers universal filter, where it expects all the requests should have headerName
which is been defined in the configuration and value of it should be one of the valid tenants <i>[Valid tenants list
is taken from the config where you have defined DB details for it]</i>
