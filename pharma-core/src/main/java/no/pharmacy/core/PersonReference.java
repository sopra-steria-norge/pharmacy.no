package no.pharmacy.core;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EqualsAndHashCode(of = "reference")
public class PersonReference {

    @Getter
    private final String reference;

    @Getter
    private final String firstName;

    @Getter
    private final String lastName;


    public String getDisplay() {
        return firstName + " " + lastName;
    }


    public PersonReference(String reference, String fullName) {
        this.reference = reference;
        int lastNamePos = fullName.lastIndexOf(' ');
        this.firstName = fullName.substring(0, lastNamePos);
        this.lastName = fullName.substring(lastNamePos+1);
    }

}
