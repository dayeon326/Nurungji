package com.example.nurungji.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nurungji.ui.viewmodels.ShoppingListViewModel
import com.example.nurungji.models.Recipe
import com.example.nurungji.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipeCard(
    recipe: Recipe,
    userIngredients: List<String>
) {
    val availableIngredients = recipe.ingredients.filter { it in userIngredients }
    val missingIngredients = recipe.ingredients.filter { it !in userIngredients }
    val context = LocalContext.current
    val ShoppingListViewModel: ShoppingListViewModel = viewModel()

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
                        .size(88.dp)
                        .clip(RoundedCornerShape(18.dp))
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = recipe.title,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 18.sp,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = recipe.time,
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = recipe.content,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (recipe.hashtags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            recipe.hashtags.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFEAF4EF), RoundedCornerShape(50))
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "#$tag",
                                        color = PrimaryGreenDark,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (availableIngredients.isNotEmpty()) {
                IngredientSection(
                    title = "보유 재료",
                    ingredients = availableIngredients,
                    icon = Icons.Default.Check,
                    chipColor = Color.White,
                    textColor = PrimaryGreenDark
                )
            }

            if (missingIngredients.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                IngredientSection(
                    title = "필요한 재료",
                    ingredients = missingIngredients,
                    icon = Icons.Default.Close,
                    chipColor = Color(0xFFE8F5E9),
                    textColor = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    missingIngredients.forEach {
                        ShoppingListViewModel.addItem(context, it)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryGreen),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "🛒 장바구니에 추가",
                    color = PrimaryGreenDark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IngredientSection(
    title: String,
    ingredients: List<String>,
    icon: ImageVector,
    chipColor: Color,
    textColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(TextSecondary.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary.copy(alpha = 0.75f),
                modifier = Modifier.size(11.dp)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = title,
            color = TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ingredients.forEach { ingredient ->
            Box(
                modifier = Modifier
                    .background(chipColor, RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = ingredient,
                    color = textColor,
                    fontSize = 12.sp
                )
            }
        }
    }
}