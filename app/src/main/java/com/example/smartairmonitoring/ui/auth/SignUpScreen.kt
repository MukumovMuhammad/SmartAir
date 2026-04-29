package com.example.smartairmonitoring.ui.auth


import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager

import com.example.smartairmonitoring.R
import com.example.smartairmonitoring.ui.components.AppTextField
import com.example.smartairmonitoring.ui.components.GoogleSignInButton
import com.example.smartairmonitoring.ui.components.PrimaryGradientButton
import com.example.smartairmonitoring.ui.theme.*

const val TAG = "SignUpScreen_TAG"

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    onBackClick: () -> Unit,
    onSuccess: () -> Unit,
    onLoginClick: () -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = CredentialManager.create(context)
    val webClientId = stringResource(id = R.string.default_web_client_id)
    val current =   LocalContext.current

    var EightCharacters by remember { mutableStateOf(false) }
    var ContainsNumber by remember { mutableStateOf(false) }
    var ContainsUppercase by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            Log.i(TAG, "Success")
            onSuccess()
//            viewModel.resetState()
        } else if (authState is AuthState.Error) {
            Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.bg_img),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Dark Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDeepNavy.copy(alpha = 0.8f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Add Placeholder
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(BackgroundSecondary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = AIAccent,
                        modifier = Modifier.size(40.dp)
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(24.dp)
                            .background(AQIGood, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = buildAnnotatedString {
                        append("Create ")
                        withStyle(style = SpanStyle(color = AIAccent)) {
                            append("Account")
                        }
                    },
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Join SmartAir and breathe better",
                    color = TextSecondary,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            AppTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = "First Name",
                placeholder = "First name",
                modifier = Modifier.weight(1f),
                leadingIcon = Icons.Default.Person
            )
            AppTextField(
                value = surname,
                onValueChange = { surname = it },
                label = "Surname",
                placeholder = "Surname",
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "Enter your email",
                leadingIcon = Icons.Default.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                placeholder = "Create a password",
                leadingIcon = Icons.Default.Lock,
                hideText = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirm Password",
                placeholder = "Confirm your password",
                leadingIcon = Icons.Default.Lock,
                hideText = true
            )

            Spacer(modifier = Modifier.height(16.dp))
            EightCharacters =  password.length >= 8
            ContainsNumber = password.any { it.isDigit() }
            ContainsUppercase = password.any { it.isUpperCase() }


            // Password requirements (Simple validation check examples)
            PasswordRequirementItem(text = "At least 8 characters", isMet = EightCharacters)
            PasswordRequirementItem(text = "Contains a number", isMet = ContainsNumber)
            PasswordRequirementItem(text = "Contains an uppercase letter", isMet = ContainsUppercase)

            Spacer(modifier = Modifier.height(32.dp))


            if (authState is AuthState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = AIAccent)
            } else {
                PrimaryGradientButton(
                    text = "Create Account",
                    onClick = {

                        if (EightCharacters && ContainsNumber && ContainsUppercase){
                            if (password == confirmPassword) {
                                viewModel.signUp(firstName, surname, email, password)
                            } else {
                                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                            }
                        }else{
                            Toast.makeText(context, "Password requirements not met", Toast.LENGTH_SHORT).show()
                        }

                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = BackgroundElevated)
                    Text(
                        text = " OR ",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = BackgroundElevated)
                }

                Spacer(modifier = Modifier.height(24.dp))

                GoogleSignInButton(
                    text = "Sign up with Google",
                    onClick = {
                        viewModel.signInWithGoogle(current)
                    }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "Already have an account? ", color = TextSecondary)
                Text(
                    text = "Login",
                    color = AIAccent,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onLoginClick() }
                )
            }
        }
    }
}

@Composable
fun PasswordRequirementItem(text: String, isMet: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isMet) AQIGood else TextDisabled,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = if (isMet) AQIGood else TextSecondary,
            fontSize = 12.sp
        )
    }
}

