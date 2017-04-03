package no.pharmacy.practitioner;

import lombok.Getter;
import lombok.Setter;
import no.pharmacy.core.Reference;

public class Practitioner {

    @Getter @Setter
    private long identifier;

    @Getter @Setter
    private String name;

    public Reference getReference() {
        return new Reference(String.valueOf(identifier), name);
    }

}
