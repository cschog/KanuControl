package com.kcserver.entity;


import jakarta.persistence.*;
import jakarta.persistence.GenerationType;

@Entity
@Table(name = "postal_code")
public class PostalCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @Column(nullable = false)
    private String city;
}