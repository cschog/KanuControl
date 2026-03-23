package com.kcserver.support.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;

import com.kcserver.dto.person.PersonSaveDTO;

import com.kcserver.support.builder.MitgliedSaveDTOBuilder;
import com.kcserver.support.builder.PersonSaveDTOBuilder;
import com.kcserver.support.web.AbstractApiTestFactory;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


import java.util.List;
import java.util.function.Consumer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * FINAL: Builder-first Test Factory
 */
public class PersonTestFactory extends AbstractApiTestFactory {

    public PersonTestFactory(
            MockMvc mockMvc,
            ObjectMapper objectMapper
    ) {
        super(mockMvc, objectMapper, null);
    }

    public Long create() throws Exception {
        return create(b -> {});
    }

    public Long create(Consumer<PersonSaveDTOBuilder> customizer) throws Exception {

        PersonSaveDTOBuilder builder = PersonSaveDTOBuilder.aDTO();
        customizer.accept(builder);

        return performCreate(builder.build());
    }

    public Long createWithVerein(
            Long vereinId,
            Consumer<PersonSaveDTOBuilder> customizer
    ) throws Exception {

        PersonSaveDTOBuilder builder = PersonSaveDTOBuilder.aDTO();
        customizer.accept(builder);

        PersonSaveDTO dto = builder.build();

        dto.setMitgliedschaften(List.of(
                MitgliedSaveDTOBuilder.aDTO()
                        .withVerein(vereinId)
                        .build()
        ));

        return performCreate(dto);
    }

    private Long performCreate(PersonSaveDTO dto) throws Exception {

        MvcResult result =
                mockMvc.perform(
                                post("/api/person")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto))
                        )
                        .andExpect(status().isCreated())
                        .andReturn();

        return objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("id")
                .asLong();
    }
}