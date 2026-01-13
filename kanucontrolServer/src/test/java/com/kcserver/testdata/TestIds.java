package com.kcserver.testdata;

public final class TestIds {

    private TestIds() {}

    public static final String VEREIN_SEARCH = "EKC_SEARCH";
    public static final String VEREIN_DEFAULT = "EKC_DEFAULT";

    public static String unique(String base) {
        return base + "_" + System.nanoTime();
    }
}