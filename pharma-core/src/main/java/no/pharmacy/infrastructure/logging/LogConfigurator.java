package no.pharmacy.infrastructure.logging;

import java.util.Arrays;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.CachingDateFormatter;

/**
 * Loaded via ServiceLoader by META-INFO/services/ch.qos.logback.classic.spi.Configurator
 * @see ch.qos.logback.classic.util.ContextInitializer#autoConfig()
 */
public class LogConfigurator extends ContextAwareBase implements Configurator {

    // Same as
    // layout.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
    private static class LogLayout extends LayoutBase<ILoggingEvent> {

        CachingDateFormatter cachingDateFormatter = new CachingDateFormatter("HH:mm:ss.SSS");
        ThrowableProxyConverter tpc = new ThrowableProxyConverter();

        @Override
        public void start() {
            tpc.setContext(getContext());
            tpc.setOptionList(Arrays.asList("20", "org.eclipse.jetty."));
            tpc.start();
            super.start();
        }

        @Override
        public String doLayout(ILoggingEvent event) {
            if (!isStarted()) {
                return CoreConstants.EMPTY_STRING;
            }
            StringBuilder sb = new StringBuilder();

            long timestamp = event.getTimeStamp();

            sb.append(cachingDateFormatter.format(timestamp));
            sb.append(" [");
            sb.append(event.getThreadName());
            sb.append("] ");
            sb.append(event.getLevel().toString());
            sb.append(" ");
            sb.append(event.getLoggerName());
            sb.append(" - ");
            sb.append(event.getFormattedMessage());
            sb.append(CoreConstants.LINE_SEPARATOR);
            IThrowableProxy tp = event.getThrowableProxy();
            if (tp != null) {
                String stackTrace = tpc.convert(event);
                sb.append(stackTrace);
            }
            return sb.toString();
        }
    }


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

        LogLayout layout = new LogLayout();
        layout.setContext(context);
        layout.start();
        encoder.setLayout(layout);

        console.setEncoder(encoder);
        console.start();
        return console;
    }
}
