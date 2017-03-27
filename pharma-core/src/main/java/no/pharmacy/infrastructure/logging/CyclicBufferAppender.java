package no.pharmacy.infrastructure.logging;

import java.io.PrintWriter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.helpers.CyclicBuffer;

public class CyclicBufferAppender extends AppenderBase<ILoggingEvent> {

    private CyclicBuffer<String> buffer;
    private PatternLayout layout;
    private Level level;

    public CyclicBufferAppender(Level level, int size) {
        this.level = level;
        buffer = new CyclicBuffer<>(size);
    }

    @Override
    public void start() {
        super.start();
        layout = new PatternLayout();
        layout.setContext(context);
        layout.setPattern("%d %-5level [%thread]: %message%n");
        layout.start();

        addFilter(LogConfiguration.createThresholdFilter(level));
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        buffer.add(layout.doLayout(eventObject));
    }

    public void writeHtml(PrintWriter writer) {
        writer.write("<h2>Messages at level: " + level + "</h2>");
        writer.write("<pre>");
        for (int i = 0; i < buffer.length(); i++) {
            writer.write(buffer.get(i));
        }
        writer.write("</pre>");
    }

}
