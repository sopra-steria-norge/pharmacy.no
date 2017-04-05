package no.pharmacy.web.infrastructure.auth;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.security.DefaultUserIdentity;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.UserIdentity;

public class AuthenticationFilter implements Filter {

    private static final String SESSION_AUTH = "MySession";
    private AuthenticationConfiguration authConfig;


    public AuthenticationFilter(AuthenticationConfiguration authConfig) {
        this.authConfig = authConfig;
    }

    @Override
    public void destroy() {
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        if (req.getSession().getAttribute(SESSION_AUTH) != null) {
            Authentication auth = (Authentication) req.getSession().getAttribute(SESSION_AUTH);
            ((Request)request).setAuthentication(auth);
            chain.doFilter(request, response);
        } else if (req.getParameter("id_token") != null) {
            Authentication auth = decodeAuthentication(req.getParameter("id_token"));
            ((Request)request).setAuthentication(auth);
            req.getSession().setAttribute(SESSION_AUTH, auth);
            resp.sendRedirect(authConfig.getCurrentUri(req));
        } else {
            resp.sendRedirect(authConfig.getRedirectUrl(req));
        }
    }

    private Authentication decodeAuthentication(String idToken) {
        OpenIdPrincipal userPrincipal = authConfig.decodeUserPrincipal(idToken);
        UserIdentity userIdentity = new DefaultUserIdentity(userPrincipal.getSubject(), userPrincipal, userPrincipal.getRoles());
        Authentication auth = new UserAuthentication("OpenID", userIdentity);
        return auth;
    }
}
