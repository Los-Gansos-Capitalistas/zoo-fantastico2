package com.zoo.zoofantastico.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(
  name = "zone",
  uniqueConstraints = @UniqueConstraint(name = "uk_zone_name", columnNames = "name")
)
public class Zone {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Column(name = "name", length = 255, nullable = false)
  private String name;

  @Column(length = 255)
  private String description;

  private Integer capacity;


// getters/setters
public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCapacity() {
    return capacity;
}
public void setCapacity(Integer capacity) {
    this.capacity = capacity;
}
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
