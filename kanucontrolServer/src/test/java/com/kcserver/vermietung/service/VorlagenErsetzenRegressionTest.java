package com.kcserver.vermietung.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class VorlagenErsetzenRegressionTest {
    @TestFactory Stream<DynamicTest> originalTextContract() throws IOException {
        // Expected results were captured from the original implementation at fe5c3fcc.
        // Keep these independent fixtures, rather than the vulnerable regexes, in the test suite.
        try (var input = getClass().getResourceAsStream("/vorlagen-ersetzen-regression.json")) {
            assertNotNull(input);
            var fixtures = new ObjectMapper().readTree(input);
            var tests = new ArrayList<DynamicTest>();
            for (int i = 0; i < fixtures.size(); i++) {
                var fixture = fixtures.get(i);
                tests.add(DynamicTest.dynamicTest("Originalfall " + i, () -> {
                    var data = new HashMap<String, String>();
                    data.put("nachname", "Muster$\\");
                    data.put("buchungsnummer", "2026-007");
                    if (!fixture.get("first").isNull()) data.put("vorname", fixture.get("first").asText());
                    if (!fixture.get("previous").isNull()) data.put("fruehere_buchungsnummer", fixture.get("previous").asText());
                    if (fixture.has("error")) {
                        var failure = assertThrows(ResponseStatusException.class,
                                () -> VorlagenModell.ersetzen(fixture.get("input").asText(), data));
                        assertEquals(fixture.get("error").asInt(), failure.getStatusCode().value());
                    } else {
                        assertEquals(fixture.get("expected").asText(),
                                VorlagenModell.ersetzen(fixture.get("input").asText(), data));
                    }
                }));
            }
            return tests.stream();
        }
    }

    @Test void longUnsuccessfulNamePairPreservesWhitespaceWithoutBacktracking() {
        String whitespace = " \t\n".repeat(3900);
        var data = Map.of("vorname", "___", "nachname", "Muster");
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            for (int i = 0; i < 100; i++) {
                assertEquals("Muster" + whitespace + "Ende",
                        VorlagenModell.ersetzen("{{vorname}}" + whitespace + "Ende", data));
            }
        });
    }

    @Test void longSentenceWithoutTokenIsRetainedAndFollowingLegacySentenceRemoved() {
        String retained = "x".repeat(11900) + ".";
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            for (int i = 0; i < 100; i++) {
                assertEquals(retained, VorlagenModell.ersetzen(
                        retained + " Bezug {{fruehere_buchungsnummer}}.", Map.of()));
            }
        });
    }

    @Test void longSentenceContainingMultipleLegacyTokensIsCompletelyRemoved() {
        String input = "x".repeat(11800) + "{{fruehere_buchungsnummer}} und {{fruehere_buchungsnummer}}!";
        assertTimeoutPreemptively(Duration.ofSeconds(5), () ->
                assertEquals("", VorlagenModell.ersetzen(input, Map.of())));
    }

    @Test void textLimitAndUnknownPlaceholdersRemainRejected() {
        assertThrows(ResponseStatusException.class,
                () -> VorlagenModell.ersetzen("x".repeat(12001), Map.of()));
        assertThrows(ResponseStatusException.class,
                () -> VorlagenModell.ersetzen("{{unbekannt}}", Map.of("unbekannt", "x")));
    }
}
