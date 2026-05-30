package com.kcserver.service.postalcode;

public record GeoNamesPostalCodeRow(

        String countryCode,
        String postalCode,
        String city,
        String state,
        String district,
        Double latitude,
        Double longitude

) {
}