package no.pharmacy.infrastructure.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.layout.TTLLLayout;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.spi.ContextAwareBase;

/**
 * Loaded via ServiceLoader by META-INFO/services/ch.qos.logback.classic.spi.Configurator
 * @see ch.qos.logback.classic.util.ContextInitializer#autoConfig()
 */
public class LogConfigurator extends ContextAwareBase implements Configurator {

    @Override
    public void configure(LoggerContext context) {
        addInfo("Setting up default configuration.");

        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(createConsoleAppender(context));

        rootLogger.setLevel(Level.WARN);
        context.getLogger("no.pharmacy").setLevel(Level.DEBUG);
    }

    private ConsoleAppender<ILoggingEvent> createConsoleAppender(LoggerContext context) {
        ConsoleAppender<ILoggingEvent> console = new ConsoleAppender<ILoggingEvent>();
        console.setContext(context);
        console.setName("console");
        LayoutWrappingEncoder<ILoggingEvent> encoder = new LayoutWrappingEncoder<ILoggingEvent>();
        encoder.setContext(context);

        // same as
        // PatternLayout layout = new PatternLayout();
        // layout.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
        TTLLLayout layout = new TTLLLayout();

        layout.setContext(context);
        layout.start();
        encoder.setLayout(layout);

        console.setEncoder(encoder);
        console.start();
        return console;
    }
}
