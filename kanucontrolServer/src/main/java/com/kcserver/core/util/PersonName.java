package com.kcserver.core.util;

public final class PersonName {
    private PersonName() {}
    public static String vorname(String value) {
        String text = value == null ? "" : value.strip();
        return text.matches("_+") ? "" : text;
    }
    public static String surnameFirst(String name, String first) {
        String last = name == null ? "" : name.strip();
        String given = vorname(first);
        return given.isEmpty() ? last : last.isEmpty() ? given : last + ", " + given;
    }
    public static String givenFirst(String name, String first) {
        String last = name == null ? "" : name.strip();
        String given = vorname(first);
        return given.isEmpty() ? last : last.isEmpty() ? given : given + " " + last;
    }
}
