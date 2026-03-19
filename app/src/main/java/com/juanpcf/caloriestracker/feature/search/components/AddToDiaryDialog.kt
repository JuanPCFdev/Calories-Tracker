package com.juanpcf.caloriestracker.feature.search.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.juanpcf.caloriestracker.R
import com.juanpcf.caloriestracker.domain.model.Food
import com.juanpcf.caloriestracker.domain.model.MealType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddToDiaryDialog(
    food: Food,
    onConfirm: (MealType, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMeal by remember { mutableStateOf(MealType.BREAKFAST) }
    var servingsText by remember { mutableStateOf("1.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(food.name) },
        text = {
            Column {
                Text(stringResource(R.string.select_meal))
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(modifier = Modifier.fillMaxWidth()) {
                    MealType.entries.forEach { mealType ->
                        val label = when (mealType) {
                            MealType.BREAKFAST -> stringResource(R.string.meal_breakfast)
                            MealType.LUNCH -> stringResource(R.string.meal_lunch)
                            MealType.DINNER -> stringResource(R.string.meal_dinner)
                            MealType.SNACK -> stringResource(R.string.meal_snack)
                        }
                        FilterChip(
                            selected = selectedMeal == mealType,
                            onClick = { selectedMeal = mealType },
                            label = { Text(label) },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = servingsText,
                    onValueChange = { servingsText = it },
                    label = { Text(stringResource(R.string.label_servings)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(selectedMeal, servingsText.toDoubleOrNull() ?: 1.0)
                }
            ) {
                Text(stringResource(R.string.btn_add_to_diary))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}
