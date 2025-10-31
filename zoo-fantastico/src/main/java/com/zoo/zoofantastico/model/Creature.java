package com.zoo.zoofantastico.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data @NoArgsConstructor
public class Creature {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String name;
  private String species;
  private double size;
  private int dangerLevel;
  private String healthStatus;

 // src/main/java/com/zoo/zoofantastico/model/Creature.java
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "zone_id", nullable = false)
// import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties({"creatures"}) // evita bucles si Zone tiene lista de creatures
private Zone zone;

}

