package com.example.kreditin.ui.payment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kreditin.ui.data.creditor.Creditor
import com.example.kreditin.ui.data.database.AppDatabase
import com.example.kreditin.ui.data.motorcycle.Motorcycle
import com.example.kreditin.ui.theme.KreditinTheme
import kotlinx.coroutines.flow.Flow

class TransactionViewModelFactory(private val database: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class TransactionViewModel(private val database: AppDatabase) : ViewModel() {
    val allCreditors: Flow<List<Creditor>> = database.creditorDao().getAllCreditors()
    val allMotorcycles: Flow<List<Motorcycle>> = database.motorcycleDao().getAllMotorcycles()
}

class TransactionReqData : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KreditinTheme {
                val context = LocalContext.current
                val database = AppDatabase.getDatabase(context)
                val viewModel: TransactionViewModel = viewModel(
                    factory = TransactionViewModelFactory(database)
                )
                val creditors by viewModel.allCreditors.collectAsState(initial = emptyList())
                val motorcycles by viewModel.allMotorcycles.collectAsState(initial = emptyList())
                UserSelectionScreen(creditors = creditors, motorcycles = motorcycles)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSelectionScreen(creditors: List<Creditor>, motorcycles: List<Motorcycle>) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    var showCreditorSheet by remember { mutableStateOf(false) }
    var showMotorcycleSheet by remember { mutableStateOf(false) }
    var selectedCreditor by remember { mutableStateOf<Creditor?>(null) }
    var selectedMotorcycle by remember { mutableStateOf<Motorcycle?>(null) }
    val creditorSheetState = rememberModalBottomSheetState()
    val motorcycleSheetState = rememberModalBottomSheetState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TransactionTopAppBar(
                scrollBehavior = scrollBehavior,
                onNavigateBack = {
                    backPressedDispatcher?.onBackPressed()
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = selectedCreditor?.name ?: "",
                onValueChange = {},
                label = { Text("Select Creditor") },
                placeholder = { Text("Tap to select...") },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCreditorSheet = true },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                leadingIcon = { Icon(Icons.Default.ContactPhone, contentDescription = null) }
            )

            selectedCreditor?.let { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Creditor Detail:", style = MaterialTheme.typography.labelLarge)
                        Text(text = "Address: ${user.address}")
                        Text(text = "Phone: ${user.phone}")
                    }
                }
            }

            OutlinedTextField(
                value = selectedMotorcycle?.model ?: "",
                onValueChange = {},
                label = { Text("Select Motorcycle List") },
                placeholder = { Text("Tap to select...") },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showMotorcycleSheet = true },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                leadingIcon = { Icon(Icons.Default.TwoWheeler, contentDescription = null) }
            )

            selectedMotorcycle?.let { motorcycle ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Motorcycle Detail:", style = MaterialTheme.typography.labelLarge)
                        Text(text = "Year: ${motorcycle.year}")
                        Text(text = "Color: ${motorcycle.color}")
                        Text(text = "Price: ${motorcycle.price}")
                    }
                }
            }

            //TODO
        }

        if (showCreditorSheet) {
            ModalBottomSheet(
                onDismissRequest = { showCreditorSheet = false },
                sheetState = creditorSheetState
            ) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)) {
                    Text(
                        text = "Select Creditor",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(creditors) { user ->
                            ListItem(
                                headlineContent = { Text(user.name) },
                                supportingContent = { Text(user.address) },
                                trailingContent = { Text(user.phone) },
                                modifier = Modifier.clickable {
                                    selectedCreditor = user
                                    showCreditorSheet = false
                                }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }

        if (showMotorcycleSheet) {
            ModalBottomSheet(
                onDismissRequest = { showMotorcycleSheet = false },
                sheetState = motorcycleSheetState
            ) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)) {
                    Text(
                        text = "Select Motorcycle List",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(motorcycles) { motorcycle ->
                            ListItem(
                                headlineContent = { Text(motorcycle.model) },
                                supportingContent = { Text("${motorcycle.year} - ${motorcycle.color}") },
                                trailingContent = { Text("${motorcycle.price}") },
                                modifier = Modifier.clickable {
                                    selectedMotorcycle = motorcycle
                                    showMotorcycleSheet = false
                                }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionTopAppBar(scrollBehavior: TopAppBarScrollBehavior, onNavigateBack: () -> Unit) {
    MediumTopAppBar(
        title = { Text("Transaction Request") },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        ),
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Navigate back"
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@Preview(showBackground = true)
@Composable
fun UserSelectionScreenPreview() {
    KreditinTheme {
        val creditors = listOf(
            Creditor(id = 1, name = "John Doe", email = "john.doe@example.com", phone = "123-456-7890", address = "123 Main St"),
            Creditor(id = 2, name = "Jane Smith", email = "jane.smith@example.com", phone = "098-765-4321", address = "456 Oak Ave")
        )
        val motorcycles = listOf(
            Motorcycle(id = 1, model = "Honda Beat", year = 2021, color = "Black", price = 17000000),
            Motorcycle(id = 2, model = "Yamaha NMAX", year = 2022, color = "Red", price = 35000000)
        )
        UserSelectionScreen(creditors = creditors, motorcycles = motorcycles)
    }
}
