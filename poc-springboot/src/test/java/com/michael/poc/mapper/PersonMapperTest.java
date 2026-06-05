package com.michael.poc.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.michael.poc.dto.PersonRequest;
import com.michael.poc.dto.PersonResponse;
import com.michael.poc.entity.Person;
import org.junit.jupiter.api.Test;

class PersonMapperTest {

  private final PersonMapper mapper = new PersonMapperImpl();

  @Test
  void toEntity_mapsAllFieldsExceptId() {
    PersonRequest request = new PersonRequest();
    request.setName("Ana");
    request.setEmail("ana@mail.com");
    request.setPhone("999");

    Person entity = mapper.toEntity(request);

    assertThat(entity.getName()).isEqualTo("Ana");
    assertThat(entity.getEmail()).isEqualTo("ana@mail.com");
    assertThat(entity.getPhone()).isEqualTo("999");

    assertThat(entity.getId()).isNull();
  }

  @Test
  void toResponse_mapsAllFields() {
    Person entity = new Person(1L, "Ana", "ana@mail.com", "999");

    PersonResponse response = mapper.toResponse(entity);

    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getName()).isEqualTo("Ana");
    assertThat(response.getEmail()).isEqualTo("ana@mail.com");
    assertThat(response.getPhone()).isEqualTo("999");
  }

  @Test
  void updateEntity_overwritesFieldsButPreservesId() {
    Person existing = new Person(1L, "Old Name", "old@mail.com", "000");

    PersonRequest request = new PersonRequest();
    request.setName("New Name");
    request.setEmail("new@mail.com");
    request.setPhone("111");

    mapper.updateEntity(request, existing);

    assertThat(existing.getName()).isEqualTo("New Name");
    assertThat(existing.getEmail()).isEqualTo("new@mail.com");
    assertThat(existing.getPhone()).isEqualTo("111");

    assertThat(existing.getId()).isEqualTo(1L);
  }
}
