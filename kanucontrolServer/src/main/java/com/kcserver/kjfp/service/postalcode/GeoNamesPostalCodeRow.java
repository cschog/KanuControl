package com.kcserver.kjfp.service.postalcode;

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