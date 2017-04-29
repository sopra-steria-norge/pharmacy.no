package no.pharmacy.web.test;

import java.io.IOException;
import java.util.LinkedList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.pharmacy.test.MessageLogEntry;
import no.pharmacy.test.PrescriptionSimulator;

public class ReseptFormidlerLogTestController extends HttpServlet {

    private PrescriptionSimulator reseptFormidler;

    public ReseptFormidlerLogTestController(PrescriptionSimulator reseptFormidler) {
        this.reseptFormidler = reseptFormidler;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");

        resp.getWriter().write("<h1>RF meldingslogg</h1>");

        for (MessageLogEntry entry : reseptFormidler.getMessageLog()) {
            resp.getWriter().write("<h3>" + entry.getParticipants() + " (" + entry.getTimestamp() + ")</h3>");
            resp.getWriter().write("<textarea cols=100 rows=5 readonly>");
            entry.getMessage().writeIndentedTo(resp.getWriter(), new LinkedList<>(), "  ", "");
            resp.getWriter().write("</textarea>");
        }
    }

}
