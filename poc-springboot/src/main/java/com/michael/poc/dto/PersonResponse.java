package com.michael.poc.dto;

import lombok.Data;

@Data
public class PersonResponse {

  private Long id;
  private String name;
  private String email;
  private String phone;
}
