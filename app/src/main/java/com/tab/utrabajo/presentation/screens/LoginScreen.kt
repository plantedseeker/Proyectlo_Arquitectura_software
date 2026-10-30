package com.tab.utrabajo.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tab.utrabajo.R
import com.tab.utrabajo.data.UTrabajoRepository
import com.tab.utrabajo.presentation.components.UTrabajoBrandMark
import com.tab.utrabajo.presentation.components.UTrabajoErrorBanner
import com.tab.utrabajo.presentation.navigation.Screen

@Composable
fun LoginScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val emailField = stringResource(R.string.email_field)
    val passwordField = stringResource(R.string.password_field)
    val loginButton = stringResource(R.string.login_button)
    val companyLoginButton = stringResource(R.string.company_login_button)
    val recoverPassword = stringResource(R.string.recover_password)
    val fillAllFieldsError = stringResource(R.string.fill_all_fields_error)
    val uidError = stringResource(R.string.uid_error)
    val userCheckError = stringResource(R.string.user_check_error)
    val companyCheckError = stringResource(R.string.company_check_error)

    fun submit(expectedRole: String, destination: String, roleError: String) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = fillAllFieldsError
            return
        }

        isLoading = true
        errorMessage = null
        UTrabajoRepository.getInstance().loginUser(
            email = email.trim(),
            password = password,
            onSuccess = {
                isLoading = false
                val repository = UTrabajoRepository.getInstance()
                val user = repository.getCurrentUser()
                when {
                    user == null -> errorMessage = uidError
                    user.role == expectedRole -> {
                        navController.navigate(destination) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    }
                    else -> {
                        repository.logout()
                        errorMessage = roleError
                    }
                }
            },
            onError = { error ->
                isLoading = false
                errorMessage = error
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 22.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UTrabajoBrandMark(modifier = Modifier.size(52.dp))
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text("UTrabajo", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Talento que encuentra su lugar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(44.dp))
            Text(
                text = "Bienvenido de nuevo",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Ingresa con el correo asociado a tu perfil.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(28.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    errorMessage?.let {
                        UTrabajoErrorBanner(message = it)
                        Spacer(Modifier.height(16.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(emailField) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Email, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        enabled = !isLoading
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(passwordField) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Lock, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña"
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        enabled = !isLoading
                    )
                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            submit(
                                expectedRole = "student",
                                destination = Screen.JobsList.route,
                                roleError = userCheckError
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Outlined.School, contentDescription = null)
                            Spacer(Modifier.size(9.dp))
                            Text(loginButton)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            submit(
                                expectedRole = "company",
                                destination = Screen.CompanyHome.route,
                                roleError = companyCheckError
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !isLoading,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Outlined.Business, contentDescription = null)
                        Spacer(Modifier.size(9.dp))
                        Text(companyLoginButton)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            TextButton(
                onClick = { navController.navigate(Screen.RecoverStart.route) },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                enabled = !isLoading
            ) {
                Text(recoverPassword)
            }
        }
    }
}