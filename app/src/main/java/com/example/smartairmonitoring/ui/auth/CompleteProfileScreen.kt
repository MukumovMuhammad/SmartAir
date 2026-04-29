package com.example.smartairmonitoring.ui.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartairmonitoring.R
import com.example.smartairmonitoring.ui.components.AppTextField
import com.example.smartairmonitoring.ui.components.PrimaryGradientButton
import com.example.smartairmonitoring.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteProfileScreen(
    viewModel: AuthViewModel,
    onSuccess: () -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var ageGroup by remember { mutableStateOf("") }
    var healthCondition by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onSuccess()
            viewModel.resetState()
        } else if (authState is AuthState.Error) {
            Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_img),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        Box(modifier = Modifier.fillMaxSize().background(BackgroundDeepNavy.copy(alpha = 0.8f)))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = buildAnnotatedString {
                    append("Complete ")
                    withStyle(style = SpanStyle(color = AIAccent)) {
                        append("Profile")
                    }
                },
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Help us personalize your experience",
                color = TextSecondary,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            AppTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = "First Name",
                placeholder = "Enter your first name",
                leadingIcon = Icons.Default.Person
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = surname,
                onValueChange = { surname = it },
                label = "Surname",
                placeholder = "Enter your surname",
                leadingIcon = Icons.Default.Person
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = ageGroup,
                onValueChange = { ageGroup = it },
                label = "Age Group",
                placeholder = "e.g. 18 - 24",
                leadingIcon = Icons.Default.History
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = healthCondition,
                onValueChange = { healthCondition = it },
                label = "Health Condition",
                placeholder = "e.g. Asthma, None",
                leadingIcon = Icons.Default.HealthAndSafety
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (authState is AuthState.Loading) {
                CircularProgressIndicator(color = AIAccent)
            } else {
                PrimaryGradientButton(
                    text = "Finish",
                    onClick = {
                        if (firstName.isNotBlank() && surname.isNotBlank()) {
                            viewModel.completeProfile(firstName, surname, ageGroup, healthCondition)
                        } else {
                            Toast.makeText(context, "Please fill in your name", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}
