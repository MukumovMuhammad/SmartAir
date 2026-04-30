package com.example.smartairmonitoring.ui.auth

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompleteProfileScreen(
    viewModel: AuthViewModel,
    onSuccess: () -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var selectedAgeGroup by remember { mutableStateOf("") }
    var selectedConditions by remember { mutableStateOf(setOf<String>()) }
    var otherCondition by remember { mutableStateOf("") }

    val ageGroups = listOf("Under 18", "18 - 24", "25 - 34", "35 - 44", "45 - 54", "55 - 64", "65+")
    val healthConditions = listOf("Asthma", "Allergies", "Bronchitis", "COPD", "Heart Condition", "None", "Others")

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
        
        Box(modifier = Modifier.fillMaxSize().background(BackgroundDeepNavy.copy(alpha = 0.85f)))

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
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Help us personalize your air quality insights",
                color = TextSecondary,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Name Fields Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = BackgroundSecondary.copy(alpha = 0.5f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BackgroundElevated)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
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
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Age Group Section
            SectionHeader(title = "Your Age Group")
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ageGroups.forEach { age ->
                    val isSelected = selectedAgeGroup == age
                    SelectableChip(
                        text = age,
                        isSelected = isSelected,
                        onClick = { selectedAgeGroup = age }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Health Conditions Section
            SectionHeader(title = "Health Conditions", subtitle = "Select all that apply")
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                healthConditions.forEach { condition ->
                    val isSelected = selectedConditions.contains(condition)
                    SelectableChip(
                        text = condition,
                        isSelected = isSelected,
                        showCheckbox = true,
                        onClick = {
                            val newSet = selectedConditions.toMutableSet()
                            if (condition == "None") {
                                if (isSelected) newSet.remove("None") else {
                                    newSet.clear()
                                    newSet.add("None")
                                }
                            } else {
                                newSet.remove("None")
                                if (isSelected) newSet.remove(condition) else newSet.add(condition)
                            }
                            selectedConditions = newSet
                        }
                    )
                }
            }

            // "Others" Text Field
            AnimatedVisibility(
                visible = selectedConditions.contains("Others"),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    AppTextField(
                        value = otherCondition,
                        onValueChange = { otherCondition = it },
                        label = "Specify Other Conditions",
                        placeholder = "e.g., Sinusitis, Dust sensitivity",
                        leadingIcon = Icons.Default.HealthAndSafety
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            if (authState is AuthState.Loading) {
                CircularProgressIndicator(color = AIAccent, modifier = Modifier.size(48.dp))
            } else {
                PrimaryGradientButton(
                    text = "Finish Registration",
                    onClick = {
                        if (firstName.isNotBlank() && surname.isNotBlank() && selectedAgeGroup.isNotBlank() && selectedConditions.isNotEmpty()) {
                            val finalConditionsSet = selectedConditions.toMutableSet()
                            if (finalConditionsSet.contains("Others")) {
                                finalConditionsSet.remove("Others")
                                if (otherCondition.isNotBlank()) {
                                    finalConditionsSet.add(otherCondition)
                                }
                            }
                            
                            viewModel.completeProfile(
                                firstName, 
                                surname, 
                                selectedAgeGroup, 
                                finalConditionsSet.joinToString(", ")
                            )
                        } else {
                            Toast.makeText(context, "Please complete all fields", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String? = null) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun SelectableChip(
    text: String,
    isSelected: Boolean,
    showCheckbox: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) AIAccent.copy(alpha = 0.15f) else BackgroundSecondary.copy(alpha = 0.6f))
            .border(
                width = 1.5.dp,
                color = if (isSelected) AIAccent else BackgroundElevated,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showCheckbox) {
                Icon(
                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isSelected) AIAccent else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = text,
                color = if (isSelected) AIAccent else TextPrimary,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}
