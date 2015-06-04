import lib.InvalidTenantException;
import lib.TenantResolverConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import resolver.TenantDataSourceFactory;
import resolver.TenantResolver;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by anandhi on 04/06/15.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({TenantResolverConfig.class, TenantDataSourceFactory.class})
public class TenantResolverTest {
    private static EntityManager entityManagerForTenantOne = Mockito.mock(EntityManager.class);
    private static List<String> validTenants = new LinkedList<String>(Arrays.asList("tenant_one", "tenant_two"));
    private static Set<EntityManager> emList = new HashSet<EntityManager>();

    @Before
    @PrepareForTest
    public void setUp() throws InvalidTenantException {
        PowerMockito.mockStatic(TenantResolverConfig.class);
        PowerMockito.mockStatic(TenantDataSourceFactory.class);
        Mockito.when(TenantResolverConfig.getValidTenants()).thenReturn(validTenants);
        Mockito.when(TenantDataSourceFactory.createEntityManager("tenant_one")).thenReturn(entityManagerForTenantOne);
    }

    @Test(expected = InvalidTenantException.class)
    public void throwInvalidTenantExceptionWhenInvalidTenant() throws InvalidTenantException {
        TenantResolver.setEntityManagerForTenant("tenant_test");
    }

    @Test
    public void testValidEntityManagerWhenRequested(){
        try {
            assertThat(TenantResolver.getEntityManager("tenant_one")).isEqualTo(entityManagerForTenantOne);
        } catch (InvalidTenantException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSingleThreadWillHaveSingleOneInstanceOfEm() throws InvalidTenantException {
        Mockito.when(TenantDataSourceFactory.createEntityManager("tenant_one")).then(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return Mockito.mock(EntityManager.class);
            }
        });
        EntityManager emOne = TenantResolver.getEntityManager("tenant_one");
        EntityManager emTwo = TenantResolver.getEntityManager("tenant_one");
        assertThat(emOne).isEqualTo(emTwo);
    }

    @Test
    public void testThreadsDoNotShareEmInstance() throws InvalidTenantException, InterruptedException {
        Mockito.when(TenantDataSourceFactory.createEntityManager("tenant_one")).then(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return Mockito.mock(EntityManager.class);
            }
        });

        List<RequestSimulationThread> simulationThreads = new LinkedList<RequestSimulationThread>();
        final int threadCount = 10;
        for(int i = 0; i < threadCount; i++){
            RequestSimulationThread simulationThread = new RequestSimulationThread();
            simulationThreads.add(simulationThread);
            simulationThread.start();
        }

        for(RequestSimulationThread thread : simulationThreads){
            thread.join();
        }

        assertThat(emList.size()).isEqualTo(threadCount);

    }

    private class RequestSimulationThread extends Thread {

        @Override
        public void run() {
            try {
                EntityManager em = TenantResolver.getEntityManager("tenant_one");
                emList.add(em);
            } catch (InvalidTenantException e) {
                e.printStackTrace();
            }
        }
    }
}
