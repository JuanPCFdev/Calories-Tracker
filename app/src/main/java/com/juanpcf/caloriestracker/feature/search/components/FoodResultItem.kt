package com.juanpcf.caloriestracker.feature.search.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.juanpcf.caloriestracker.domain.model.Food
import com.juanpcf.caloriestracker.domain.model.MealType

@Composable
fun FoodResultItem(
    food: Food,
    onAdd: (mealType: MealType, servings: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text(food.name) },
        supportingContent = {
            Text(
                "${food.calories.toInt()} kcal · " +
                    "P:${food.protein.toInt()}g " +
                    "C:${food.carbs.toInt()}g " +
                    "F:${food.fat.toInt()}g"
            )
        },
        trailingContent = {
            IconButton(onClick = { showDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
            }
        },
        modifier = modifier
    )

    if (showDialog) {
        AddToDiaryDialog(
            food = food,
            onConfirm = { mealType, servings ->
                onAdd(mealType, servings)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}
