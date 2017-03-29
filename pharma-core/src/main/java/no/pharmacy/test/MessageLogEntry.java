package no.pharmacy.test;

import java.time.Instant;

import org.eaxy.Element;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MessageLogEntry {

    @Getter
    private final String participants;

    @Getter
    private final Instant timestamp;

    @Getter
    private final Element message;

}
