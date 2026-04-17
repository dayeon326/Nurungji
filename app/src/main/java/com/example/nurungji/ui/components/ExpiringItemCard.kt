package com.example.nurungji.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
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
import com.example.nurungji.ui.screens.calculateDaysLeft
import com.example.nurungji.ui.screens.dDayBackgroundColor
import com.example.nurungji.ui.screens.dDayText
import com.example.nurungji.ui.screens.dDayTextColor
import com.example.nurungji.ui.screens.getCategoryEmoji

@Composable
fun ExpiringItemCard(
    item: InventoryItem
) {
    val expireDate = item.expireDate?.toDate()
    val daysLeft = calculateDaysLeft(expireDate)
    val badgeLabel = dDayText(daysLeft)
    val badgeColor = dDayBackgroundColor(daysLeft)
    val badgeTextColor = dDayTextColor(daysLeft)

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
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(44.dp)
                    .background(
                        color = Color(0xFFF0F7F4),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getCategoryEmoji(item.category),
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Text(
                text = item.itemName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = badgeColor,
                        shape = RoundedCornerShape(50.dp)
                    )
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeLabel,
                    color = badgeTextColor,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}