package com.kcserver.persistence.specification;

import com.kcserver.dto.teilnehmer.TeilnehmerSearchCriteria;
import com.kcserver.entity.Mitglied;
import com.kcserver.entity.Person;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Verein;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import static com.kcserver.util.StringUtils.hasText;

public class TeilnehmerSpecification {

    public static Specification<Teilnehmer> byCriteria(TeilnehmerSearchCriteria c) {
        return (root, query, cb) -> {

            if (query != null && !Long.class.equals(query.getResultType())) {
                query.distinct(true);
            }

            Predicate predicate = cb.conjunction();

            // ⭐ JOIN auf Person
            Join<Teilnehmer, Person> person = root.join("person", JoinType.INNER);

            /* =========================================================
               🔍 GENERIC SEARCH (Name + Vorname)
               ========================================================= */

            if (hasText(c.getSearch())) {

                String search = c.getSearch().toLowerCase().trim();
                String[] parts = search.split("\\s+");

                if (parts.length == 1) {
                    String s = parts[0];

                    Predicate nameLike = cb.like(cb.lower(person.get("name")), "%" + s + "%");
                    Predicate vornameLike = cb.like(cb.lower(person.get("vorname")), "%" + s + "%");

                    predicate = cb.and(predicate, cb.or(nameLike, vornameLike));
                }

                if (parts.length >= 2) {
                    String a = parts[0];
                    String b = parts[1];

                    Predicate vornameName = cb.and(
                            cb.like(cb.lower(person.get("vorname")), "%" + a + "%"),
                            cb.like(cb.lower(person.get("name")), "%" + b + "%")
                    );

                    Predicate nameVorname = cb.and(
                            cb.like(cb.lower(person.get("name")), "%" + a + "%"),
                            cb.like(cb.lower(person.get("vorname")), "%" + b + "%")
                    );

                    predicate = cb.and(predicate, cb.or(vornameName, nameVorname));
                }
            }

            /* =========================================================
               Verein Filter (optional)
               ========================================================= */

            if (hasText(c.getVerein())) {

                Join<Person, Mitglied> mitglied =
                        person.join("mitgliedschaften", JoinType.LEFT);

                Join<Mitglied, Verein> verein =
                        mitglied.join("verein", JoinType.LEFT);

                predicate = cb.and(
                        predicate,
                        cb.and(
                                cb.isTrue(mitglied.get("hauptVerein")),
                                cb.like(
                                        cb.lower(verein.get("abk")),
                                        "%" + c.getVerein().toLowerCase() + "%"
                                )
                        )
                );
            }

            return predicate;
        };
    }
}