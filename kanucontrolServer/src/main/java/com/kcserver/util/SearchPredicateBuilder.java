package com.kcserver.util;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.List;

public class SearchPredicateBuilder {

    public static <T> void applyNameSearch(
            List<Predicate> predicates,
            CriteriaBuilder cb,
            Root<T> root,
            String search,
            String field1,   // z.B. "name"
            String field2    // z.B. "vorname"
    ) {
        if (search == null || search.isBlank()) return;

        String[] parts = search.toLowerCase().trim().split("\\s+");

        if (parts.length == 1) {
            String s = parts[0];

            predicates.add(cb.or(
                    cb.like(cb.lower(root.get(field1)), "%" + s + "%"),
                    cb.like(cb.lower(root.get(field2)), "%" + s + "%")
            ));
        }

        if (parts.length >= 2) {
            String a = parts[0];
            String b = parts[1];

            predicates.add(cb.or(
                    cb.and(
                            cb.like(cb.lower(root.get(field2)), "%" + a + "%"),
                            cb.like(cb.lower(root.get(field1)), "%" + b + "%")
                    ),
                    cb.and(
                            cb.like(cb.lower(root.get(field1)), "%" + a + "%"),
                            cb.like(cb.lower(root.get(field2)), "%" + b + "%")
                    )
            ));
        }
    }
}