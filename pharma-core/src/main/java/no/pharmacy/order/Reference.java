package no.pharmacy.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Reference {

    @Getter
    private final String reference;

    @Getter
    private final String display;

}
