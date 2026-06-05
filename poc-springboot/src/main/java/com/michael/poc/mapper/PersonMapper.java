package com.michael.poc.mapper;

import com.michael.poc.dto.PersonRequest;
import com.michael.poc.dto.PersonResponse;
import com.michael.poc.entity.Person;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PersonMapper {

  Person toEntity(PersonRequest request);

  PersonResponse toResponse(Person person);

  void updateEntity(PersonRequest request, @MappingTarget Person person);
}
