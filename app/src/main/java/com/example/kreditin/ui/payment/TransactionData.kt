package com.example.kreditin.ui.payment

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import java.io.FileOutputStream
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Locale

class TransactionData : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KreditinTheme {
                val context = LocalContext.current
                val database = AppDatabase.getDatabase(context)
                val viewModel: TransactionListViewModel = viewModel(
                    factory = TransactionListViewModelFactory(database)
                )
                TransactionDataScreen(viewModel = viewModel)
            }
        }
    }
}

class TransactionListViewModelFactory(private val database: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionListViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class TransactionListViewModel(private val database: AppDatabase) : ViewModel() {
    val allTransactions: Flow<List<Transaction>> = database.transactionDao().getAllTransactions()

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            database.transactionDao().deleteTransaction(transaction)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDataScreen(viewModel: TransactionListViewModel) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val transactions by viewModel.allTransactions.collectAsState(initial = emptyList())
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TransactionDataTopAppBar(
                scrollBehavior = scrollBehavior,
                onNavigateBack = {
                    backPressedDispatcher?.onBackPressed()
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(transactions) { transaction ->
                TransactionListItemCard(
                    transaction = transaction,
                    onDeleteClick = {
                        viewModel.deleteTransaction(transaction)
                        Toast.makeText(context, "Transaction deleted", Toast.LENGTH_SHORT).show()
                    },
                    onCardClick = {
                        val intent = Intent(context, InstallmentReceipt::class.java).apply {
                            putExtra("TRANSACTION_ID", transaction.id)
                        }
                        context.startActivity(intent)
                    },
                    onPrintClick = {
                        printTransactionAsPdf(context, transaction)
                    }
                )
            }
        }
    }
}

@Composable
fun TransactionListItemCard(
    transaction: Transaction,
    onDeleteClick: () -> Unit,
    onCardClick: () -> Unit,
    onPrintClick: () -> Unit
) {
    val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = transaction.creditorName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = transaction.motorcycleName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Date: ${DateFormat.getDateInstance().format(transaction.timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Monthly: ${rupiahFormat.format(transaction.monthlyPayment)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .clickable { onPrintClick() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = "Print Transaction",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .clickable { onDeleteClick() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Transaction",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

fun printTransactionAsPdf(context: Context, transaction: Transaction) {
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
    val jobName = "Transaction_Receipt_${transaction.id}"
    val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

    val printAdapter = object : PrintDocumentAdapter() {

        override fun onLayout(
            oldAttributes: PrintAttributes?,
            newAttributes: PrintAttributes,
            cancellationSignal: CancellationSignal?,
            callback: LayoutResultCallback,
            extras: Bundle?
        ) {
            if (cancellationSignal?.isCanceled == true) {
                callback.onLayoutCancelled()
                return
            }

            val pdi = PrintDocumentInfo.Builder("Kreditin_Receipt_${transaction.id}.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(1)
                .build()

            callback.onLayoutFinished(pdi, true)
        }

        override fun onWrite(
            pages: Array<out PageRange>?,
            destination: ParcelFileDescriptor,
            cancellationSignal: CancellationSignal?,
            callback: WriteResultCallback
        ) {
            if (cancellationSignal?.isCanceled == true) {
                callback.onWriteFailed("Cancelled")
                return
            }

            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 page size
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            val labelPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 12f
            }
            val valuePaint = Paint().apply {
                color = Color.BLACK
                textSize = 12f
            }
            val boldValuePaint = Paint(valuePaint).apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val footerPaint = Paint().apply {
                color = Color.GRAY
                textSize = 10f
                textAlign = Paint.Align.CENTER
            }
            val linePaint = Paint().apply {
                color = Color.LTGRAY
                strokeWidth = 1f
            }

            var y = 60f
            val xStart = 40f
            val xEnd = page.canvas.width - 40f

            canvas.drawText("KREDITIN RECEIPT", page.canvas.width / 2f, y, titlePaint)
            y += 30f
            canvas.drawLine(xStart, y, xEnd, y, linePaint)
            y += 30f

            fun drawRow(label: String, value: String, isBold: Boolean = false) {
                canvas.drawText(label, xStart, y, labelPaint)
                val valueX = xEnd - if(isBold) boldValuePaint.measureText(value) else valuePaint.measureText(value)
                canvas.drawText(value, valueX, y, if(isBold) boldValuePaint else valuePaint)
                y += 20f
            }

            drawRow("Customer", transaction.creditorName)
            drawRow("Phone", transaction.creditorPhone)
            y += 10f
            canvas.drawLine(xStart, y, xEnd, y, linePaint)
            y += 30f

            canvas.drawText("Unit: ${transaction.motorcycleName}", xStart, y, boldValuePaint)
            y += 20f
            drawRow("Price", rupiahFormat.format(transaction.motorcyclePrice))
            y += 10f
            canvas.drawLine(xStart, y, xEnd, y, linePaint)
            y += 30f

            drawRow("Down Payment", rupiahFormat.format(transaction.downPayment))
            drawRow("Interest Rate", "${transaction.interestRate}%")
            drawRow("Tenure", "${transaction.tenure} Months")
            y += 10f
            canvas.drawLine(xStart, y, xEnd, y, linePaint)
            y += 30f

            drawRow("Total Loan", rupiahFormat.format(transaction.totalLoanAmount), isBold = true)
            drawRow("Monthly Installment", rupiahFormat.format(transaction.monthlyPayment), isBold = true)

            y += 60f
            val date = DateFormat.getDateTimeInstance().format(transaction.timestamp)
            canvas.drawText("Generated on $date", page.canvas.width / 2f, y, footerPaint)

            document.finishPage(page)

            try {
                FileOutputStream(destination.fileDescriptor).use { fos ->
                    document.writeTo(fos)
                }
                callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            } catch (e: Exception) {
                callback.onWriteFailed(e.toString())
            } finally {
                document.close()
            }
        }
    }

    printManager.print(jobName, printAdapter, null)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDataTopAppBar(scrollBehavior: TopAppBarScrollBehavior, onNavigateBack: () -> Unit) {
    MediumTopAppBar(
        title = { Text("Transaction Data") },
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
fun TransactionDataScreenPreview() {
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
        // This is a simplified preview. In the real app the VM provides the data.
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                TransactionListItemCard(transaction = sampleTransaction, onDeleteClick = {}, onCardClick = {}, onPrintClick = {})
            }
        }
    }
}
