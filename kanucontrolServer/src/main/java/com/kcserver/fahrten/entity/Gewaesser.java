package com.kcserver.fahrten.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "gewaesser", uniqueConstraints = @UniqueConstraint(name = "uk_gewaesser_name", columnNames = "name"))
@Getter @Setter
public class Gewaesser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 160)
    private String name;
}
