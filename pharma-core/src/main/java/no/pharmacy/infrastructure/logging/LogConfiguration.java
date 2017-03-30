package no.pharmacy.infrastructure.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.filter.Filter;

public class LogConfiguration {

    private LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

    void addSlackAppender(String slackWebhook) {
        addAppender(new SlackAppender(context, slackWebhook, createThresholdFilter(Level.WARN)));
    }

    static Filter<ILoggingEvent> createThresholdFilter(Level level) {
        ThresholdFilter filter = new ThresholdFilter();
        filter.setLevel(level.levelStr);
        filter.start();
        return filter;
    }

    private void addAppender(Appender<ILoggingEvent> appender) {
        appender.setContext(context);
        appender.start();
        context.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(appender);
    }

    public void setLevel(String name, Level level) {
        context.getLogger(name).setLevel(level);
    }

    public LoggerContext getContext() {
        return context;
    }

}
