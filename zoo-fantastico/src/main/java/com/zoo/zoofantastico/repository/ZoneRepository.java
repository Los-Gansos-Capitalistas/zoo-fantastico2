package com.zoo.zoofantastico.repository;

import com.zoo.zoofantastico.dto.ZoneSummaryDTO;
import com.zoo.zoofantastico.model.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ZoneRepository extends JpaRepository<Zone, Long> {

    // Para idempotencia/validación por nombre (ignorando mayúsculas/minúsculas)
    Optional<Zone> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);

    // Resumen de zonas con conteo de criaturas
    @Query("""
           SELECT new com.zoo.zoofantastico.dto.ZoneSummaryDTO(
              z.id, z.name, z.description, z.capacity, COUNT(c.id)
           )
           FROM Zone z
           LEFT JOIN Creature c ON c.zone = z
           GROUP BY z.id, z.name, z.description, z.capacity
           ORDER BY z.name ASC
           """)
    List<ZoneSummaryDTO> findZoneSummaries();
}

