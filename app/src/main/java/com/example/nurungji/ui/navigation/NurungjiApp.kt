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

@Composable
fun NurungjiApp() {

    var currentScreen by remember { mutableStateOf(Screen.Home) }

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
                    onNavigate = { currentScreen = it }
                )

                Screen.Inventory -> InventoryScreen(
                    onNavigate = { currentScreen = it }
                )

                Screen.AddItem -> AddItemScreen(
                    onNavigate = { currentScreen = it }
                )

                Screen.Recipes -> RecipeScreen(
                    onNavigate = { currentScreen = it }
                )

                Screen.Shopping -> ShoppingListScreen(
                    onNavigate = { currentScreen = it }
                )

                Screen.Profile -> ProfileScreen(
                    onNavigate = { currentScreen = it }
                )
            }
        }
    }
}