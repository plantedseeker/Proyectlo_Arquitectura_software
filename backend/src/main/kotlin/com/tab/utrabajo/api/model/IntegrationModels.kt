package com.tab.utrabajo.api.model

import java.util.UUID

data class JobConversationContextResponse(
    val jobId: UUID,
    val companyId: UUID,
)
