package com.tab.utrabajo.api.architecture

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices
import org.junit.jupiter.api.Test

class DatabaseIsolationArchitectureTests {
    private val productionClasses: JavaClasses = ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tab.utrabajo.api")

    @Test
    fun `controllers cannot access JDBC or SQL APIs`() {
        noClasses()
            .that().resideInAPackage("..controller..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "org.springframework.jdbc..",
                "java.sql..",
                "javax.sql..",
            )
            .because("ADR-002 requires controllers to cross the service boundary before persistence")
            .check(productionClasses)
    }

    @Test
    fun `persistence APIs stay inside approved boundary packages`() {
        noClasses()
            .that().resideOutsideOfPackages(
                "..service..",
                "..auth..",
                "..config..",
            )
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "org.springframework.jdbc..",
                "java.sql..",
                "javax.sql..",
            )
            .because("database access must remain behind an explicit application boundary")
            .check(productionClasses)
    }

    @Test
    fun `top level application packages remain free of cycles`() {
        slices()
            .matching("com.tab.utrabajo.api.(*)..")
            .should().beFreeOfCycles()
            .because("cyclic packages prevent bounded contexts from being isolated later")
            .check(productionClasses)
    }
}
