package com.example.kreditin.ui.payment

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kreditin.ui.data.database.AppDatabase
import com.example.kreditin.ui.data.transaction.Transaction
import com.example.kreditin.ui.theme.KreditinTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.NumberFormat
import java.util.Locale

class InstallmentReceipt : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val transactionId = intent.getIntExtra("TRANSACTION_ID", -1)

        setContent {
            KreditinTheme {
                val context = LocalContext.current
                val database = AppDatabase.getDatabase(context)
                val viewModel: InstallmentReceiptViewModel = viewModel(
                    factory = InstallmentReceiptViewModelFactory(database, transactionId)
                )
                val transaction by viewModel.transaction.collectAsState(initial = null)

                transaction?.let {
                    InstallmentReceiptScreen(it, viewModel)
                } ?: run {
                    // Handle case where transaction is not found
                    Scaffold {
                        Box(modifier = Modifier.fillMaxSize().padding(it), contentAlignment = Alignment.Center) {
                            Text("Transaction not found.")
                        }
                    }
                }
            }
        }
    }
}

class InstallmentReceiptViewModelFactory(private val database: AppDatabase, private val transactionId: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InstallmentReceiptViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InstallmentReceiptViewModel(database, transactionId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class InstallmentReceiptViewModel(private val database: AppDatabase, transactionId: Int) : ViewModel() {
    val transaction: Flow<Transaction?> = database.transactionDao().getTransactionById(transactionId)

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            database.transactionDao().deleteTransaction(transaction)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstallmentReceiptScreen(transaction: Transaction, viewModel: InstallmentReceiptViewModel) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            InstallmentReceiptTopAppBar(
                scrollBehavior = scrollBehavior,
                onNavigateBack = {
                    backPressedDispatcher?.onBackPressed()
                },
                onPrintClick = {
                    generateReceiptPdf(context, transaction)
                },
                onDeleteClick = {
                    viewModel.deleteTransaction(transaction)
                    Toast.makeText(context, "Transaction deleted", Toast.LENGTH_SHORT).show()
                    backPressedDispatcher?.onBackPressed()
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TransactionReceiptCard(transaction = transaction)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstallmentReceiptTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior, 
    onNavigateBack: () -> Unit,
    onPrintClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    MediumTopAppBar(
        title = { Text("Installment Receipt") },
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
        actions = {
            IconButton(onClick = onPrintClick) {
                Icon(
                    imageVector = Icons.Default.Print,
                    contentDescription = "Print PDF",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Transaction",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
fun TransactionReceiptCard(transaction: Transaction) {
    val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "KREDITIN RECEIPT",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // Info Kreditur
            ReceiptRow(label = "Customer", value = transaction.creditorName)
            ReceiptRow(label = "Phone", value = transaction.creditorPhone)
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Info Kendaraan
            Text(
                text = "Unit: ${transaction.motorcycleName}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            ReceiptRow(label = "Price", value = rupiahFormat.format(transaction.motorcyclePrice))
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Rincian Kredit
            ReceiptRow(label = "Down Payment", value = rupiahFormat.format(transaction.downPayment))
            ReceiptRow(label = "Interest Rate", value = "${transaction.interestRate}%")
            ReceiptRow(label = "Tenure", value = "${transaction.tenure} Months")
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Total & Cicilan (Highlight)
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    ReceiptRow(
                        label = "Total Loan", 
                        value = rupiahFormat.format(transaction.totalLoanAmount),
                        isBold = true
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ReceiptRow(
                        label = "Monthly Installment", 
                        value = rupiahFormat.format(transaction.monthlyPayment),
                        valueColor = MaterialTheme.colorScheme.error,
                        isBold = true
                    )
                }
            }

            // Footer
            Text(
                text = "Generated on ${java.text.DateFormat.getDateTimeInstance().format(transaction.timestamp)}",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ReceiptRow(
    label: String, 
    value: String, 
    isBold: Boolean = false, 
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label, 
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value, 
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = valueColor
        )
    }
}

@Preview(showBackground = true)
@Composable
fun InstallmentReceiptScreenPreview() {
    KreditinTheme {
        val sampleTransaction = Transaction(
            id = 1,
            creditorName = "John Doe",
            creditorAddress = "123 Main St",
            creditorPhone = "123-456-7890",
            motorcycleName = "Honda Beat",
            motorcyclePrice = 17000000.0,
            downPayment = 2000000.0,
            interestRate = 1.5,
            tenure = 12,
            loanPrincipal = 15000000.0,
            totalLoanAmount = 17700000.0,
            monthlyPayment = 1475000.0
        )
        InstallmentReceiptScreen(transaction = sampleTransaction, viewModel = viewModel())
    }
}

fun generateReceiptPdf(context: Context, transaction: Transaction) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Ukuran A4
    val page = pdfDocument.startPage(pageInfo)
    val canvas: Canvas = page.canvas
    val paint = Paint()
    val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))


    val dateFormat = java.text.SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
    val dateString = dateFormat.format(java.util.Date(transaction.timestamp))


    val invoiceNumber = "INV/${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)}/${transaction.id.toString().padStart(3, '0')}"

    var yPos = 60f
    val xPos = 50f


    paint.textSize = 20f
    paint.isFakeBoldText = true
    canvas.drawText("KREDITIN RECEIPT", xPos, yPos, paint)

    paint.textSize = 12f
    paint.isFakeBoldText = false
    paint.color = android.graphics.Color.GRAY
    yPos += 20f
    canvas.drawText("Invoice NO: $invoiceNumber", xPos, yPos, paint)
    canvas.drawText("Date: $dateString", 400f, yPos, paint)


    paint.color = android.graphics.Color.BLACK
    yPos += 15f
    canvas.drawLine(xPos, yPos, 545f, yPos, paint)


    paint.textSize = 14f
    yPos += 40f
    canvas.drawText("Customer: ${transaction.creditorName}", xPos, yPos, paint)
    yPos += 25f
    canvas.drawText("Phone: ${transaction.creditorPhone}", xPos, yPos, paint)

    yPos += 40f
    paint.isFakeBoldText = true
    canvas.drawText("Unit: ${transaction.motorcycleName}", xPos, yPos, paint)
    paint.isFakeBoldText = false
    yPos += 25f
    canvas.drawText("Price: ${rupiahFormat.format(transaction.motorcyclePrice)}", xPos, yPos, paint)


    yPos += 40f
    canvas.drawText("Down Payment: ${rupiahFormat.format(transaction.downPayment)}", xPos, yPos, paint)
    yPos += 25f
    canvas.drawText("Tenure: ${transaction.tenure} Months", xPos, yPos, paint)
    yPos += 25f
    canvas.drawText("Interest Rate: ${transaction.interestRate}%", xPos, yPos, paint)

    yPos += 40f
    paint.isFakeBoldText = true
    canvas.drawText("Monthly Installment: ${rupiahFormat.format(transaction.monthlyPayment)}", xPos, yPos, paint)


    paint.isFakeBoldText = false
    paint.textSize = 11f
    yPos += 100f
    canvas.drawText("Statement:", xPos, yPos, paint)
    yPos += 20f
    paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.ITALIC)
    canvas.drawText("\"Declare that the above information is ture and valid\"", xPos, yPos, paint)

    paint.typeface = android.graphics.Typeface.DEFAULT

    yPos += 60f
    paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.ITALIC)
    canvas.drawText("Sincerely,", 400f, yPos, paint)
    yPos += 50f
    canvas.drawText("( ________________ )", 400f, yPos, paint)

    pdfDocument.finishPage(page)

    val fileName = "Invoice_${transaction.id}_${System.currentTimeMillis()}.pdf"

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // API 29 Up
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                resolver.openOutputStream(uri).use { outputStream ->
                    if (outputStream != null) {
                        pdfDocument.writeTo(outputStream)
                        Toast.makeText(context, "PDF created in Download", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            // API Under 28
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(context, "PDF created in: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        }
    } catch (e: IOException) {
        e.printStackTrace()
        Toast.makeText(context, "Gagal menyimpan PDF: ${e.message}", Toast.LENGTH_SHORT).show()
    } finally {
        pdfDocument.close()
    }
}

private fun openPdfIntent(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(Intent.createChooser(intent, "Buka PDF dengan..."))
    } catch (e: Exception) {
        Toast.makeText(context, "Tidak ada aplikasi PDF viewer", Toast.LENGTH_SHORT).show()
    }
}