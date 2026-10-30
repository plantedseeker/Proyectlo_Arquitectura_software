package com.tab.utrabajo.presentation.screens

import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import com.tab.utrabajo.R
import com.tab.utrabajo.data.Cancellable
import com.tab.utrabajo.data.UTrabajoRepository
import com.tab.utrabajo.presentation.navigation.Screen
import com.tab.utrabajo.presentation.components.MainTab
import com.tab.utrabajo.presentation.components.UTrabajoBottomBar
import com.tab.utrabajo.presentation.components.UTrabajoPageHeader
import com.tab.utrabajo.presentation.components.UTrabajoEmptyState
import com.tab.utrabajo.presentation.components.UTrabajoLoadingState
import com.tab.utrabajo.presentation.components.UTrabajoErrorBanner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun JobsListScreen(navController: NavHostController) {
    val repository = remember { UTrabajoRepository.getInstance() }

    var query by remember { mutableStateOf("") }
    var jobList by remember { mutableStateOf<List<JobItemData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }

    // Recursos

    val headerTitle = stringResource(R.string.jobslist_header_title)
    val headerSubtitle = stringResource(R.string.jobslist_header_subtitle)
    val searchPlaceholder = stringResource(R.string.jobslist_search_placeholder)
    val viewDetails = stringResource(R.string.jobslist_view_details)
    val viewDetailsDesc = stringResource(R.string.jobslist_view_details_desc)

    // Listener en tiempo real
    DisposableEffect(Unit) {
        val listener: Cancellable = repository.listenToActiveJobOffers(
            onUpdate = { jobs ->
                val mapped = jobs.map { doc ->
                    val position = (
                            doc["titulo"]
                                ?: doc["title"]
                                ?: "Sin título"
                            ).toString()

                    val salary = (
                            doc["salario"]
                                ?: doc["salary"]
                                ?: "Salario no especificado"
                            ).toString()

                    val description = (
                            doc["descripcion"]
                                ?: doc["description"]
                                ?: ""
                            ).toString()

                    val jobId = doc["id"]?.toString() ?: doc["documentId"]?.toString() ?: ""
                    val companyId = doc["empresaId"]?.toString() ?: ""

                    JobItemData(
                        id = jobId,
                        companyId = companyId,
                        position = position,
                        salary = salary,
                        description = description,
                        isChecked = false
                    )
                }
                jobList = mapped
                isLoading = false
                loadError = null
            },
            onError = { error ->
                jobList = emptyList()
                isLoading = false
                loadError = error
            }
        )

        onDispose {
            try {
                listener.remove()
            } catch (_: Exception) { }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            UTrabajoBottomBar(navController, selected = MainTab.Home)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            UTrabajoPageHeader(
                title = headerTitle,
                subtitle = headerSubtitle,
                eyebrow = "Oportunidades para ti"
            )

            // Barra de búsqueda
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(searchPlaceholder) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Loading o lista
            if (isLoading) {
                UTrabajoLoadingState(modifier = Modifier.fillMaxSize())
            } else if (loadError != null) {
                UTrabajoErrorBanner(
                    message = loadError.orEmpty(),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            } else {
                val filtered = jobList.filter {
                    it.position.contains(query, ignoreCase = true) ||
                            it.salary.contains(query, ignoreCase = true)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    if (filtered.isEmpty()) {
                        item {
                            UTrabajoEmptyState(
                                title = if (query.isBlank()) "No hay empleos disponibles" else "No encontramos coincidencias",
                                subtitle = if (query.isBlank()) "Vuelve pronto para descubrir nuevas oportunidades." else "Prueba con otro cargo o rango salarial.",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        items(items = filtered, key = { it.id }) { job ->
                            JobCardForList(
                                job = job,
                                repository = repository,
                                navController = navController,
                                onClick = { },
                                viewDetails = viewDetails,
                                viewDetailsDesc = viewDetailsDesc
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JobCardForList(
    job: JobItemData,
    repository: UTrabajoRepository,
    navController: NavHostController,
    onClick: () -> Unit,
    viewDetails: String,
    viewDetailsDesc: String
) {
    val cardHeight = 110.dp
    var showDescriptionDialog by remember { mutableStateOf(false) }
    var animateToExpanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val currentUser = repository.getCurrentUser()
    // Estado para mostrar mensajes de resultado
    var showMessage by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Mostrar Toast cuando showMessage sea true
    LaunchedEffect(showMessage) {
        if (showMessage) {
            Toast.makeText(context, messageText, Toast.LENGTH_SHORT).show()
            // resetear el flag para evitar repetir el Toast
            showMessage = false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight)
            .clickable {
                onClick()
                showDescriptionDialog = true
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp, horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Work,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = job.position,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = job.salary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                modifier = Modifier
                    .width(80.dp)
                    .padding(start = 6.dp)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFFEEEEEE), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = viewDetailsDesc,
                        tint = Color(0xFF6D6D6D),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = viewDetails,
                    fontSize = 11.sp,
                    color = Color(0xFF6D6D6D),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    // Aquí uso la animación que te gustó (igual que en el otro archivo)
    if (showDescriptionDialog) {
        LaunchedEffect(Unit) {
            animateToExpanded = false
            delay(10)
            animateToExpanded = true
        }

        val targetHeight = if (animateToExpanded) 380.dp else cardHeight
        val animatedHeight by animateDpAsState(targetValue = targetHeight, animationSpec = tween(durationMillis = 380))
        val scaleTarget = if (animateToExpanded) 1f else 0.96f
        val animatedScale by animateFloatAsState(targetValue = scaleTarget, animationSpec = tween(durationMillis = 380))

        Dialog(onDismissRequest = {
            coroutineScope.launch {
                animateToExpanded = false
                delay(200)
                showDescriptionDialog = false
            }
        }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(animatedHeight)
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                    },
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 6.dp,
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = job.position,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            coroutineScope.launch {
                                animateToExpanded = false
                                delay(180)
                                showDescriptionDialog = false
                            }
                        }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(top = 6.dp)
                    ) {
                        Text(
                            text = "Descripción",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = job.description.ifBlank { "Descripción no disponible" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = job.salary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = {
                                if (currentUser != null && job.id.isNotEmpty() && job.companyId.isNotEmpty()) {
                                    repository.applyToJob(
                                        jobId = job.id,
                                        studentId = currentUser.uid,
                                        companyId = job.companyId,
                                        jobTitle = job.position,
                                        onSuccess = {
                                            // MOSTRAR MENSAJE DE ÉXITO
                                            messageText = "¡Has aplicado exitosamente!"
                                            showMessage = true

                                            // Cerrar el diálogo automáticamente al aplicar (animación)
                                            coroutineScope.launch {
                                                animateToExpanded = false
                                                delay(180)
                                                showDescriptionDialog = false
                                            }

                                            // Crear chat automáticamente (no bloquea el cierre)
                                            repository.createOrGetChat(
                                                studentId = currentUser.uid,
                                                companyId = job.companyId,
                                                jobId = job.id,
                                                jobTitle = job.position,
                                                onSuccess = { chatId -> /* opcional */ },
                                                onError = { error ->
                                                    // opcional: actualizar mensaje o loggear
                                                    messageText = "Aplicación exitosa, pero error al crear chat"
                                                    showMessage = true
                                                }
                                            )
                                        },
                                        onError = { error ->
                                            messageText = "Error al aplicar: $error"
                                            showMessage = true
                                        }
                                    )
                                } else {
                                    messageText = "No se puede aplicar. Información incompleta."
                                    showMessage = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(text = stringResource(R.string.apply_button))
                        }
                    }
                }
            }
        }
    }

    // Snackbar de mensajes (se mantiene, además del Toast)
    if (showMessage) {
        LaunchedEffect(showMessage) {
            delay(2500)
            showMessage = false
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Snackbar(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(messageText)
            }
        }
    }
}

data class JobItemData(
    val id: String,
    val companyId: String,
    val position: String,
    val salary: String,
    val description: String,
    val isChecked: Boolean
)
