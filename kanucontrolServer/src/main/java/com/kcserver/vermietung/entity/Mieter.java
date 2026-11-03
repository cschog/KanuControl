package com.kcserver.vermietung.entity;

import com.kcserver.core.entity.Person;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "mieter", uniqueConstraints = @UniqueConstraint(name = "uk_mieter_person", columnNames = "person_id"))
public class Mieter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @Column(nullable = false)
    private boolean aktiv = true;
}
