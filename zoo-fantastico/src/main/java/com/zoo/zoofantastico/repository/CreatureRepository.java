package com.zoo.zoofantastico.repository;
import com.zoo.zoofantastico.model.Creature;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreatureRepository extends JpaRepository<Creature, Long> {
    long countByZoneId(Long zoneId);
}
