package com.tab.utrabajo.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JobOfferRulesTest {
    @Test
    fun `rejects an offer when required domain data is absent`() {
        val result = JobOfferRules.validate(
            JobOfferDraft(
                title = " ",
                description = "Descripción válida",
                requirements = " , ",
                salary = "2.000.000",
                location = ""
            )
        )

        assertTrue(result is JobOfferValidationResult.Invalid)
        assertEquals(
            setOf(
                JobOfferValidationResult.Field.TITLE,
                JobOfferValidationResult.Field.REQUIREMENTS,
                JobOfferValidationResult.Field.LOCATION
            ),
            (result as JobOfferValidationResult.Invalid).missingFields
        )
    }

    @Test
    fun `normalizes whitespace and removes duplicate requirements`() {
        val result = JobOfferRules.validate(
            JobOfferDraft(
                title = "  Desarrollador Android  ",
                description = "  Desarrollo de la aplicación  ",
                requirements = "Kotlin, Compose, kotlin,  ",
                salary = " 3.000.000 ",
                location = " Bogotá "
            )
        )

        assertTrue(result is JobOfferValidationResult.Valid)
        val offer = (result as JobOfferValidationResult.Valid).offer
        assertEquals("Desarrollador Android", offer.title)
        assertEquals("Desarrollo de la aplicación", offer.description)
        assertEquals(listOf("Kotlin", "Compose"), offer.requirements)
        assertEquals("3.000.000", offer.salary)
        assertEquals("Bogotá", offer.location)
    }
}
