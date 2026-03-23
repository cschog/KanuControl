package com.kcserver.support.builder;

import com.kcserver.entity.Verein;

public class VereinBuilder {

    private static long counter = 0;

    private String name;
    private String abk;

    public VereinBuilder() {
        counter++;

        this.name = "Verein " + counter;
        this.abk = "V" + counter; // ✅ max 10 Zeichen beachten!
    }

    public static VereinBuilder aVerein() {
        return new VereinBuilder();
    }

    public VereinBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public VereinBuilder withAbk(String abk) {
        this.abk = abk;
        return this;
    }

    public Verein build() {
        Verein v = new Verein();
        v.setName(name);
        v.setAbk(abk);
        return v;
    }

    public static void resetCounter() {
        counter = 0;
    }
}