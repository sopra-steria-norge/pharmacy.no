package no.pharmacy.practitioner;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import no.pharmacy.core.PersonReference;

@ToString(of = {"identifier", "firstName", "lastName", "authorizations"})
public class Practitioner {

    @Getter @Setter
    private long identifier;

    // TODO: This should probably be removed due to privacy concerns
    @Getter @Setter
    private String nationalId;

    @Getter @Setter
    private String firstName, lastName;

    @Getter @Setter
    private LocalDate dateOfBirth;

    @Getter @Setter
    private Instant updatedAt;

    @Getter
    private Set<PractionerAuthorization> authorizations = new HashSet<>();

    public PersonReference getReference() {
        return new PersonReference(String.valueOf(identifier), firstName, lastName);
    }

    public String getDisplay() {
        return getFirstName() + " " + getLastName();
    }

}
