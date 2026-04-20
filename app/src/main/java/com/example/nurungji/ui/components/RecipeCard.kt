package com.example.nurungji.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color

import com.example.nurungji.data.Recipe
import com.example.nurungji.ui.theme.*

@Composable
fun RecipeCard(recipe: Recipe) {

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            Row {

                Image(
                    painter = painterResource(id = recipe.imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = recipe.name,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )

                        Text(
                            text = recipe.time,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    //  보유 재료
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(
                                    TextSecondary.copy(alpha = 0.15f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = TextSecondary.copy(alpha = 0.7f),
                                modifier = Modifier.size(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Text("보유 재료", color = TextSecondary, fontSize = 12.sp)
                    }

                    Row {
                        recipe.availableIngredients.forEach {
                            Box(
                                modifier = Modifier
                                    .padding(end = 6.dp, top = 4.dp)
                                    .background(
                                        Color(0xFFE8F5E9),   //  살짝 진하게
                                        RoundedCornerShape(50)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = it,
                                    color = PrimaryGreenDark,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // 필요한 재료
                    if (recipe.missingIngredients.isNotEmpty()) {

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {

                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        TextSecondary.copy(alpha = 0.15f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = TextSecondary.copy(alpha = 0.7f),
                                    modifier = Modifier.size(10.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Text("필요한 재료", color = TextSecondary, fontSize = 12.sp)
                        }

                        Row {
                            recipe.missingIngredients.forEach {
                                Box(
                                    modifier = Modifier
                                        .padding(end = 6.dp, top = 4.dp)
                                        .background(
                                            MutedGreen,   // 더 연하게
                                            RoundedCornerShape(50)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = it,
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SecondaryGreen
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(
                    text = "🛒 장바구니에 추가",
                    color = PrimaryGreenDark,
                    fontSize = 14.sp
                )
            }
        }
    }
}