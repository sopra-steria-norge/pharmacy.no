package no.pharmacy.infrastructure.logging;

import java.io.IOException;
import java.net.URL;

import org.jsonbuddy.JsonObject;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.filter.Filter;
import no.pharmacy.infrastructure.IOUtil;

public class SlackAppender extends AsyncAppender {

    private String slackWebhook;

    SlackAppender(LoggerContext context, String slackWebhook, Filter<ILoggingEvent> filter) {
        this.slackWebhook = slackWebhook;
        setContext(context);
        setName(slackWebhook);
        addFilter(filter);

        AppenderBase<ILoggingEvent> appender = createAppender();
        appender.setName(slackWebhook);
        appender.setContext(context);
        addAppender(appender);
        appender.start();
    }

    private AppenderBase<ILoggingEvent> createAppender() {
        return new AppenderBase<ILoggingEvent>() {
            @Override
            public synchronized void append(ILoggingEvent eventObject) {
                sendToSlack(eventObject);
            }
        };
    }

    private void sendToSlack(ILoggingEvent event) {
        JsonObject message = new JsonObject()
                .put("text", event.getFormattedMessage());

        try {
            IOUtil.post(message.toJson(), new URL(slackWebhook));
        } catch (IOException e) {
            addError("Failed to post to slack", e);
        }
    }

}
