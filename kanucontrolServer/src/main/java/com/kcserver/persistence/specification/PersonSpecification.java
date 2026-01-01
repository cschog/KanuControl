package com.kcserver.persistence.specification;

import com.kcserver.entity.Mitglied;
import com.kcserver.entity.Person;
import com.kcserver.enumtype.Sex;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class PersonSpecification {

    public static Specification<Person> byCriteria(
            String name,
            String vorname,
            Long vereinId,
            Boolean aktiv,
            Sex sex,
            Integer alterMin,
            Integer alterMax,
            String plz,
            String ort
    ) {
        return (root, query, cb) -> {

            Predicate predicate = cb.conjunction();

            if (name != null) {
                predicate = cb.and(predicate,
                        cb.like(cb.lower(root.get("name")),
                                "%" + name.toLowerCase() + "%"));
            }

            if (vorname != null) {
                predicate = cb.and(predicate,
                        cb.like(cb.lower(root.get("vorname")),
                                "%" + vorname.toLowerCase() + "%"));
            }

            if (sex != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("sex"), sex));
            }

            if (plz != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("plz"), plz));
            }

            if (ort != null) {
                predicate = cb.and(predicate,
                        cb.like(cb.lower(root.get("ort")),
                                "%" + ort.toLowerCase() + "%"));
            }

            // Alter → Geburtsdatum
            LocalDate today = LocalDate.now();

            if (alterMin != null) {
                LocalDate latestBirthdate = today.minusYears(alterMin);
                predicate = cb.and(predicate,
                        cb.lessThanOrEqualTo(root.get("geburtsdatum"), latestBirthdate));
            }

            if (alterMax != null) {
                LocalDate earliestBirthdate = today.minusYears(alterMax + 1).plusDays(1);
                predicate = cb.and(predicate,
                        cb.greaterThanOrEqualTo(root.get("geburtsdatum"), earliestBirthdate));
            }

            // Mitgliedschaft (JOIN)
            if (vereinId != null || aktiv != null) {

                Join<Person, Mitglied> mitgliedJoin =
                        root.join("mitgliedschaften", JoinType.LEFT);

                if (vereinId != null) {
                    predicate = cb.and(predicate,
                            cb.equal(mitgliedJoin.get("verein").get("id"), vereinId));
                }

                if (aktiv != null) {
                    predicate = cb.and(predicate,
                            cb.equal(mitgliedJoin.get("aktiv"), aktiv));
                }

                query.distinct(true);
            }

            return predicate;
        };
    }
}