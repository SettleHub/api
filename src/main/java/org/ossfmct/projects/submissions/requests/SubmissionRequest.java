package org.ossfmct.projects.submissions.requests;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.ossfmct.projects.spreadsheets.enums.Gender;
import org.ossfmct.projects.submissions.enums.SubmissionType;
import java.util.HashMap;

@Setter
@Getter
public class SubmissionRequest {

    @NotNull
    private Long submitterId;

    private HashMap<String, String> phones = new HashMap<>();

    private HashMap<String, String> emails = new HashMap<>();

    private SubmissionType type = SubmissionType.SETTLEMENT_STUDENT_FOR_A_YEAR;

    private String description;

    private Long hostel;

    private Long floor;

    private Long section;

    private String block;

    private String blockGender;

    private String room;
}
