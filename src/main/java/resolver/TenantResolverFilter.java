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
    public static final String PIVOTS_CONCAT_CHAR = ".";
    public static final String TENANT_HEADER_MISSING_MSG = "Tenant header is Missing : ";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        if(request instanceof HttpServletRequest){
            String error = null;
            String pivotsValue;

            try {
                pivotsValue = contrivePivotsValue(request);
                TenantResolver.setEntityManagerForTenant(pivotsValue);
                chain.doFilter(request, response);
            }
            catch (InvalidTenantException ite) {
                error = ite.getMessage();
            }
            finally {
                if (TenantResolver.getEntityManager() != null && TenantResolver.getEntityManager().isOpen()) {
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
    }

    private String contrivePivotsValue (ServletRequest request) throws InvalidTenantException {
        String pivotsValue = null;

        if (TenantResolverConfig.getEnforceTenantHeaderInAllRequests()) {
            pivotsValue = extractHeaderValue(request, TenantResolverConfig.getTenantHeaderName());
        }
        if (TenantResolverConfig.getAuxiliaryHeaderPivots() != null) {
            for (String pivot : TenantResolverConfig.getAuxiliaryHeaderPivots()) {
                String headerValue = extractHeaderValue(request, pivot);
                if (pivotsValue == null) {
                    pivotsValue = headerValue;
                }
                else {
                    pivotsValue = pivotsValue + PIVOTS_CONCAT_CHAR + headerValue;
                }
            }
        }
        return pivotsValue;
    }

    private String extractHeaderValue (ServletRequest request, String pivot) throws InvalidTenantException {
        String error;
        String headerValue = ((HttpServletRequest) request).getHeader(pivot);
        if(headerValue == null){
            error = TENANT_HEADER_MISSING_MSG + pivot;
            throw new InvalidTenantException(pivot, error);
        }
        return headerValue;
    }
}
