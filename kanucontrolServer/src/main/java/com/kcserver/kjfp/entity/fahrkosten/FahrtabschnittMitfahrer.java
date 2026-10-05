package com.kcserver.kjfp.entity.fahrkosten;

import com.kcserver.kjfp.entity.Person;
import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fahrtabschnitt_mitfahrer")
public class FahrtabschnittMitfahrer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Fahrtabschnitt fahrtabschnitt;

    @ManyToOne(optional = false)
    private Person person;
}