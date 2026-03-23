package com.kcserver.repository;

import com.kcserver.dto.veranstaltung.VeranstaltungFilterDTO;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.enumtype.VeranstaltungTyp;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

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
    public static Specification<Veranstaltung> vereinEquals(Long vereinId) {
        return (root, query, cb) -> {
            if (vereinId == null) return null;
            return cb.equal(root.get("verein").get("id"), vereinId);
        };
    }

    public static Specification<Veranstaltung> beginnDatum(LocalDate von) {
        return (root, query, cb) -> {
            if (von == null) return null;
            return cb.greaterThanOrEqualTo(root.get("beginnDatum"), von);
        };
    }

    public static Specification<Veranstaltung> endeDatum(LocalDate bis) {
        return (root, query, cb) -> {
            if (bis == null) return null;
            return cb.lessThanOrEqualTo(root.get("beginnDatum"), bis);
        };
    }

    public static Specification<Veranstaltung> typEquals(VeranstaltungTyp typ) {
        return (root, query, cb) -> {
            if (typ == null) return null;
            return cb.equal(root.get("typ"), typ);
        };
    }
    public static Specification<Veranstaltung> filter(VeranstaltungFilterDTO f) {
        return Specification.allOf(
                nameContains(f.getName()),
                aktivEquals(f.getAktiv()),
                vereinEquals(f.getVereinId()),
                beginnDatum(f.getBeginnDatum()),
                endeDatum(f.getEndeDatum()),
                typEquals(f.getTyp())
        );
    }
}