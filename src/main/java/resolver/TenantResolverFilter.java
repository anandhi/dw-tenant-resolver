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
            try{
                String headerValue = ((HttpServletRequest) request).getHeader(TenantResolverConfig.getTenantHeaderName());
                if(TenantResolverConfig.getTenantHeaderName().contains(headerValue.toLowerCase())){
                    TenantResolver.setEntityManagerForTenant(headerValue);
                    chain.doFilter(request, response);
                }
            }catch (InvalidTenantException ite){
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.setStatus(HttpStatus.BAD_REQUEST_400);
                httpResponse.getWriter().print(ite.getMessage());
            }
        }
    }

    @Override
    public void destroy() {

    }
}
