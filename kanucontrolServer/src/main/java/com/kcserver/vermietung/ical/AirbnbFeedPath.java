package com.kcserver.vermietung.ical;
import java.util.regex.Pattern;
public final class AirbnbFeedPath {
    private AirbnbFeedPath() {}
    public static final String PREFIX = "/api/kalender/airbnb/";
    public static final String REGEX = "/api/kalender/airbnb/[A-Za-z0-9_-]{43}\\.ics";
    public static boolean matches(String method, String path) {
        return ("GET".equals(method) || "HEAD".equals(method)) && Pattern.matches(REGEX, path);
    }
}
