package com.example.nurungji

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nurungji.data.InventoryItem
import com.example.nurungji.ui.InventoryViewModel
import com.example.nurungji.ui.theme.NurungjiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NurungjiTheme {
                InventoryScreen()
            }
        }
    }
}

@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = viewModel()
) {
    val inventoryItems by viewModel.inventoryItems.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadInventory()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "재고 목록",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        errorMessage?.let {
            Text(text = "오류: $it")
        }

        if (inventoryItems.isEmpty()) {
            Text(text = "재고가 없습니다.")
        } else {
            LazyColumn {
                items(inventoryItems) { item ->
                    InventoryItemCard(item)
                }
            }
        }
    }
}

@Composable
fun InventoryItemCard(item: InventoryItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(text = "상품명: ${item.itemName}")
        Text(text = "카테고리: ${item.category}")
        Text(text = "수량: ${item.quantity}")
        Text(text = "사용자: ${item.userId}")
    }
}