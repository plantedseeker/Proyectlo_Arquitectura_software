package com.tab.utrabajo.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tab.utrabajo.data.UTrabajoRepository
import com.tab.utrabajo.R
import com.tab.utrabajo.presentation.navigation.Screen

@Composable
fun RegisterStudentScreen(navController: NavHostController) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    // Strings
    val labelFullName = stringResource(R.string.registerstudent_label_fullname)
    val labelEmail = stringResource(R.string.registerstudent_label_email)
    val labelPassword = stringResource(R.string.registerstudent_label_password)
    val labelConfirmPassword = stringResource(R.string.registerstudent_label_confirm_password)

    val pwRequirementsTitle = stringResource(R.string.registerstudent_pw_requirements_title)
    val pwRequirementsList = stringResource(R.string.registerstudent_pw_requirements_list) // contains newlines and bullets

    val errorFillAll = stringResource(R.string.registerstudent_error_fill_all)
    val errorInvalidEmail = stringResource(R.string.registerstudent_error_invalid_email)
    val errorPasswordsMismatch = stringResource(R.string.registerstudent_error_passwords_mismatch)
    val errorPasswordShort = stringResource(R.string.registerstudent_error_password_short)
    val errorPasswordRules = stringResource(R.string.registerstudent_error_password_rules)

    val registeringText = stringResource(R.string.registerstudent_registering)
    val continueText = stringResource(R.string.registerstudent_continue)
    val successRegistered = stringResource(R.string.registerstudent_success_registered)
    val userCheckError = stringResource(R.string.registerstudent_user_check_error_fmt)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(Modifier.height(8.dp))

        // Mostrar error detallado
        errorMessage?.let {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = " ",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Campo Nombre Completo
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(labelFullName) },
            enabled = !isLoading
        )
        Spacer(Modifier.height(8.dp))

        // Campo Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(labelEmail) },
            enabled = !isLoading
        )
        Spacer(Modifier.height(8.dp))

        // Campo Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(labelPassword) },
            visualTransformation = PasswordVisualTransformation(),
            enabled = !isLoading
        )
        Spacer(Modifier.height(8.dp))

        // Campo Confirmar Contraseña
        OutlinedTextField(
            value = confirm,
            onValueChange = { confirm = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(labelConfirmPassword) },
            visualTransformation = PasswordVisualTransformation(),
            enabled = !isLoading
        )
        Spacer(Modifier.height(12.dp))

        // Requisitos de contraseña
        Text(pwRequirementsTitle, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Text(pwRequirementsList)
        Spacer(Modifier.height(18.dp))

        // BOTÓN PRINCIPAL - CONTINUAR
        Button(
            onClick = {
                // Resetear mensajes
                errorMessage = null

                // Validaciones básicas
                if (fullName.isBlank() || email.isBlank() || password.isBlank() || confirm.isBlank()) {
                    errorMessage = errorFillAll
                    return@Button
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    errorMessage = errorInvalidEmail
                    return@Button
                }

                if (password != confirm) {
                    errorMessage = errorPasswordsMismatch
                    return@Button
                }

                if (password.length < 8) {
                    errorMessage = errorPasswordShort
                    return@Button
                }

                // Validar requisitos de contraseña
                val hasUpperCase = password.any { it.isUpperCase() }
                val hasDigit = password.any { it.isDigit() }
                val hasSpecialChar = password.any { !it.isLetterOrDigit() }

                if (!hasUpperCase || !hasDigit || !hasSpecialChar) {
                    errorMessage = errorPasswordRules
                    return@Button
                }

                isLoading = true

                UTrabajoRepository.getInstance().registerStudent(
                    email = email.trim(),
                    password = password,
                    fullName = fullName.trim(),
                    onSuccess = {
                        isLoading = false
                        // mostramos toast y navega
                        Toast.makeText(context, successRegistered, Toast.LENGTH_SHORT).show()
                        navController.navigate(Screen.StudentWorkInfo.route)
                    },
                    onError = { error ->
                        isLoading = false
                        // 'error' es tratado como String (no-nullable). Usamos directamente.
                        errorMessage = String.format(userCheckError, error)
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) Text(registeringText) else Text(continueText)
        }
    }
}
