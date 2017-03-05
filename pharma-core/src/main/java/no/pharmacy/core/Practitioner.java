package no.pharmacy.core;

import lombok.Getter;
import lombok.Setter;
import no.pharmacy.order.Reference;

public class Practitioner {

    @Getter @Setter
    private long identifier;

    @Getter @Setter
    private String name;

    public Reference getReference() {
        return new Reference(String.valueOf(identifier), name);
    }

}
