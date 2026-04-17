package com.example.nurungji.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.nurungji.data.InventoryItem
import java.util.concurrent.TimeUnit

@Composable
fun ExpiringItemCard(
    item: InventoryItem
) {
    val expireDate = item.expireDate?.toDate()

    val daysLeft = if (expireDate != null) {
        val diff = expireDate.time - System.currentTimeMillis()
        TimeUnit.MILLISECONDS.toDays(diff).toInt()
    } else {
        null
    }

    val badgeColor = when {
        daysLeft == null -> Color(0xFFE0E0E0)
        daysLeft <= 1 -> Color(0xFFFF6B6B)
        daysLeft <= 3 -> Color(0xFFFFB84D)
        else -> Color(0xFFD8F3DC)
    }

    val badgeTextColor = when {
        daysLeft == null -> Color.Gray
        daysLeft <= 3 -> Color.White
        else -> Color(0xFF2D6A4F)
    }

    val dDayText = when {
        daysLeft == null -> "날짜 없음"
        daysLeft < 0 -> "만료됨"
        daysLeft == 0 -> "D-Day"
        else -> "D-$daysLeft"
    }

    Card(
        modifier = Modifier
            .width(120.dp)
            .height(126.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(
                modifier = Modifier
                    .width(44.dp)
                    .height(44.dp)
                    .background(
                        color = Color(0xFFF0F7F4),
                        shape = CircleShape
                    )
            )

            Text(
                text = item.itemName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = dDayText,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = badgeColor,
                        shape = RoundedCornerShape(50.dp)
                    )
                    .padding(vertical = 6.dp),
                color = badgeTextColor,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}