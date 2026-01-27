package com.kcserver.testsupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public final class ResultMatchersExt {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private ResultMatchersExt() {
    }

    public static ResultMatcher hasExactlyOneHauptverein() {
        return (MvcResult result) -> {
            String json = result.getResponse().getContentAsString();
            JsonNode root = objectMapper.readTree(json);

            JsonNode mitgliedschaften = root.get("mitgliedschaften");

            if (mitgliedschaften == null || !mitgliedschaften.isArray()) {
                fail("Expected 'mitgliedschaften' to be an array");
            }

            long count =
                    mitgliedschaften
                            .findValues("hauptVerein")
                            .stream()
                            .filter(JsonNode::asBoolean)
                            .count();

            assertEquals(
                    1,
                    count,
                    "Expected exactly one hauptVerein=true, but found " + count
            );
        };
    }
}