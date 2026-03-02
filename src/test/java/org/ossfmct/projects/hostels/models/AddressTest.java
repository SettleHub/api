package org.ossfmct.projects.hostels.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AddressTest {

    @Test
    void testSimpleStringFormat() {
        Address address = new Address("Київ", "01001", "Хрещатик", "10");
        String expected = "м.Київ, 01001, вул.Хрещатик, 10";
        assertEquals(expected, address.simpleStringFormat());
    }

    @Test
    void testStringFormatWithCountryAndRegion() {
        Address address = new Address("Україна", "Київська", "Київ", "01001", "Хрещатик", "10");
        String expected = "Україна, Київська обл., м.Київ, 01001, вул.Хрещатик, 10";
        assertEquals(expected, address.stringFormat());
    }

    @Test
    void testStringFormatFallbackToSimple() {
        Address address = new Address("Київ", "01001", "Хрещатик", "10");
        // Country and region are empty by default
        String expected = "м.Київ, 01001, вул.Хрещатик, 10";
        assertEquals(expected, address.stringFormat());
    }

    @Test
    void testFromStringWithSimpleFormat() {
        String input = "Київ, 01001, Хрещатик, 10";
        Address expected = new Address("Київ", "01001", "Хрещатик", "10");
        Address actual = Address.fromString(input);
        assertEquals(expected, actual);
    }

    @Test
    void testFromStringWithFullFormat() {
        String input = "Україна, Київська, Київ, 01001, Хрещатик, 10";
        Address expected = new Address("Україна", "Київська", "Київ", "01001", "Хрещатик", "10");
        Address actual = Address.fromString(input);
        assertEquals(expected, actual);
    }

    @Test
    void testFromStringInvalidFormatTooFewParts() {
        String input = "Київ, 01001, Хрещатик"; // only 3 parts
        assertThrows(RuntimeException.class, () -> Address.fromString(input));
    }

    @Test
    void testFromStringInvalidFormatTooManyParts() {
        String input = "Україна, Київська, Київ, 01001, Хрещатик, 10, зайве";
        assertThrows(RuntimeException.class, () -> Address.fromString(input));
    }

    @Test
    void testEqualsAndHashCode() {
        Address a1 = new Address("Україна", "Київська", "Київ", "01001", "Хрещатик", "10");
        Address a2 = new Address("Україна", "Київська", "Київ", "01001", "Хрещатик", "10");
        Address a3 = new Address("Україна", "Вінницька", "Вінниця", "21001", "Київська", "20");

        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());
        assertNotEquals(a1, a3);
    }

    @Test
    void testToString() {
        Address address = new Address("Україна", "Київська", "Київ", "01001", "Хрещатик", "10");
        String expected = "Address{country='Україна',postalCode='01001',region='Київська',city='Київ',street='Хрещатик',house='10'}";
        assertEquals(expected, address.toString());
    }
}
