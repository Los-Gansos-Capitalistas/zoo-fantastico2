package com.zoo.zoofantastico.validation;

import com.zoo.zoofantastico.dto.request.CreateCreatureRequest;
import com.zoo.zoofantastico.dto.request.UpdateCreatureRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CreatureRequestValidationTest {

  private static Validator validator;

  @BeforeAll
  static void init() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  // ---------- Helpers ----------
  private CreateCreatureRequest validCreate() {
    CreateCreatureRequest req = new CreateCreatureRequest();
    req.setName("Fénix");
    req.setSpecies("Ave");
    req.setSize(0.5);         // >= 0.1
    req.setDangerLevel(3);    // entre 1..5
    req.setHealthStatus("stable");
    req.setZoneId(1L);
    return req;
  }

  private UpdateCreatureRequest emptyUpdate() {
    return new UpdateCreatureRequest(); // todos los campos opcionales, sin setear
  }

  @Test
  void createCreatureRequest_invalid_whenSizeNegative_orDangerOutOfRange() {
    CreateCreatureRequest req = new CreateCreatureRequest();
    req.setName("");
    req.setSpecies("");
    req.setSize(-1.0);   // inválido
    req.setDangerLevel(6); // inválido (> Max 5)
    req.setHealthStatus("");
    req.setZoneId(1L);
    req.setSize(0.0);    // también inválido con @DecimalMin("0.1")

    Set<ConstraintViolation<CreateCreatureRequest>> v = validator.validate(req);
    assertFalse(v.isEmpty());
    assertTrue(v.stream().anyMatch(e -> e.getPropertyPath().toString().equals("size")));
    assertTrue(v.stream().anyMatch(e -> e.getPropertyPath().toString().equals("dangerLevel")));
  }

  @Test
  void createCreatureRequest_invalid_whenNameBlank() {
    CreateCreatureRequest req = validCreate();
    req.setName(" "); // inválido por @NotBlank

    Set<ConstraintViolation<CreateCreatureRequest>> v = validator.validate(req);
    assertFalse(v.isEmpty());
    assertTrue(v.stream().anyMatch(e -> e.getPropertyPath().toString().equals("name")));
  }

  @Test
  void createCreatureRequest_invalid_whenSpeciesBlank() {
    CreateCreatureRequest req = validCreate();
    req.setSpecies("  "); // inválido por @NotBlank

    Set<ConstraintViolation<CreateCreatureRequest>> v = validator.validate(req);
    assertFalse(v.isEmpty());
    assertTrue(v.stream().anyMatch(e -> e.getPropertyPath().toString().equals("species")));
  }

  @Test
  void createCreatureRequest_invalid_whenHealthStatusBlank() {
    CreateCreatureRequest req = validCreate();
    req.setHealthStatus(" "); // inválido por @NotBlank

    Set<ConstraintViolation<CreateCreatureRequest>> v = validator.validate(req);
    assertFalse(v.isEmpty());
    assertTrue(v.stream().anyMatch(e -> e.getPropertyPath().toString().equals("healthStatus")));
  }

  @Test
  void createCreatureRequest_invalid_whenZoneIdNull() {
    CreateCreatureRequest req = validCreate();
    req.setZoneId(null); // inválido por @NotNull

    Set<ConstraintViolation<CreateCreatureRequest>> v = validator.validate(req);
    assertFalse(v.isEmpty());
    assertTrue(v.stream().anyMatch(e -> e.getPropertyPath().toString().equals("zoneId")));
  }

  @Test
  void createCreatureRequest_valid_whenBoundsAreOk() {
    CreateCreatureRequest req = new CreateCreatureRequest();
    req.setName("Fénix");
    req.setSpecies("Ave");
    req.setSize(0.1);      // límite con @DecimalMin("0.1")
    req.setDangerLevel(1); // límite inferior válido con @Min(1)
    req.setHealthStatus("stable");
    req.setZoneId(1L);

    Set<ConstraintViolation<CreateCreatureRequest>> v = validator.validate(req);
    assertTrue(v.isEmpty());
  }


  @Test
  void updateCreatureRequest_allOptionalButValidatedWhenPresent() {
    UpdateCreatureRequest req = new UpdateCreatureRequest();
    req.setSize(-0.1);     // inválido si se envía (DecimalMin("0.1")/Positive)
    req.setDangerLevel(0); // inválido si se envía (Min(1))

    Set<ConstraintViolation<UpdateCreatureRequest>> v = validator.validate(req);
    assertFalse(v.isEmpty());
    assertTrue(v.stream().anyMatch(e -> e.getPropertyPath().toString().equals("size")));
    assertTrue(v.stream().anyMatch(e -> e.getPropertyPath().toString().equals("dangerLevel")));
  }

  @Test
  void updateCreatureRequest_invalid_whenDangerAboveMax() {
    UpdateCreatureRequest req = new UpdateCreatureRequest();
    req.setDangerLevel(6); // > Maximo5

    Set<ConstraintViolation<UpdateCreatureRequest>> v = validator.validate(req);
    assertFalse(v.isEmpty());
    assertTrue(v.stream().anyMatch(e -> e.getPropertyPath().toString().equals("dangerLevel")));
  }

  @Test
  void updateCreatureRequest_invalid_whenNameBlankProvided() {
    UpdateCreatureRequest req = new UpdateCreatureRequest();
    req.setName("   "); // @NotBlank o @Pattern para evitar solo espacios

    Set<ConstraintViolation<UpdateCreatureRequest>> v = validator.validate(req);
    assertFalse(v.isEmpty(), "Nombre en blanco debería invalidarse cuando se provee");
    assertTrue(v.stream().anyMatch(e -> e.getPropertyPath().toString().equals("name")));
  }

  @Test
  void updateCreatureRequest_valid_whenEmptyPatch() {
    // Patch vacío es válido si todos los campos son opcionales
    Set<ConstraintViolation<UpdateCreatureRequest>> v = validator.validate(emptyUpdate());
    assertTrue(v.isEmpty());
  }
}
