package com.tab.utrabajo.ui.company

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.tab.utrabajo.R
import com.tab.utrabajo.data.UTrabajoRepository
import com.tab.utrabajo.presentation.components.MainTab
import com.tab.utrabajo.presentation.components.UTrabajoBottomBar
import com.tab.utrabajo.presentation.components.UTrabajoPageHeader
import com.tab.utrabajo.presentation.components.UTrabajoEmptyState
import com.tab.utrabajo.presentation.components.UTrabajoErrorBanner
import com.tab.utrabajo.presentation.components.UTrabajoLoadingState

// Data class para ofertas de trabajo (ahora incluye description)
data class JobOffer(
    val id: String = "",
    val title: String = "",
    val salary: String = "",
    val description: String = ""
)

@Composable
fun CompanyHomeScreen(navController: NavHostController) {
    var jobOffers by remember { mutableStateOf<List<JobOffer>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingJob by remember { mutableStateOf<JobOffer?>(null) }

    val context = LocalContext.current
    val repo = UTrabajoRepository.getInstance()

    // Header
    val headerTitle = stringResource(R.string.company_header_title)
    val headerSubtitle = stringResource(R.string.company_header_subtitle)

    // Actualización periódica desde la API para empleos activos
    DisposableEffect(Unit) {
        val listener = repo.listenToActiveJobOffers(onUpdate = { list ->
            // Mapear cada documento (mapa) a JobOffer
            val companyId = repo.getCurrentUser()?.uid
            val mapped = list.filter { it["empresaId"]?.toString() == companyId }.mapIndexed { index, item ->
                val idVal = item["id"] as? String
                val titleVal = item["titulo"] as? String ?: item["title"] as? String
                val salaryVal = item["salario"]?.toString() ?: item["salary"]?.toString()
                val descriptionVal = item["descripcion"] as? String ?: item["description"] as? String ?: ""
                JobOffer(
                    id = idVal ?: (item["documentId"] as? String ?: index.toString()),
                    title = titleVal ?: "",
                    salary = salaryVal ?: "",
                    description = descriptionVal
                )
            }
            jobOffers = mapped
            isLoading = false
            loadError = null
        }, onError = { err ->
            jobOffers = emptyList()
            isLoading = false
            loadError = err
        })

        onDispose {
            try {
                listener.remove()
            } catch (_: Exception) { /* ignore */ }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar oferta")
            }
        },
        bottomBar = {
            UTrabajoBottomBar(
                navController = navController,
                selected = MainTab.Home,
                companyMode = true
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            UTrabajoPageHeader(
                title = "Tus ofertas",
                subtitle = "Administra las oportunidades publicadas por tu empresa.",
                eyebrow = "Panel de empresa"
            )

            when {
                isLoading -> UTrabajoLoadingState(modifier = Modifier.fillMaxSize())
                loadError != null -> UTrabajoErrorBanner(
                    message = loadError.orEmpty(),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                jobOffers.isEmpty() -> UTrabajoEmptyState(
                    title = "Aún no tienes ofertas publicadas",
                    subtitle = "Usa el botón + para crear tu primera oportunidad.",
                    modifier = Modifier.fillMaxWidth()
                )
                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(jobOffers, key = { it.id }) { offer ->
                        JobOfferListItem(
                            offer = offer,
                            onEdit = { editingJob = offer },
                            onDelete = {
                                if (offer.id.isNotBlank()) {
                                    repo.deleteJobOffer(
                                        offer.id,
                                        onSuccess = {
                                            Toast.makeText(context, "Oferta eliminada", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { err ->
                                            Toast.makeText(context, "Error al eliminar: $err", Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Diálogo para agregar/editar oferta
    if (showAddDialog || editingJob != null) {
        JobOfferDialog(
            jobOffer = editingJob,
            onDismiss = {
                showAddDialog = false
                editingJob = null
            },
            onSave = { jobOffer ->
                val currentUser = repo.getCurrentUser()
                val companyId = currentUser?.uid ?: ""
                if (editingJob != null) {
                    // Actualizar oferta existente mediante la API
                    if (jobOffer.id.isNotBlank()) {
                        // Nota: updateJobOffer del repo actualiza título y salario (dependiendo de tu repo).
                        repo.updateJobOffer(jobOffer.id, title = jobOffer.title, salary = jobOffer.salary, onSuccess = {
                            Toast.makeText(context, "Oferta actualizada", Toast.LENGTH_SHORT).show()
                        }, onError = { err ->
                            Toast.makeText(context, "Error al actualizar: $err", Toast.LENGTH_LONG).show()
                        })
                        // Si tu repo no actualiza descripción, hay que agregar esa lógica en UTrabajoRepository.
                    } else {
                        Toast.makeText(context, "ID inválido para actualización", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // Crear nueva oferta mediante la API
                    // usamos companyId (si no hay user, se crea con companyId vacío)
                    repo.createJobOffer(
                        companyId = companyId,
                        title = jobOffer.title,
                        description = jobOffer.description,
                        requirements = emptyList(),
                        salary = jobOffer.salary,
                        location = "Por definir",
                        onSuccess = {
                            Toast.makeText(context, "Oferta creada", Toast.LENGTH_SHORT).show()
                        },
                        onError = { err ->
                            Toast.makeText(context, "Error al crear oferta: $err", Toast.LENGTH_LONG).show()
                        }
                    )
                }

                // cerrar diálogo y limpiar
                showAddDialog = false
                editingJob = null
            }
        )
    }
}

@Composable
fun JobOfferListItem(
    offer: JobOffer,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    // MANTENIENDO EXACTAMENTE EL MISMO DISEÑO que CandidateListItem
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Avatar o icono - MANTENIDO IGUAL
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.LightGray, shape = androidx.compose.foundation.shape.CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Información de la oferta - mostramos título, descripción y salario
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = offer.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Descripción (puede ocupar varias líneas)
                if (offer.description.isNotBlank()) {
                    Text(
                        text = offer.description,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Salario al final (valor)
                if (offer.salary.isNotBlank()) {
                    Text(
                        text = offer.salary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Botones de acción
            Row(
                verticalAlignment = Alignment.Top
            ) {
                IconButton(
                    onClick = onEdit
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar oferta",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onDelete
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar oferta",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun JobOfferDialog(
    jobOffer: JobOffer?,
    onDismiss: () -> Unit,
    onSave: (JobOffer) -> Unit
) {
    var title by remember { mutableStateOf(jobOffer?.title ?: "") }
    var salary by remember { mutableStateOf(jobOffer?.salary ?: "") }
    var description by remember { mutableStateOf(jobOffer?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (jobOffer != null) "Editar Oferta" else "Nueva Oferta")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título del puesto") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )

                OutlinedTextField(
                    value = salary,
                    onValueChange = { salary = it },
                    label = { Text("Salario") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val updatedJobOffer = JobOffer(
                        id = jobOffer?.id ?: "",
                        title = title,
                        salary = salary,
                        description = description
                    )
                    onSave(updatedJobOffer)
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
