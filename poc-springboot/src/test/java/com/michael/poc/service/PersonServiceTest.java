package com.michael.poc.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.michael.poc.entity.Person;
import com.michael.poc.repository.PersonRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

  @Mock private PersonRepository repository;

  @InjectMocks private PersonService service;

  private Person person;

  @BeforeEach
  void setUp() {
    person = new Person(1L, "Ana", "ana@mail.com", "999");
  }

  @Test
  void findAll_returnsAllPersons() {
    when(repository.findAll()).thenReturn(List.of(person));

    List<Person> result = service.findAll();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getName()).isEqualTo("Ana");

    verify(repository, times(1)).findAll();
  }

  @Test
  void findById_whenPersonExists_returnsPerson() {
    when(repository.findById(1L)).thenReturn(Optional.of(person));

    Optional<Person> result = service.findById(1L);

    assertThat(result).isPresent();
    assertThat(result.get().getEmail()).isEqualTo("ana@mail.com");
  }

  @Test
  void findById_whenPersonDoesNotExist_returnsEmpty() {
    when(repository.findById(99L)).thenReturn(Optional.empty());

    Optional<Person> result = service.findById(99L);

    assertThat(result).isEmpty();
  }

  @Test
  void save_persistsAndReturnsPerson() {
    Person newPerson = new Person(null, "Bob", "bob@mail.com", "888");
    Person saved = new Person(2L, "Bob", "bob@mail.com", "888");

    when(repository.save(newPerson)).thenReturn(saved);

    Person result = service.save(newPerson);

    assertThat(result.getId()).isEqualTo(2L);
    assertThat(result.getName()).isEqualTo("Bob");
  }

  @Test
  void delete_delegatesToRepositoryDeleteById() {
    // Act
    service.delete(1L);

    verify(repository, times(1)).deleteById(1L);
  }
}
