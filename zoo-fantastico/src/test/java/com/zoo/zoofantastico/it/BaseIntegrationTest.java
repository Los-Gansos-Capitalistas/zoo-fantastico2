package com.zoo.zoofantastico.it;

import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Clase base para todos los tests de integración.
 * - Usa perfil "test" (H2 en memoria + Flyway)
 * - NO reemplaza el datasource (dejamos el que provee application-test.yml)
 * - Transaccional: cada test se ejecuta y se revierte al terminar
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public abstract class BaseIntegrationTest {
    // Config común
}
