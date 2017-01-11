package resolver;

import lib.InvalidTenantException;
import lib.TenantResolverConfig;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by anandhi on 02/06/15.
 */
public class TenantResolverFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if(request instanceof HttpServletRequest){
            String error = null;
            try{
                String headerValue = ((HttpServletRequest) request).getHeader(TenantResolverConfig.getTenantHeaderName());
                if(headerValue == null){
                   error = "Tenant header is Missing!";
                }

            TenantResolver.setEntityManagerForTenant(headerValue.toLowerCase());
            chain.doFilter(request, response);

            }catch (InvalidTenantException ite){
                error = ite.getMessage();
            }
            finally {
                if(TenantResolver.getEntityManager().isOpen()){
                    TenantResolver.getEntityManager().close();
                }

            }
            if(error != null){
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.setStatus(HttpStatus.BAD_REQUEST_400);
                httpResponse.getWriter().print(error);
            }
        }
    }

    @Override
    public void destroy() {
        System.out.println("I am here in after destroy ");
    }
}
