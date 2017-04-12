package no.pharmacy.web.infrastructure.auth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class IdCheckServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain");

        resp.getWriter().println(req.getUserPrincipal().getName());
        resp.getWriter().println();
        OpenIdPrincipal openIdPrincipal = (OpenIdPrincipal)req.getUserPrincipal();
        resp.getWriter().println("HER-nummer: " + openIdPrincipal.getToken().claim("HER-nummer").orElse("<Missing>"));
        resp.getWriter().println(openIdPrincipal.getToken().getPayload());
    }

}
