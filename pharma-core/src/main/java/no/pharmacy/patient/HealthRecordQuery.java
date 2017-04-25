package no.pharmacy.patient;

import java.util.Optional;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

public class HealthRecordQuery {

    public enum HealthRecordQueryPurpose {
        PATIENT_REQUEST, GUARDIAN_REQUEST, COUNTY_REQUEST
    }


    @Getter @Setter
    private UUID patientId;

    @Getter @Setter
    private String organizationHerNumber, operatorHprNumber, operatorJwtToken;

    @Getter @Setter
    private Optional<String> documentation = Optional.empty();
    @Getter @Setter
    private Optional<String> requestorIdType = Optional.empty();
    @Getter @Setter
    private Optional<String>requestorIdNumber = Optional.empty();

    @Getter @Setter
    private HealthRecordQueryPurpose purpose;

    public void setPurposeAsString(String purposeString) {
        this.purpose = HealthRecordQueryPurpose.valueOf(purposeString);
    }

}
