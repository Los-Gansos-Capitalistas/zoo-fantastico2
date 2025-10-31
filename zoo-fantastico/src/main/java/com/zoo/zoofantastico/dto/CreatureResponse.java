package com.zoo.zoofantastico.dto;

public class CreatureResponse {
  private Long id;
  private String name;
  private String species;
  private Double size;
  private Integer dangerLevel;
  private String healthStatus;
  private ZoneResponse zone;

  public CreatureResponse() {}

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getSpecies() { return species; }
  public void setSpecies(String species) { this.species = species; }
  public Double getSize() { return size; }
  public void setSize(Double size) { this.size = size; }
  public Integer getDangerLevel() { return dangerLevel; }
  public void setDangerLevel(Integer dangerLevel) { this.dangerLevel = dangerLevel; }
  public String getHealthStatus() { return healthStatus; }
  public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }
  public ZoneResponse getZone() { return zone; }
  public void setZone(ZoneResponse zone) { this.zone = zone; }
}

