package com.ejemplo.tienda;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * The architecture as a test. If someone breaks a module boundary, the build
 * fails here, the same way it fails when a unit test fails.
 */
class ModularityTests {

    private static final ApplicationModules modules =
            ApplicationModules.of(TiendaApplication.class);

    @Test
    void printsTheDetectedModules() {
        modules.forEach(System.out::println);
    }

    @Test
    void modulesRespectTheirBoundaries() {
        modules.verify();
    }

    @Test
    void writesTheModuleDocumentation() {
        // Writes PlantUML diagrams and one canvas per module to target/spring-modulith-docs
        new Documenter(modules).writeDocumentation();
    }
}
