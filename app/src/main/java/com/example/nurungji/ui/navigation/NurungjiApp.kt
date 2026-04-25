package com.example.nurungji.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

@Composable
fun NurungjiApp() {

    var currentScreen by remember { mutableStateOf(Screen.Home) }
    val recipeViewModel: RecipeViewModel = viewModel()

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
                    onBack = { currentScreen = Screen.Recipes }
                )

                Screen.ShoppingList -> ShoppingListScreen(
                    onNavigate = { currentScreen = it }
                )

                Screen.Profile -> ProfileScreen(
                    onNavigate = { currentScreen = it }
                )

                Screen.MyRecipes -> MyRecipesScreen(
                    onBack = { currentScreen = Screen.Profile },
                    onNavigate = { currentScreen = it }
                )
                Screen.RecipeDetail -> RecipeDetailScreen(
                    onBack = { currentScreen = Screen.Recipes },
                    onNavigate = { currentScreen = it },
                    recipeViewModel = recipeViewModel
                )
                Screen.EditRecipe -> EditRecipeScreen(
                    onBack = { currentScreen = Screen.RecipeDetail }
                )
                Screen.SavedRecipes -> SavedRecipesScreen(
                    onBack = { currentScreen = Screen.Profile },
                    onNavigate = { currentScreen = it }
                )
            }
        }
    }
}