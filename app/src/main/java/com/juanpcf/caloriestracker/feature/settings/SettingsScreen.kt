package com.juanpcf.caloriestracker.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanpcf.caloriestracker.BuildConfig
import com.juanpcf.caloriestracker.R
import com.juanpcf.caloriestracker.domain.model.Language
import com.juanpcf.caloriestracker.domain.model.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToGoals: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsUiEvent.NavigateToGoals -> onNavigateToGoals()
                is SettingsUiEvent.ShowThemeDialog -> showThemeDialog = true
                is SettingsUiEvent.ShowLanguageDialog -> showLanguageDialog = true
                is SettingsUiEvent.SignOut -> onSignOut()
                is SettingsUiEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    if (showThemeDialog) {
        ThemeDialog(
            currentTheme = uiState.theme,
            onThemeSelected = { theme ->
                viewModel.onThemeSelected(theme)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showLanguageDialog) {
        LanguageDialog(
            currentLanguage = uiState.language,
            onLanguageSelected = { language ->
                viewModel.onLanguageSelected(language)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }

    if (showSignOutDialog) {
        SignOutDialog(
            onConfirm = {
                showSignOutDialog = false
                viewModel.onSignOut()
            },
            onDismiss = { showSignOutDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.title_settings)) })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(vertical = 8.dp)
        ) {

            // ── Account section ──────────────────────────────────────────
            item {
                SettingsSectionHeader(stringResource(R.string.settings_account))
            }
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column {
                        ListItem(
                            headlineContent = {
                                Text(uiState.userProfile?.displayName ?: uiState.userProfile?.email ?: "")
                            },
                            supportingContent = {
                                if (uiState.userProfile?.displayName != null) {
                                    Text(uiState.userProfile?.email ?: "")
                                }
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Filled.AccountCircle,
                                    contentDescription = null
                                )
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = stringResource(R.string.btn_sign_out),
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            modifier = Modifier.clickable { showSignOutDialog = true }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // ── Appearance section ───────────────────────────────────────
            item {
                SettingsSectionHeader(stringResource(R.string.settings_appearance))
            }
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column {
                        val themeLabel = when (uiState.theme) {
                            Theme.LIGHT -> stringResource(R.string.settings_theme_light)
                            Theme.DARK -> stringResource(R.string.settings_theme_dark)
                            Theme.SYSTEM -> stringResource(R.string.settings_theme_system)
                        }
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.settings_theme)) },
                            supportingContent = { Text(themeLabel) },
                            modifier = Modifier.clickable { viewModel.onShowThemeDialog() }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        val languageLabel = when (uiState.language) {
                            Language.EN -> stringResource(R.string.settings_language_en)
                            Language.ES -> stringResource(R.string.settings_language_es)
                        }
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.settings_language)) },
                            supportingContent = { Text(languageLabel) },
                            modifier = Modifier.clickable { viewModel.onShowLanguageDialog() }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // ── Goals section ────────────────────────────────────────────
            item {
                SettingsSectionHeader(stringResource(R.string.settings_goals))
            }
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_goals)) },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.clickable { viewModel.onNavigateToGoals() }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // ── Version footer ───────────────────────────────────────────
            item {
                Text(
                    text = "${stringResource(R.string.settings_version)}: ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

@Composable
private fun ThemeDialog(
    currentTheme: Theme,
    onThemeSelected: (Theme) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_theme)) },
        text = {
            Column {
                Theme.entries.forEach { theme ->
                    val label = when (theme) {
                        Theme.LIGHT -> stringResource(R.string.settings_theme_light)
                        Theme.DARK -> stringResource(R.string.settings_theme_dark)
                        Theme.SYSTEM -> stringResource(R.string.settings_theme_system)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(theme) }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = currentTheme == theme,
                            onClick = { onThemeSelected(theme) }
                        )
                        Text(label, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}

@Composable
private fun LanguageDialog(
    currentLanguage: Language,
    onLanguageSelected: (Language) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_language)) },
        text = {
            Column {
                Language.entries.forEach { language ->
                    val label = when (language) {
                        Language.EN -> stringResource(R.string.settings_language_en)
                        Language.ES -> stringResource(R.string.settings_language_es)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageSelected(language) }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = currentLanguage == language,
                            onClick = { onLanguageSelected(language) }
                        )
                        Text(label, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}

@Composable
private fun SignOutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.btn_sign_out)) },
        text = { Text(stringResource(R.string.settings_sign_out_confirm)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.btn_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}
