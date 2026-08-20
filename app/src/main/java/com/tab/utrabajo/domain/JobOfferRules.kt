package com.tab.utrabajo.domain

data class JobOfferDraft(
    val title: String,
    val description: String,
    val requirements: String,
    val salary: String,
    val location: String
)

data class ValidatedJobOffer(
    val title: String,
    val description: String,
    val requirements: List<String>,
    val salary: String,
    val location: String
)

sealed interface JobOfferValidationResult {
    data class Valid(val offer: ValidatedJobOffer) : JobOfferValidationResult
    data class Invalid(val missingFields: Set<Field>) : JobOfferValidationResult

    enum class Field {
        TITLE,
        DESCRIPTION,
        REQUIREMENTS,
        SALARY,
        LOCATION
    }
}

object JobOfferRules {
    fun validate(draft: JobOfferDraft): JobOfferValidationResult {
        val normalizedRequirements = draft.requirements
            .split(',')
            .map(String::trim)
            .filter(String::isNotEmpty)
            .distinctBy { it.lowercase() }

        val missingFields = buildSet {
            if (draft.title.isBlank()) add(JobOfferValidationResult.Field.TITLE)
            if (draft.description.isBlank()) add(JobOfferValidationResult.Field.DESCRIPTION)
            if (normalizedRequirements.isEmpty()) add(JobOfferValidationResult.Field.REQUIREMENTS)
            if (draft.salary.isBlank()) add(JobOfferValidationResult.Field.SALARY)
            if (draft.location.isBlank()) add(JobOfferValidationResult.Field.LOCATION)
        }

        if (missingFields.isNotEmpty()) {
            return JobOfferValidationResult.Invalid(missingFields)
        }

        return JobOfferValidationResult.Valid(
            ValidatedJobOffer(
                title = draft.title.trim(),
                description = draft.description.trim(),
                requirements = normalizedRequirements,
                salary = draft.salary.trim(),
                location = draft.location.trim()
            )
        )
    }
}
