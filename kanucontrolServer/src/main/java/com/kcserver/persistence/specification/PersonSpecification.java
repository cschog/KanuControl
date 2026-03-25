package com.kcserver.persistence.specification;

import com.kcserver.dto.person.PersonSearchCriteria;
import com.kcserver.entity.Mitglied;
import com.kcserver.entity.Person;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import static com.kcserver.util.StringUtils.hasText;

public class PersonSpecification {

    public static Specification<Person> byCriteria(PersonSearchCriteria c) {
        return (root, query, cb) -> {



            if (query != null && !Long.class.equals(query.getResultType())) {
                query.distinct(true);
            }
            Predicate predicate = cb.conjunction();

            // =========================
// Name / Vorname (Kombifeld)
// =========================
            if (hasText(c.getSearch())) {

                String search = c.getSearch().toLowerCase().trim();
                String[] parts = search.split("\\s+");

                if (parts.length == 1) {
                    String s = parts[0];

                    Predicate nameLike = cb.like(cb.lower(root.get("name")), "%" + s + "%");
                    Predicate vornameLike = cb.like(cb.lower(root.get("vorname")), "%" + s + "%");

                    predicate = cb.and(predicate, cb.or(nameLike, vornameLike));
                }

                if (parts.length >= 2) {
                    String a = parts[0];
                    String b = parts[1];

                    Predicate vornameName = cb.and(
                            cb.like(cb.lower(root.get("vorname")), "%" + a + "%"),
                            cb.like(cb.lower(root.get("name")), "%" + b + "%")
                    );

                    Predicate nameVorname = cb.and(
                            cb.like(cb.lower(root.get("name")), "%" + a + "%"),
                            cb.like(cb.lower(root.get("vorname")), "%" + b + "%")
                    );

                    predicate = cb.and(predicate, cb.or(vornameName, nameVorname));
                }
            }

// =========================
// Vorname separat
// =========================
            if (!hasText(c.getSearch()) && hasText(c.getVorname())) {
                predicate = cb.and(predicate,
                        cb.like(
                                cb.lower(root.get("vorname")),
                                "%" + c.getVorname().toLowerCase() + "%"
                        )
                );
            }
            // Sex
            if (c.getSex() != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("sex"), c.getSex()));
            }

            // Aktiv (Default: true)
            Boolean aktiv = c.getAktiv();
            // Aktiv (nur filtern wenn gesetzt)
            if (c.getAktiv() != null) {
                predicate = cb.and(
                        predicate,
                        cb.equal(root.get("aktiv"), c.getAktiv())
                );
            }

            // Unvollständig
            if (Boolean.TRUE.equals(c.getUnvollstaendig())) {
                predicate = cb.and(predicate,
                        cb.isNull(root.get("geburtsdatum")));
            }

            // PLZ
            if (hasText(c.getPlz())) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("plz"), c.getPlz()));
            }

            // Ort
            if (hasText(c.getOrt())) {
                predicate = cb.and(predicate,
                        cb.like(cb.lower(root.get("ort")),
                                "%" + c.getOrt().toLowerCase() + "%"));
            }

            // Verein
            if (c.getVereinId() != null) {
                Join<Person, Mitglied> join =
                        root.join("mitgliedschaften", JoinType.INNER);
                predicate = cb.and(predicate,
                        cb.equal(join.get("verein").get("id"), c.getVereinId()));
            }

            // Alter
            if (c.getAlterMin() != null || c.getAlterMax() != null) {

                Expression<Integer> alterExpr = cb.function(
                        "date_part",
                        Integer.class,
                        cb.literal("year"),
                        cb.function("age", Object.class,
                                cb.currentDate(),
                                root.get("geburtsdatum"))
                );

                if (c.getAlterMin() != null) {
                    predicate = cb.and(predicate,
                            cb.greaterThanOrEqualTo(alterExpr, c.getAlterMin()));
                }
                if (c.getAlterMax() != null) {
                    predicate = cb.and(predicate,
                            cb.lessThanOrEqualTo(alterExpr, c.getAlterMax()));
                }
            }

            return predicate;
        };
    }
}