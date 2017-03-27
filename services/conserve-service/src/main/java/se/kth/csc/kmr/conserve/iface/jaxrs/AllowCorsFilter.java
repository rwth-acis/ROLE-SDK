package se.kth.csc.kmr.conserve.iface.jaxrs;

import javax.inject.Singleton;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by adabru on 06.03.17.
 */
@Singleton
public class AllowCorsFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse res = (HttpServletResponse) servletResponse;
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        res.addHeader("Access-Control-Allow-Origin", "*");
        if(req.getHeader("Origin") != null)
            res.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        res.addHeader("Access-Control-Allow-Methods", "*");
        res.addHeader("Access-Control-Allow-Headers", req.getHeader("Access-Control-Request-Headers"));
        res.addHeader("Access-Control-Allow-Credentials", "true");
        filterChain.doFilter(servletRequest, servletResponse);
        if(!res.containsHeader("Access-Control-Allow-Origin"))
            res.addHeader("Access-Control-Allow-Origin", "*");
        if(req.getHeader("Origin") != null)
            res.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        if(!res.containsHeader("Access-Control-Allow-Methods"))
            res.addHeader("Access-Control-Allow-Methods", "*");
        if(!res.containsHeader("Access-Control-Allow-Headers"))
            res.addHeader("Access-Control-Allow-Headers", req.getHeader("Access-Control-Request-Headers"));
        if(!res.containsHeader("Access-Control-Allow-Credentials"))
            res.addHeader("Access-Control-Allow-Credentials", "true");
    }

    @Override
    public void destroy() {

    }
}
