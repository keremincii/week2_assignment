package com.example.week2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.week2.ui.theme.Week2_assignmentTheme

// A. Veri katmanı: tüm alanlar val, @Immutable ile Compose'a kararlılık sözü
@Immutable
data class HardwareItem(
    val id: String,
    val name: String,
    val category: String,
    val cost: Double,
    val stockCount: Int
)

val hardwareList = listOf(
    HardwareItem(id = "101", name = "MacBook Pro M3 Max", category = "Geliştirici Bilgisayarı", cost = 3499.0, stockCount = 5),
    HardwareItem(id = "102", name = "NVIDIA RTX 4090 Sunucu", category = "Yapay Zeka / GPU", cost = 1599.0, stockCount = 2),
    HardwareItem(id = "103", name = "Google Pixel 8 Pro", category = "Mobil Test Cihazı", cost = 999.0, stockCount = 8),
    HardwareItem(id = "104", name = "Dell UltraSharp 49\"", category = "Monitör", cost = 1199.0, stockCount = 4),
    HardwareItem(id = "105", name = "Sony WH-1000XM5 Kulaklık", category = "Odaklanma Ekipmanı", cost = 399.0, stockCount = 12),
    HardwareItem(id = "106", name = "Raspberry Pi 5 8GB", category = "IoT Geliştirme Kiti", cost = 85.0, stockCount = 25),
    HardwareItem(id = "107", name = "HHKB Professional Keyboard", category = "Ergonomik Klavye", cost = 299.0, stockCount = 0), // Stok sınırı test verisi
    HardwareItem(id = "108", name = "AMD Ryzen Threadripper", category = "Yapay Zeka / GPU", cost = 4999.0, stockCount = 1)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Week2_assignmentTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DevClusterScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// D. Tek merkezi Stateful kapsayıcı: tüm durum burada, alt bileşenler stateless
@Composable
fun DevClusterScreen(modifier: Modifier = Modifier) {
    // B. Ekran dönünce kaybolmaması için Bundle'a kaydedilir
    var searchQuery by rememberSaveable { mutableStateOf("") }

    // B. Toplam ödenek Slot Table'da remember ile tutulur
    var totalSpent by remember { mutableDoubleStateOf(0.0) }

    // B. Turnike: filtre sadece searchQuery değişince yeniden hesaplanır
    val filteredList = remember(searchQuery) {
        hardwareList.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = modifier) {
        Text(
            text = "Toplam harcanan: $${"%.2f".format(totalSpent)}",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
        )
        HardwareSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            modifier = Modifier.padding(16.dp)
        )
        // C. LazyColumn + key ile nokta atışı recomposition
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = filteredList,
                key = { it.id }
            ) { item ->
                HardwareCard(
                    item = item,
                    onClick = {
                        if (item.stockCount > 0) {
                            totalSpent += item.cost
                        }
                    }
                )
            }
        }
    }
}

// D. Stateless: içinde remember yok, durum yukarıdan gelir, olay yukarı gider
@Composable
fun HardwareSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text(text = "Donanım veya kategori ara") },
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )
}

// D. Stateless kart. Modifier sırası: önce clickable, sonra padding
@Composable
fun HardwareCard(
    item: HardwareItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val inStock = item.stockCount > 0
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = inStock) { onClick() }
            .padding(16.dp)
    ) {
        Column {
            Text(text = item.name, style = MaterialTheme.typography.titleMedium)
            Text(text = item.category)
            Text(text = "Fiyat: $${item.cost}")
            if (inStock) {
                Text(text = "Stok Adedi: ${item.stockCount}")
            } else {
                Text(text = "Stokta yok", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
fun DevClusterScreenPreview() {
    Week2_assignmentTheme {
        DevClusterScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun HardwareSearchBarPreview() {
    Week2_assignmentTheme {
        HardwareSearchBar(query = "Mac", onQueryChange = { })
    }
}

@Preview(showBackground = true)
@Composable
fun HardwareCardPreview() {
    Week2_assignmentTheme {
        HardwareCard(item = hardwareList[0], onClick = { })
    }
}

@Preview(showBackground = true)
@Composable
fun HardwareCardOutOfStockPreview() {
    Week2_assignmentTheme {
        HardwareCard(item = hardwareList[6], onClick = { })
    }
}
