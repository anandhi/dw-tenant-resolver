package resolver;

import lib.InvalidTenantException;
import lib.TenantResolverConfig;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by anandhi on 02/06/15.
 */
public class TenantResolverFilter implements Filter {
    public static final String PIVOTS_CONCAT_CHAR = ".";
    public static final String TENANT_PARAM_MISSING_MSG = "Tenant Parameter is Missing : ";
    public static final String HEADER_KEY = "header";
    public static final String URI_KEY = "uri";

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

    private String contrivePivotsValue(ServletRequest request) throws InvalidTenantException {
        String pivotsValue = null;

        if (TenantResolverConfig.getEnforceTenantHeaderInAllRequests()) {
            pivotsValue = extractHeaderValue(request, TenantResolverConfig.getTenantHeaderName(), true);
        }

        if (TenantResolverConfig.getAuxiliaryConfigurationPivots() != null) {
            for (List<List<String>> priorityConfigurationList : TenantResolverConfig.getAuxiliaryConfigurationPivots()) {
                String atomicPivotValue = derivePivotValue(request, priorityConfigurationList);
                if (pivotsValue == null) {
                    pivotsValue = atomicPivotValue;
                }
                else {
                    pivotsValue = pivotsValue + PIVOTS_CONCAT_CHAR + atomicPivotValue;
                }
            }
        }
        return pivotsValue;
    }

    private String derivePivotValue(ServletRequest request, List<List<String>> priorityConfigurationList)
            throws InvalidTenantException {
        String pivotValue =  null;
        String errorMessage = "";
        for (List<String> configList : priorityConfigurationList) {
            if(pivotValue == null) {
                pivotValue = extractPivotValue(request, configList.get(0).toLowerCase(), configList.get(1));
                if (pivotValue == null)
                    errorMessage += configList.get(0) + " MISSING. ";
            }
        }
        if(pivotValue == null) {
            String error = TENANT_PARAM_MISSING_MSG + errorMessage;
            throw new InvalidTenantException(pivotValue, error);
        }
        return pivotValue;
    }

    private String extractPivotValue(ServletRequest request, String key, String pivot) throws InvalidTenantException {
        String pivotValue;
        if(key.equals(URI_KEY)) {
            pivotValue = extractValueFromURI(request, pivot);
        } else if(key.equals(HEADER_KEY)) {
            pivotValue = extractHeaderValue(request, pivot, false);
        } else {
            pivotValue = null;
        }
        return pivotValue;
    }

    private String extractValueFromURI(ServletRequest request, String pivot) {
        String pivotValue = null;
        String requestURI = ((HttpServletRequest) request).getRequestURI();
//        Pattern pattern = Pattern.compile("(.*/(type|TYPE)/)(\\w*)");
        Pattern regexPattern = Pattern.compile("(.*/(" + pivot.toLowerCase() + "|" + pivot.toUpperCase() + ")/)(\\w*)");
        Matcher regexMatcher = regexPattern.matcher(requestURI);

        if (regexMatcher.find()) {
            pivotValue = regexMatcher.group(3);
        }
        return pivotValue;
    }

    private String extractHeaderValue(ServletRequest request, String pivot, boolean throwErrorIfNotPresent)
            throws InvalidTenantException {
        String headerValue = ((HttpServletRequest) request).getHeader(pivot);
        if(throwErrorIfNotPresent && headerValue == null) {
            String error = TENANT_PARAM_MISSING_MSG + pivot;
            throw new InvalidTenantException(pivot, error);
        }
        return headerValue;
    }
}
