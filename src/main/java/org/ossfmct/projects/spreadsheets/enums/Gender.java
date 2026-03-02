package org.ossfmct.projects.spreadsheets.enums;

public enum Gender {
    MALE,
    FEMALE,
    NOT_SPECIFIED;

    public static Gender fromString(String value) {
        if (value == null) return NOT_SPECIFIED;
        try {
            return Gender.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NOT_SPECIFIED;
        }
    }
}
