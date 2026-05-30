package com.kcserver.entity;

import com.kcserver.enumtype.CountryCode;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        schema = "public",
        name = "postal_code",

        indexes = {
                @Index(
                        name = "idx_postal_country_plz",
                        columnList = "country_code, postal_code"
                ),
                @Index(
                        name = "idx_postal_country_city",
                        columnList = "country_code, city"
                )
        },

        uniqueConstraints = {

                @UniqueConstraint(
                        name = "uk_postal_country_plz_city",
                        columnNames = {
                                "country_code",
                                "postal_code",
                                "city"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostalCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ISO-3166-1 alpha-2
     * DE, AT, CH, BE ...
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "country_code", nullable = false, length = 2)
    private CountryCode countryCode;

    /**
     * International:
     * DE -> 52064
     * UK -> SW1A 1AA
     * CA -> K1A 0B1
     */
    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    /**
     * Stadt / Ort
     */
    @Column(nullable = false, length = 255)
    private String city;

    /**
     * Bundesland / Province / Region
     */
    @Column(length = 255)
    private String state;

    /**
     * Landkreis / County / District
     */
    @Column(length = 255)
    private String district;

    /**
     * Geo-Koordinaten
     */
    private Double latitude;

    private Double longitude;

    /**
     * Datenquelle
     */
    @Column(length = 50)
    private String source;

    /**
     * Importzeitpunkt
     */
    @Column(name = "imported_at")
    private LocalDateTime importedAt;
}