package org.ossfmct.projects.hostels.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Address {
    private String country = "";

    private String region = "";

    private String city = "";

    private String postalCode = "";

    private String street = "";

    private String house = "";

    public Address(String city, String postalCode, String street, String house) {
        this.city = city;
        this.postalCode = postalCode;
        this.street = street;
        this.house = house;
    }

    public static Address fromString(String string) {
        List<String> parts = Arrays.stream(string.split(","))
            .map(String::trim)
            .toList();

        if (parts.size() != 4 && parts.size() != 6) {
            throw new RuntimeException("Invalid address format: " + string);
        }

        Address address;
        if (parts.size() == 4) {
            address = new Address(parts.get(0), parts.get(1), parts.get(2), parts.get(3));
        } else {
            address = new Address(parts.get(0), parts.get(1), parts.get(2), parts.get(3), parts.get(4), parts.get(5));
        }

        return address;
    }

    public String stringFormat() {
        if (country.isEmpty() || region.isEmpty()) return simpleStringFormat();
        return country + ", "
            + region + " обл., "
            + "м." + city + ", "
            + postalCode + ", "
            + "вул." + street + ", "
            + house;
    }

    public String simpleStringFormat() {
        return "м." + city + ", "
            + postalCode + ", "
            + "вул." + street + ", "
            + house;
    }

    @Override
    public int hashCode() {
        return Objects.hash(country, postalCode, region, city, street, house);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) return true;
        if (!(object instanceof Address other)) return false;
        return this.country.equals(other.country)
            && this.postalCode.equals(other.postalCode)
            && this.region.equals(other.region)
            && this.city.equals(other.city)
            && this.street.equals(other.street)
            && this.house.equals(other.house);
    }

    @Override
    public String toString() {
        return "Address{"
            + "country='" + country + "',"
            + "postalCode='" + postalCode + "',"
            + "region='" + region + "',"
            + "city='" + city + "',"
            + "street='" + street + "',"
            + "house='" + house + "'"
            + "}";
    }
}