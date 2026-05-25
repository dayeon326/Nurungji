package com.example.nurungji.ui.navigation

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.nurungji.ui.components.BottomNavBar
import com.example.nurungji.ui.screens.AddItemScreen
import com.example.nurungji.ui.screens.HomeScreen
import com.example.nurungji.ui.screens.InventoryScreen
import com.example.nurungji.ui.screens.ProfileScreen
import com.example.nurungji.ui.screens.RecipeScreen
import com.example.nurungji.ui.screens.ShoppingListScreen
import com.example.nurungji.ui.screens.AddRecipeScreen
import com.example.nurungji.ui.screens.MyRecipesScreen
import com.example.nurungji.ui.screens.RecipeDetailScreen
import com.example.nurungji.ui.screens.SavedRecipesScreen
import com.example.nurungji.ui.screens.EditRecipeScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nurungji.ui.viewmodels.RecipeViewModel
import com.example.nurungji.ui.screens.ReceiptScanScreen

@Composable
fun NurungjiApp(
    onLogout: () -> Unit
) {
    var currentScreen by remember { mutableStateOf(Screen.Home) }
    var previousBackPressedAt by remember { mutableStateOf(0L) }
    var pendingBackTarget by remember { mutableStateOf<Screen?>(null) }
    val recipeViewModel: RecipeViewModel = viewModel()
    val context = LocalContext.current

    fun navigateBackFrom(screen: Screen) {
        when (screen) {
            Screen.Home -> {
                val now = System.currentTimeMillis()
                if (now - previousBackPressedAt < 2000L) {
                    (context as? Activity)?.finish()
                } else {
                    previousBackPressedAt = now
                    Toast.makeText(context, "한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
                }
            }

            Screen.Inventory,
            Screen.Recipes,
            Screen.Profile,
            Screen.ShoppingList -> currentScreen = Screen.Home

            Screen.AddItem,
            Screen.ReceiptScan,
            Screen.AddRecipe,
            Screen.EditRecipe -> {
                pendingBackTarget = when (screen) {
                    Screen.AddItem -> Screen.Inventory
                    Screen.ReceiptScan -> Screen.AddItem
                    Screen.AddRecipe -> Screen.Recipes
                    Screen.EditRecipe -> Screen.RecipeDetail
                    else -> null
                }
            }

            Screen.RecipeDetail -> currentScreen = Screen.Recipes
            Screen.MyRecipes,
            Screen.SavedRecipes -> currentScreen = Screen.Profile
        }
    }

    BackHandler {
        navigateBackFrom(currentScreen)
    }

    Scaffold(
        bottomBar = {
                BottomNavBar(
                    currentScreen = currentScreen,
                    onNavigate = { currentScreen = it }
                )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier.padding(innerPadding)
        ) {

            when (currentScreen) {
                Screen.Home -> HomeScreen(
                    onNavigate = { currentScreen = it },
                    recipeViewModel = recipeViewModel
                )

                Screen.Inventory -> InventoryScreen(
                    onNavigate = { currentScreen = it }
                )

                Screen.AddItem -> AddItemScreen(
                    onNavigate = { currentScreen = it }
                )

                Screen.Recipes -> RecipeScreen(
                    onNavigate = { currentScreen = it },
                    recipeViewModel = recipeViewModel
                )

                Screen.AddRecipe -> AddRecipeScreen(
                    onBack = { currentScreen = Screen.Recipes },
                    recipeViewModel = recipeViewModel
                )

                Screen.ShoppingList -> ShoppingListScreen(
                    onNavigate = { currentScreen = it }
                )

                Screen.Profile -> ProfileScreen(
                    onNavigate = { currentScreen = it },
                    onLogOut = onLogout
                )

                Screen.MyRecipes -> MyRecipesScreen(
                    onBack = { currentScreen = Screen.Profile },
                    onNavigate = { currentScreen = it },
                    recipeViewModel = recipeViewModel
                )
                Screen.RecipeDetail -> RecipeDetailScreen(
                    onBack = { currentScreen = Screen.Recipes },
                    onNavigate = { currentScreen = it },
                    recipeViewModel = recipeViewModel
                )
                Screen.EditRecipe -> EditRecipeScreen(
                    onBack = { currentScreen = Screen.RecipeDetail },
                    recipeViewModel = recipeViewModel
                )
                Screen.SavedRecipes -> SavedRecipesScreen(
                    onBack = { currentScreen = Screen.Profile },
                    onNavigate = { currentScreen = it },
                    recipeViewModel = recipeViewModel
                )
                Screen.ReceiptScan -> ReceiptScanScreen(
                    onBack = { currentScreen = Screen.AddItem },
                    onNavigate = { currentScreen = it }
                )
            }
        }

        pendingBackTarget?.let { target ->
            val isEditScreen = currentScreen == Screen.EditRecipe
            AlertDialog(
                onDismissRequest = { pendingBackTarget = null },
                title = {
                    Text(if (isEditScreen) "수정 중인 내용이 사라집니다" else "작성 중인 내용이 사라집니다")
                },
                text = {
                    Text(
                        if (isEditScreen) {
                            "저장하지 않은 변경사항은 반영되지 않습니다. 나가시겠습니까?"
                        } else {
                            "이 화면을 나가면 입력한 내용이 저장되지 않습니다. 나가시겠습니까?"
                        }
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            pendingBackTarget = null
                            currentScreen = target
                        }
                    ) {
                        Text("나가기")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingBackTarget = null }) {
                        Text(if (isEditScreen) "계속 수정" else "계속 작성")
                    }
                }
            )
        }
    }
}
