package com.tab.utrabajo.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.tab.utrabajo.presentation.screens.ApplicationsScreen
import com.tab.utrabajo.presentation.screens.ChatDetailScreen
import com.tab.utrabajo.presentation.screens.ChatListScreen
import com.tab.utrabajo.presentation.screens.CompanyDocumentsUploadScreen
import com.tab.utrabajo.presentation.screens.CompanyRepresentativeScreen
import com.tab.utrabajo.presentation.screens.CompleteCompanyScreen
import com.tab.utrabajo.presentation.screens.CreateJobScreen
import com.tab.utrabajo.presentation.screens.EmpleoScreen
import com.tab.utrabajo.presentation.screens.JobCreatedScreen
import com.tab.utrabajo.presentation.screens.JobsListScreen
import com.tab.utrabajo.presentation.screens.LoginScreen
import com.tab.utrabajo.presentation.screens.ProfileScreen
import com.tab.utrabajo.presentation.screens.RecoverStartScreen
import com.tab.utrabajo.presentation.screens.RecoverSuccessScreen
import com.tab.utrabajo.presentation.screens.RegisterCompanyScreen
import com.tab.utrabajo.presentation.screens.RegisterStudentScreen
import com.tab.utrabajo.presentation.screens.RegistrationCompleteScreen
import com.tab.utrabajo.presentation.screens.ResetPasswordScreen
import com.tab.utrabajo.presentation.screens.RoleSelectionScreen
import com.tab.utrabajo.presentation.screens.SplashScreen
import com.tab.utrabajo.presentation.screens.StudentSkillsScreen
import com.tab.utrabajo.presentation.screens.StudentUploadCVScreen
import com.tab.utrabajo.presentation.screens.StudentWorkInfoScreen
import com.tab.utrabajo.presentation.screens.VerifyCodeScreen
import com.tab.utrabajo.ui.company.CompanyHomeScreen

private val onboardingTitles = mapOf(
    Screen.RegisterStudent.route to "Crear perfil de estudiante",
    Screen.StudentWorkInfo.route to "Experiencia laboral",
    Screen.StudentSkills.route to "Habilidades",
    Screen.StudentUploadCV.route to "Hoja de vida",
    Screen.RegisterCompany.route to "Crear perfil de empresa",
    Screen.CompanyRepInfo.route to "Representante legal",
    Screen.CompanyDocsUpload.route to "Documentos",
    Screen.CompleteCompany.route to "Completar empresa",
    Screen.RecoverStart.route to "Recuperar acceso",
    Screen.VerifyCode.route to "Verificar código",
    Screen.ResetPassword.route to "Nueva contraseña",
    Screen.RecoverSuccess.route to "Acceso recuperado"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val topBarTitle = onboardingTitles[currentRoute]

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (topBarTitle != null) {
                TopAppBar(
                    title = {
                        Text(
                            text = topBarTitle,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) { SplashScreen(navController) }
            composable(Screen.Login.route) { LoginScreen(navController) }
            composable(Screen.RoleSelection.route) { RoleSelectionScreen(navController) }
            composable(Screen.RegisterStudent.route) { RegisterStudentScreen(navController) }
            composable(Screen.StudentWorkInfo.route) { StudentWorkInfoScreen(navController) }
            composable(Screen.StudentSkills.route) { StudentSkillsScreen(navController) }
            composable(Screen.StudentUploadCV.route) { StudentUploadCVScreen(navController) }
            composable(Screen.RegisterCompany.route) { RegisterCompanyScreen(navController) }
            composable(Screen.CompanyRepInfo.route) { CompanyRepresentativeScreen(navController) }
            composable(Screen.CompanyDocsUpload.route) { CompanyDocumentsUploadScreen(navController) }
            composable(Screen.CompleteCompany.route) { CompleteCompanyScreen(navController) }
            composable(Screen.RegistrationComplete.route) { RegistrationCompleteScreen(navController) }
            composable(Screen.RecoverStart.route) { RecoverStartScreen(navController) }
            composable(Screen.VerifyCode.route) { VerifyCodeScreen(navController) }
            composable(Screen.ResetPassword.route) { ResetPasswordScreen(navController) }
            composable(Screen.RecoverSuccess.route) { RecoverSuccessScreen(navController) }

            composable(Screen.CompanyHome.route) { CompanyHomeScreen(navController) }
            composable("empleo") { EmpleoScreen(navController) }
            composable(Screen.JobsList.route) { JobsListScreen(navController) }
            composable(Screen.Profile.route) { ProfileScreen(navController = navController) }
            composable(Screen.CreateJob.route) { CreateJobScreen(navController) }
            composable(Screen.JobCreated.route) { JobCreatedScreen(navController) }

            composable(
                route = Screen.ChatList.route,
                deepLinks = listOf(
                    navDeepLink { uriPattern = "android-app://androidx.navigation/company_chats" }
                )
            ) {
                ChatListScreen(navController)
            }

            composable(
                Screen.ChatDetail.route + "/{chatId}",
                arguments = listOf(navArgument("chatId") { type = NavType.StringType })
            ) { entry ->
                ChatDetailScreen(navController, entry.arguments?.getString("chatId"))
            }

            composable("applications_screen") { ApplicationsScreen(navController) }
        }
    }
}