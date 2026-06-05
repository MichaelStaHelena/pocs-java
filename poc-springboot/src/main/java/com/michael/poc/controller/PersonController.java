package com.michael.poc.controller;

import com.michael.poc.dto.PersonRequest;
import com.michael.poc.dto.PersonResponse;
import com.michael.poc.mapper.PersonMapper;
import com.michael.poc.service.PersonService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/people")
@Tag(name = "People", description = "CRUD for Person resource")
public class PersonController {

  private final PersonService service;
  private final PersonMapper mapper;

  public PersonController(PersonService service, PersonMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @GetMapping
  public List<PersonResponse> listAll() {
    return service.findAll().stream().map(mapper::toResponse).toList();
  }

  @GetMapping("/{id}")
  public ResponseEntity<PersonResponse> getById(@PathVariable Long id) {
    return service
        .findById(id)
        .map(mapper::toResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public PersonResponse create(@Valid @RequestBody PersonRequest request) {
    return mapper.toResponse(service.save(mapper.toEntity(request)));
  }

  @PutMapping("/{id}")
  public ResponseEntity<PersonResponse> update(
      @PathVariable Long id, @Valid @RequestBody PersonRequest request) {
    return service
        .findById(id)
        .map(
            existing -> {
              mapper.updateEntity(request, existing);
              return ResponseEntity.ok(mapper.toResponse(service.save(existing)));
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
