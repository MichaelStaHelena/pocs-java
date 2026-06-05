package com.michael.poc.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PersonRequest {

  @NotBlank private String name;

  @NotBlank @Email private String email;

  private String phone;
}
