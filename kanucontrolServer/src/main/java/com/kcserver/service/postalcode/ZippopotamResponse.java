package com.kcserver.service.postalcode;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ZippopotamResponse(

        @JsonProperty("post code")
        String postCode,

        String country,

        @JsonProperty("country abbreviation")
        String countryAbbreviation,

        List<Place> places

) {

    public record Place(

            @JsonProperty("place name")
            String placeName,

            String state,

            @JsonProperty("state abbreviation")
            String stateAbbreviation,

            String latitude,

            String longitude

    ) {
    }
}