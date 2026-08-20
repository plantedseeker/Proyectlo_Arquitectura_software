package com.tab.utrabajo.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tab.utrabajo.data.UTrabajoRepository
import com.tab.utrabajo.R
import com.tab.utrabajo.domain.JobOfferDraft
import com.tab.utrabajo.domain.JobOfferRules
import com.tab.utrabajo.domain.JobOfferValidationResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateJobScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = UTrabajoRepository.getInstance()
    val currentUser = repository.getCurrentUser()

    if (currentUser == null) {
        Toast.makeText(context, context.getString(R.string.createjob_error_no_user), Toast.LENGTH_LONG).show()
        navController.navigate("company_home")
        return
    }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var requirements by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.createjob_title_topbar)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.createjob_label_title)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.createjob_label_description)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 4
            )

            OutlinedTextField(
                value = requirements,
                onValueChange = { requirements = it },
                label = { Text(stringResource(R.string.createjob_label_requirements)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 3
            )

            OutlinedTextField(
                value = salary,
                onValueChange = { salary = it },
                label = { Text(stringResource(R.string.createjob_label_salary)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text(stringResource(R.string.createjob_label_location)) },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = stringResource(R.string.createjob_required_fields_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = {
                    val validation = JobOfferRules.validate(
                        JobOfferDraft(
                            title = title,
                            description = description,
                            requirements = requirements,
                            salary = salary,
                            location = location
                        )
                    )
                    if (validation is JobOfferValidationResult.Invalid) {
                        Toast.makeText(context, context.getString(R.string.createjob_toast_complete_fields), Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true
                    val offer = (validation as JobOfferValidationResult.Valid).offer

                    repository.createJobOffer(
                        companyId = currentUser.uid,
                        title = offer.title,
                        description = offer.description,
                        requirements = offer.requirements,
                        salary = offer.salary,
                        location = offer.location,
                        onSuccess = {
                            isLoading = false
                            Toast.makeText(context, context.getString(R.string.createjob_toast_created), Toast.LENGTH_SHORT).show()
                            navController.navigate("job_created")
                        },
                        onError = { error ->
                            isLoading = false
                            Toast.makeText(context, context.getString(R.string.createjob_error_fmt, error), Toast.LENGTH_LONG).show()
                        }
                    )
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.createjob_button_publish))
                }
            }
        }
    }
}
