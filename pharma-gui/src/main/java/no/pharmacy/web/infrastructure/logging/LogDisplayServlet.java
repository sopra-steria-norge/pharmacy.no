package no.pharmacy.web.infrastructure.logging;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import no.pharmacy.infrastructure.logging.CyclicBufferAppender;

public class LogDisplayServlet extends HttpServlet {

    private Map<String, CyclicBufferAppender> buffers = new LinkedHashMap<>();

    public LogDisplayServlet(LoggerContext context) {
        ch.qos.logback.classic.Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);

        Level[] levels = new Level[] { Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR };
        for (Level level : levels) {
            CyclicBufferAppender appender = new CyclicBufferAppender(level, 200);
            appender.setContext(context);
            appender.start();
            buffers.put(level.levelStr, appender);
            rootLogger.addAppender(appender);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");

        resp.getWriter().write("<h2>Levels</h2>");
        resp.getWriter().write("<ol>");
        for (String level : buffers.keySet()) {
            String path = req.getContextPath() + req.getServletPath() + "/" + level;
            resp.getWriter().write("<li><a href='" + path + "'>" + level + "</a></li>\n");
        }
        resp.getWriter().write("</ol>");

        if (req.getPathInfo() == null) {
            return;
        }

        String level = req.getPathInfo().substring(1);
        buffers.get(level).writeHtml(resp.getWriter());
    }
}
