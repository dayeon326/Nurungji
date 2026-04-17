package com.example.nurungji.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.nurungji.ui.theme.ExpiringDanger
import com.example.nurungji.ui.theme.ExpiringWarning

@Composable
fun ExpiringItemCard(
    emoji: String
) {
    val itemName = when (emoji) {
        "🍅" -> "토마토"
        "🥛" -> "우유"
        "🥬" -> "양상추"
        else -> "식품"
    }

    val dday = when (emoji) {
        "🍅" -> 2
        "🥛" -> 1
        "🥬" -> 3
        else -> 5
    }

    val badgeColor = when {
        dday <= 1 -> ExpiringDanger
        dday <= 3 -> ExpiringWarning
        else -> MaterialTheme.colorScheme.secondary
    }

    val badgeTextColor = when {
        dday <= 3 -> Color.White
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.width(120.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = itemName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .background(
                        color = badgeColor,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "D-$dday",
                    style = MaterialTheme.typography.labelMedium,
                    color = badgeTextColor
                )
            }
        }
    }
}