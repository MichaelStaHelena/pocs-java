package com.michael.poc.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.michael.poc.dto.PersonRequest;
import com.michael.poc.dto.PersonResponse;
import com.michael.poc.entity.Person;
import com.michael.poc.mapper.PersonMapper;
import com.michael.poc.service.PersonService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(PersonController.class)
@AutoConfigureJson
class PersonControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private JsonMapper objectMapper;
  @MockitoBean private PersonService service;
  @MockitoBean private PersonMapper mapper;

  private Person person;
  private PersonResponse response;

  @BeforeEach
  void setUp() {
    person = new Person(1L, "Ana", "ana@mail.com", "999");

    response = new PersonResponse();
    response.setId(1L);
    response.setName("Ana");
    response.setEmail("ana@mail.com");
    response.setPhone("999");
  }

  @Test
  void listAll_returns200WithListOfPersons() throws Exception {
    when(service.findAll()).thenReturn(List.of(person));
    when(mapper.toResponse(person)).thenReturn(response);

    mockMvc
        .perform(get("/people"))
        .andExpect(status().isOk())
        // jsonPath navigates the JSON response — $[0].name means first element's name field
        .andExpect(jsonPath("$[0].name").value("Ana"))
        .andExpect(jsonPath("$[0].email").value("ana@mail.com"));
  }

  @Test
  void getById_whenPersonExists_returns200() throws Exception {
    when(service.findById(1L)).thenReturn(Optional.of(person));
    when(mapper.toResponse(person)).thenReturn(response);

    mockMvc
        .perform(get("/people/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Ana"));
  }

  @Test
  void getById_whenPersonDoesNotExist_returns404() throws Exception {
    when(service.findById(99L)).thenReturn(Optional.empty());

    mockMvc.perform(get("/people/99")).andExpect(status().isNotFound());
  }

  @Test
  void create_withValidRequest_returns201() throws Exception {
    PersonRequest request = new PersonRequest();
    request.setName("Ana");
    request.setEmail("ana@mail.com");
    request.setPhone("999");

    when(mapper.toEntity(any(PersonRequest.class))).thenReturn(person);
    when(service.save(any(Person.class))).thenReturn(person);
    when(mapper.toResponse(person)).thenReturn(response);

    mockMvc
        .perform(
            post("/people")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Ana"));
  }

  @Test
  void create_withMissingName_returns400() throws Exception {
    PersonRequest request = new PersonRequest();
    // name is intentionally blank — @NotBlank should reject this
    request.setName("");
    request.setEmail("ana@mail.com");

    mockMvc
        .perform(
            post("/people")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void create_withInvalidEmail_returns400() throws Exception {
    PersonRequest request = new PersonRequest();
    request.setName("Ana");
    request.setEmail("not-an-email");

    mockMvc
        .perform(
            post("/people")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void update_whenPersonExists_returns200() throws Exception {
    PersonRequest request = new PersonRequest();
    request.setName("Ana Lima");
    request.setEmail("ana@mail.com");

    when(service.findById(1L)).thenReturn(Optional.of(person));
    when(service.save(any(Person.class))).thenReturn(person);
    when(mapper.toResponse(person)).thenReturn(response);

    mockMvc
        .perform(
            put("/people/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    verify(mapper).updateEntity(any(PersonRequest.class), eq(person));
  }

  @Test
  void update_whenPersonDoesNotExist_returns404() throws Exception {
    PersonRequest request = new PersonRequest();
    request.setName("Ana");
    request.setEmail("ana@mail.com");

    when(service.findById(99L)).thenReturn(Optional.empty());

    mockMvc
        .perform(
            put("/people/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  @Test
  void delete_returns204() throws Exception {
    mockMvc.perform(delete("/people/1")).andExpect(status().isNoContent());

    verify(service).delete(1L);
  }
}
