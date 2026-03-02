package org.ossfmct.projects.spreadsheets.models;

import lombok.Getter;
import lombok.Setter;
import org.ossfmct.projects.spreadsheets.enums.Gender;
import java.util.Objects;

@Getter
@Setter
public class Resident {

    public Resident() {};

    public Resident(short bedPlace, String roomNumber, String firstName, String lastName, String middleName, Gender gender, String universityFacultyInstitute, String courseAndGroup, String contractNumber, String contactPhone, String applicationPlan2023_24, short settled, short places) {
        this.bedPlace = bedPlace;
        this.roomNumber = roomNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.gender = gender;
        this.universityFacultyInstitute = universityFacultyInstitute;
        this.courseAndGroup = courseAndGroup;
        this.contractNumber = contractNumber;
        this.contactPhone = contactPhone;
        this.applicationPlan2023_24 = applicationPlan2023_24;
        this.settled = settled;
        this.places = places;
    }

    private short bedPlace;

    private String roomNumber;

    private String firstName;

    private String lastName;

    private String middleName;

    private Gender gender;

    private String universityFacultyInstitute;

    private String courseAndGroup;

    private String contractNumber;

    private String contactPhone;

    private String applicationPlan2023_24;

    private short settled;

    private short places;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Resident)) return false;

        Resident resident = (Resident) object;
        return this.bedPlace == resident.bedPlace
            && this.roomNumber.equals(resident.roomNumber)
            && this.firstName.equals(resident.firstName)
            && this.lastName.equals(resident.lastName)
            && this.middleName.equals(resident.middleName)
            && this.gender.equals(resident.gender)
            && this.universityFacultyInstitute.equals(resident.universityFacultyInstitute)
            && this.courseAndGroup.equals(resident.courseAndGroup)
            && this.contractNumber.equals(resident.contractNumber)
            && this.contactPhone.equals(resident.contactPhone)
            && this.applicationPlan2023_24.equals(resident.applicationPlan2023_24)
            && this.settled == resident.settled
            && this.places == resident.places;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            bedPlace,
            roomNumber,
            firstName,
            lastName,
            middleName,
            gender,
            universityFacultyInstitute,
            courseAndGroup,
            contractNumber,
            contactPhone,
            applicationPlan2023_24,
            settled,
            places
        );
    }

    @Override
    public String toString() {
        return "Resident{"
            + "bedPlace=" + bedPlace
            + ", roomNumber=" + roomNumber
            + ", firstName=" + firstName
            + ", lastName=" + lastName
            + ", middleName=" + middleName
            + ", gender=" + gender
            + ", universityFacultyInstitute=" + universityFacultyInstitute
            + ", courseAndGroup=" + courseAndGroup
            + ", contractNumber=" + contractNumber
            + ", contactPhone=" + contactPhone
            + ", applicationPlan2023_24=" + applicationPlan2023_24
            + ", settled=" + settled
            + ", places=" + places
            + "}";
    }
}
