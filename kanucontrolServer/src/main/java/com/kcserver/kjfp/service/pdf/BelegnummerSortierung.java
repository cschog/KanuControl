package com.kcserver.kjfp.service.pdf;

import java.math.BigInteger;
import java.util.Comparator;

public final class BelegnummerSortierung {
    private BelegnummerSortierung() {}
    public static final Comparator<String> NUMMERN = Comparator.nullsLast((a, b) -> {
        var muster = java.util.regex.Pattern.compile("^(.*_)([0-9]+)$");
        var ma = muster.matcher(a);
        var mb = muster.matcher(b);
        if (ma.matches() && mb.matches()) {
            int prefix = ma.group(1).compareTo(mb.group(1));
            if (prefix != 0) return prefix;
            return new BigInteger(ma.group(2)).compareTo(new BigInteger(mb.group(2)));
        }
        return a.compareTo(b);
    });
}
