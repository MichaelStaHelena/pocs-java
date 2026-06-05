package com.michael.poc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.michael.poc.dto.PersonRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class PersonIntegrationTest {

  @Container @ServiceConnection
  static final PostgreSQLContainer POSTGRES =
      new PostgreSQLContainer("postgres:16-alpine").withReuse(true);

  @Autowired private MockMvc mockMvc;
  @Autowired private JsonMapper objectMapper;

  @Test
  void fullCrudFlow() throws Exception {
    PersonRequest create = new PersonRequest();
    create.setName("Ana");
    create.setEmail("ana@mail.com");
    create.setPhone("999");

    String createResponse =
        mockMvc
            .perform(
                post("/people")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(create)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.name").value("Ana"))
            .andReturn()
            .getResponse()
            .getContentAsString();

    long id = objectMapper.readTree(createResponse).get("id").asLong();

    mockMvc
        .perform(get("/people/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("ana@mail.com"));

    mockMvc
        .perform(get("/people"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.id == " + id + ")].name").value("Ana"));

    PersonRequest update = new PersonRequest();
    update.setName("Ana Lima");
    update.setEmail("ana@mail.com");
    update.setPhone("000");

    mockMvc
        .perform(
            put("/people/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Ana Lima"))
        .andExpect(jsonPath("$.phone").value("000"));

    mockMvc.perform(delete("/people/{id}", id)).andExpect(status().isNoContent());

    mockMvc.perform(get("/people/{id}", id)).andExpect(status().isNotFound());
  }

  @Test
  void create_withInvalidEmail_returns400() throws Exception {
    PersonRequest bad = new PersonRequest();
    bad.setName("Bob");
    bad.setEmail("not-an-email");

    mockMvc
        .perform(
            post("/people")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bad)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.errors").isArray());
  }
}
