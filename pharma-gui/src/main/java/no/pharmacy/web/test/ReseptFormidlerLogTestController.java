package no.pharmacy.web.test;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.pharmacy.test.FakeReseptFormidler;
import no.pharmacy.test.MessageLogEntry;

public class ReseptFormidlerLogTestController extends HttpServlet {

    private FakeReseptFormidler reseptFormidler;

    public ReseptFormidlerLogTestController(FakeReseptFormidler reseptFormidler) {
        this.reseptFormidler = reseptFormidler;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");

        resp.getWriter().write("<h1>RF meldingslogg</h1>");

        for (MessageLogEntry entry : reseptFormidler.getMessageLog()) {
            resp.getWriter().write("<h3>" + entry.getParticipants() + " (" + entry.getTimestamp() + ")</h3>");
            resp.getWriter().write("<textarea cols=100 rows=5 readonly>" + entry.getMessage().toXML("  ") + "</textarea>");
        }
    }

}