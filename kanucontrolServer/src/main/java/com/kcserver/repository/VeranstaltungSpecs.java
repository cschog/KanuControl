package com.kcserver.repository;

import com.kcserver.entity.Veranstaltung;
import org.springframework.data.jpa.domain.Specification;

public final class VeranstaltungSpecs {

    private VeranstaltungSpecs() {
        // utility class
    }

    public static Specification<Veranstaltung> nameContains(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) {
                return null;
            }
            return cb.like(
                    cb.lower(root.get("name")),
                    "%" + name.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Veranstaltung> aktivEquals(Boolean aktiv) {
        return (root, query, cb) -> {
            if (aktiv == null) {
                return null;
            }
            return cb.equal(root.get("aktiv"), aktiv);
        };
    }
}