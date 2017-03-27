package no.pharmacy.core;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode
@RequiredArgsConstructor
@ToString
public class Reference {

    @Getter
    private final String reference;

    @Getter
    private final String display;

}
