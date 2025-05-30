package com.example.greenleaf.presentation.ui.observation
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.greenleaf.presentation.navigation.Screen
import com.example.greenleaf.presentation.viewmodels.AddEditObservationViewModel
import com.example.greenleaf.presentation.viewmodels.HomeViewModel
import com.example.greenleaf.presentation.components.MainBottomBar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditObservationScreen(
    navController: NavController,
    observationId: String?,
    observationViewModel: AddEditObservationViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()

) {
    val isNewObservation = observationId == null

    LaunchedEffect(observationId) {
        if (!isNewObservation) {
            observationViewModel.loadObservation(observationId!!)
        }
    }

    // Handle save success
    LaunchedEffect(observationViewModel.isSaved.value) {
        if (observationViewModel.isSaved.value) {
            // Refresh home screen data
            homeViewModel.loadData()
            // Navigate to observation detail screen
            val observationId = observationViewModel.observation.id
            if (observationId.isNotBlank()) {
                navController.navigate(Screen.ObservationDetail.createRoute(observationId)) {
                    // Pop up to the home screen but don't include it
                    popUpTo(Screen.Home.route) { inclusive = false }
                    // Prevent multiple copies of the same screen
                    launchSingleTop = true
                }
            }
        }
    }

    val observation = observationViewModel.observation
    val context = LocalContext.current
    // ① Hold the picked image URI
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

// ② Launcher for picking one image
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> selectedImageUri = uri }

Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (isNewObservation) "Add Observation" else "Edit Observation",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                }
            )
        },
        bottomBar = {
            MainBottomBar(navController)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .height(150.dp)
                        .width(300.dp)                         // FIXED width
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .clickable {
                            // ③ Launch picker for a single image
                            imagePickerLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        selectedImageUri != null -> {
                            // ④ Show newly picked image
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Picked Observation Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        observationViewModel.observation.observationImageUrl != null -> {
                            // ⑤ Show existing image when editing
                            AsyncImage(
                                model =observationViewModel .observation.observationImageUrl,
                                contentDescription = "Existing Observation Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        else -> {
                            // ⑥ Fallback UI
                            Icon(Icons.Default.AddAPhoto, contentDescription = "Add Observation Photo", modifier = Modifier.size(48.dp))
                        }
                    }
                }

Spacer(modifier = Modifier.height(16.dp))
                // Plant selection dropdown
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = observation.relatedPlantName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Plant") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        isError = observationViewModel.error.value != null && observation.relatedPlantId == null
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        observationViewModel.plants.value.forEach { plant ->
                            DropdownMenuItem(
                                text = { Text(plant.commonName) },
                                onClick = {
                                    observationViewModel.onPlantSelected(plant)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = observation.date,
                    onValueChange = { observationViewModel.onDateChange(it) },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    isError = observationViewModel.error.value != null && observation.date.isBlank()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = observation.time,
                    onValueChange = { observationViewModel.onTimeChange(it) },
                    label = { Text("Time (HH:MM)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    isError = observationViewModel.error.value != null && observation.time.isBlank()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = observation.location,
                    onValueChange = { observationViewModel.onLocationChange(it) },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = observationViewModel.error.value != null && observation.location.isBlank()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = observation.note ?: "",
                    onValueChange = { observationViewModel.onNotesChange(it) },
                    label = { Text("Notes") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )

                // Error message
                observationViewModel.error.value?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

// Cancel / Save buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            observationViewModel.saveObservation(
                                imageUri = selectedImageUri,
                                context = context
                            ) { id ->
                                // Callback after save
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                        modifier = Modifier.weight(1f),
                        enabled = !observationViewModel.isLoading.value
                    ) {
                        if (observationViewModel.isLoading.value) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Save", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
