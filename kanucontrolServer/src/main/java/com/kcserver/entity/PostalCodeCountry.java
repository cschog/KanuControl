package com.kcserver.entity;

import com.kcserver.enumtype.CountryCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        schema = "public",
        name = "postal_code_country"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostalCodeCountry {

    @Id
    @Column(name = "country_code")
    @Enumerated(EnumType.STRING)
    private CountryCode countryCode;

    private boolean enabled;

    @Column(name = "auto_import")
    private boolean autoImport;

    @Column(name = "last_import")
    private LocalDateTime lastImport;

    @Column(name = "next_import")
    private LocalDateTime nextImport;
}
