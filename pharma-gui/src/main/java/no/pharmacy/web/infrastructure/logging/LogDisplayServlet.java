package no.pharmacy.web.infrastructure.logging;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.helpers.CyclicBuffer;
import no.pharmacy.infrastructure.logging.LogConfiguration;

public class LogDisplayServlet extends HttpServlet {

    private Map<String, CyclicBuffer<String>> buffers = new HashMap<>();


    public LogDisplayServlet(LoggerContext context) {
        PatternLayout layout = new PatternLayout();
        layout.setContext(context);
        layout.setPattern("%d %-5level [%thread]: %message%n");
        layout.start();

        ch.qos.logback.classic.Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);

        Level[] levels = new Level[] { Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR };
        for (Level level : levels) {
            CyclicBuffer<String> buffer = new CyclicBuffer<>(200);
            AppenderBase<ILoggingEvent> appender = new AppenderBase<ILoggingEvent>() {
                @Override
                protected void append(ILoggingEvent eventObject) {
                    buffer.add(layout.doLayout(eventObject));
                }
            };
            appender.addFilter(LogConfiguration.createThresholdFilter(level));
            appender.start();
            buffers.put(level.levelStr, buffer);
            rootLogger.addAppender(appender);
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");

        resp.getWriter().write("<h2>Levels</h2>");
        resp.getWriter().write("<ol>");
        for (String level : buffers.keySet()) {
            resp.getWriter().write("<li><a href='" + req.getContextPath() + req.getServletPath() + "/" + level + "'>"
                    + level + "</a></li>\n");
        }
        resp.getWriter().write("</ol>");

        if (req.getPathInfo() == null) {
            return;
        }

        String level = req.getPathInfo().substring(1);
        resp.getWriter().write("<h2>Messages at level: " + level + "</h2>");
        resp.getWriter().write("<pre>");

        CyclicBuffer<String> buffer = buffers.get(level);
        for (int i = 0; i < buffer.length(); i++) {
            resp.getWriter().write(buffer.get(i));
        }

        resp.getWriter().write("</pre>");
    }

}
