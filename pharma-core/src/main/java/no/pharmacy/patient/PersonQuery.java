package no.pharmacy.patient;

import lombok.Getter;
import lombok.Setter;

public class PersonQuery {

    @Getter @Setter
    private String firstName, lastName;

    @Getter @Setter
    private String nationalId;

    public void validateQuery() {
        if (!isMissing(nationalId)) {
            return;
        }
        if (isMissing(firstName) || isMissing(lastName)) {
            throw new IllegalArgumentException("PersonQuery must have first and last name");
        }

    }

    private boolean isMissing(String string) {
        return string == null || string.isEmpty();
    }
}
