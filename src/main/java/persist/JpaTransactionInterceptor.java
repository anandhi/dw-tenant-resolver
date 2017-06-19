package persist;

import com.google.inject.persist.Transactional;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import resolver.TenantResolver;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.lang.reflect.Method;

/**
 * Created by anupama.agarwal on 05/01/17.
 */
public class JpaTransactionInterceptor implements MethodInterceptor {


    @Transactional
    private static class Internal {
    }

    /**
     * Invoke method overridden from @link MethodInterceptor interface
     * This method is intended to be called on instantiation of the object
     *
     * @param methodInvocation
     * @return Object
     * @throws Throwable
     */
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        EntityManager em = TenantResolver.getEntityManager();
        Transactional transactional = readTransactionMetadata(methodInvocation);

        //Allow joining of transactions if there is an enclosing @Transactional method.
        if (em.getTransaction().isActive()) {
            return methodInvocation.proceed();
        }

        EntityTransaction txn = em.getTransaction();
        txn.begin();
        Object result;
        try {
            result = methodInvocation.proceed();

        } catch (Exception e) {
            handleException(transactional, e, txn);
            throw e;

        }
        // committing transaction since everything went fine
        txn.commit();
        return result;
    }


    private Transactional readTransactionMetadata(MethodInvocation methodInvocation) {
        Transactional transactional;
        Method method = methodInvocation.getMethod();
        Class<?> targetClass = methodInvocation.getThis().getClass();

        transactional = method.getAnnotation(Transactional.class);

        if (null == transactional) {
            transactional = targetClass.getAnnotation(Transactional.class);
        }

        if (null == transactional) {
            transactional = Internal.class.getAnnotation(Transactional.class);
        }

        return transactional;
    }


    /**
     * Returns True if rollback DID NOT HAPPEN (i.e. if commit should continue).
     *
     * @param transactional The metadata annotaiton of the method
     * @param e             The exception to test for rollback
     * @param txn           A JPA Transaction to issue rollbacks on
     */
    private boolean isRollbackNecessary(
            Transactional transactional, Exception e, EntityTransaction txn) {
        boolean commit = true;

        //check rollback clauses
        for (Class<? extends Exception> rollBackOn : transactional.rollbackOn()) {

            //if one matched, try to perform a rollback
            if (rollBackOn.isInstance(e)) {
                commit = false;

                //check ignore clauses (supercedes rollback clause)
                for (Class<? extends Exception> exceptOn : transactional.ignore()) {
                    //An exception to the rollback clause was found, DON'T rollback
                    // (i.e. commit and throw anyway)
                    if (exceptOn.isInstance(e)) {
                        commit = true;
                        break;
                    }
                }

                //rollback only if nothing matched the ignore check
                if (!commit) {
                    txn.rollback();
                }
                //otherwise continue to commit

                break;
            }
        }

        return commit;
    }

    /**
     * Returns True if rollback should happen
     *
     * @param transactional The metadata annotaiton of the method
     * @param e             The exception to test for rollback
     */

    private boolean isRollbackRequired(Transactional transactional, Exception e) {
        boolean rollback = false;

        //check rollback clauses
        for (Class<? extends Exception> rollBackOn : transactional.rollbackOn()) {

            //if one matched, try to perform a rollback
            if (rollBackOn.isInstance(e)) {
                rollback = true;

                //check ignore clauses (supercedes rollback clause)
                for (Class<? extends Exception> exceptOn : transactional.ignore()) {
                    //An exception to the rollback clause was found, DON'T rollback
                    // (i.e. commit and throw anyway)
                    if (exceptOn.isInstance(e)) {
                        rollback = false;
                        break;
                    }
                }
                break;
            }
        }

        return rollback;

    }

    /**
     * Handle exception, if rollback is required, rollback, otherwise commit
     *
     * @param transactional The metadata annotaiton of the method
     * @param e             The exception to test for rollback
     * @param txn           A JPA Transaction to issue rollbacks on
     */

    private void handleException(Transactional transactional, Exception e, EntityTransaction txn) {
        if (isRollbackRequired(transactional, e)) {
            txn.rollback();
        } else {
            txn.commit();
        }
    }
}
