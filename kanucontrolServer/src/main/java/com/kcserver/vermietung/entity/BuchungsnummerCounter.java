package com.kcserver.vermietung.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "buchungsnummer_counter")
public class BuchungsnummerCounter {

    @Id
    @Column(name = "jahr")
    @EqualsAndHashCode.Include
    private Integer jahr;

    @Column(name = "letzte_nummer", nullable = false)
    private Integer letzteNummer = 0;
}