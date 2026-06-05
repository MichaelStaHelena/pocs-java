package com.michael.poc.service;

import com.michael.poc.entity.Person;
import com.michael.poc.repository.PersonRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonService {

  private final PersonRepository repository;

  public List<Person> findAll() {
    log.debug("listing all people");
    return repository.findAll();
  }

  public Optional<Person> findById(Long id) {
    log.debug("finding person id={}", id);
    return repository.findById(id);
  }

  public Person save(Person person) {
    boolean isNew = person.getId() == null;
    Person saved = repository.save(person);
    if (isNew) {
      log.info("person created id={}", saved.getId());
    } else {
      log.info("person updated id={}", saved.getId());
    }
    return saved;
  }

  public void delete(Long id) {
    log.info("deleting person id={}", id);
    repository.deleteById(id);
  }
}
