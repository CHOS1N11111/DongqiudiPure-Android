package io.github.chos1n11111.dongqiudipure.feature.account

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.chos1n11111.dongqiudipure.core.data.SessionState
import io.github.chos1n11111.dongqiudipure.core.designsystem.R as DesignR
import io.github.chos1n11111.dongqiudipure.core.designsystem.icon.DqdIcons
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSize
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdTheme
import io.github.chos1n11111.dongqiudipure.core.model.AppError
import io.github.chos1n11111.dongqiudipure.core.model.NetworkKind
import io.github.chos1n11111.dongqiudipure.core.model.isRetryable

@Composable
fun AccountRoute(
    onSettingsClick: () -> Unit,
    onAppInfoClick: () -> Unit,
    modifier: Modifier = Modifier,
    appVersion: String = "0.1.0",
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
    var showLogin by remember { mutableStateOf(false) }

    LaunchedEffect(sessionState) {
        if (sessionState is SessionState.Authenticated) showLogin = false
    }

    AccountScreen(
        sessionState = sessionState,
        onLoginClick = { showLogin = true },
        onRetrySession = viewModel::retrySessionValidation,
        onLogout = viewModel::logout,
        onSettingsClick = onSettingsClick,
        onAppInfoClick = onAppInfoClick,
        appVersion = appVersion,
        modifier = modifier,
    )

    if (showLogin && sessionState !is SessionState.Authenticated) {
        LoginDialog(
            sessionState = sessionState,
            onSubmit = viewModel::login,
            onDismiss = { showLogin = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountScreen(
    sessionState: SessionState,
    onLoginClick: () -> Unit,
    onRetrySession: () -> Unit,
    onLogout: () -> Unit,
    onSettingsClick: () -> Unit,
    onAppInfoClick: () -> Unit,
    appVersion: String,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.account_title)) },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            painter = painterResource(DqdIcons.Settings),
                            contentDescription = stringResource(DesignR.string.ds_action_settings),
                            modifier = Modifier.size(DqdSize.iconMedium),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            SessionHeader(
                state = sessionState,
                onLoginClick = onLoginClick,
                onRetrySession = onRetrySession,
                onLogout = onLogout,
            )

            Column(
                modifier = Modifier
                    .padding(top = DqdSpacing.sm)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer),
            ) {
                SettingsRow(
                    icon = DqdIcons.Settings,
                    label = stringResource(R.string.account_row_settings),
                    onClick = onSettingsClick,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SettingsRow(
                    icon = DqdIcons.Info,
                    label = stringResource(R.string.account_row_app_info),
                    value = appVersion,
                    onClick = onAppInfoClick,
                )
            }

            Text(
                text = stringResource(R.string.account_disclaimer),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DqdSpacing.lg, vertical = DqdSpacing.xl),
            )
        }
    }
}

@Composable
private fun SessionHeader(
    state: SessionState,
    onLoginClick: () -> Unit,
    onRetrySession: () -> Unit,
    onLogout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = DqdSpacing.lg, vertical = DqdSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DqdSpacing.md),
    ) {
        AccountAvatar(loading = state.isBusy())

        when (state) {
            SessionState.Restoring -> SessionProgress(R.string.account_session_restoring)
            SessionState.SubmittingCredentials -> SessionProgress(R.string.account_login_submitting)
            SessionState.ValidatingSession -> SessionProgress(R.string.account_login_validating)
            is SessionState.Authenticated -> {
                Text(
                    text = state.account.displayName
                        ?: stringResource(R.string.account_authenticated_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.account_authenticated_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                OutlinedButton(onClick = onLogout) {
                    Text(stringResource(R.string.account_logout_cta))
                }
            }
            SessionState.Expired -> AnonymousContent(
                statusText = stringResource(R.string.account_session_expired),
                onLoginClick = onLoginClick,
            )
            is SessionState.Anonymous -> {
                AnonymousContent(
                    statusText = state.error?.let { loginErrorText(it) },
                    onLoginClick = onLoginClick,
                )
                if (state.error?.isRetryable == true) {
                    TextButton(onClick = onRetrySession) {
                        Text(stringResource(R.string.account_retry_session))
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountAvatar(loading: Boolean) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
            )
        } else {
            Icon(
                painter = painterResource(DqdIcons.Person),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun SessionProgress(label: Int) {
    Text(
        text = stringResource(label),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun AnonymousContent(
    statusText: String?,
    onLoginClick: () -> Unit,
) {
    Text(
        text = stringResource(R.string.account_anonymous_title),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Text(
        text = stringResource(R.string.account_anonymous_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
    if (statusText != null) {
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
    }
    Button(
        onClick = onLoginClick,
        modifier = Modifier
            .padding(top = DqdSpacing.xs)
            .widthIn(max = 240.dp)
            .fillMaxWidth(),
    ) {
        Icon(
            painter = painterResource(DqdIcons.Login),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = stringResource(R.string.account_login_cta),
            modifier = Modifier.padding(start = DqdSpacing.sm),
        )
    }
}

@Composable
private fun LoginDialog(
    sessionState: SessionState,
    onSubmit: (String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showValidationError by remember { mutableStateOf(false) }
    val busy = sessionState.isBusy()
    val submit = {
        if (identifier.isBlank() || password.isBlank()) {
            showValidationError = true
        } else {
            showValidationError = false
            onSubmit(identifier, password)
            password = ""
        }
    }

    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text(stringResource(R.string.account_login_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(DqdSpacing.md)) {
                Text(
                    text = stringResource(R.string.account_login_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = identifier,
                    onValueChange = {
                        if (it.length <= MAX_IDENTIFIER_LENGTH) {
                            identifier = it
                            showValidationError = false
                        }
                    },
                    enabled = !busy,
                    singleLine = true,
                    label = { Text(stringResource(R.string.account_login_identifier)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        if (it.length <= MAX_PASSWORD_LENGTH) {
                            password = it
                            showValidationError = false
                        }
                    },
                    enabled = !busy,
                    singleLine = true,
                    label = { Text(stringResource(R.string.account_login_password)) },
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { passwordVisible = !passwordVisible },
                            enabled = !busy,
                        ) {
                            Icon(
                                painter = painterResource(
                                    if (passwordVisible) DqdIcons.VisibilityOff else DqdIcons.Visibility,
                                ),
                                contentDescription = stringResource(
                                    if (passwordVisible) {
                                        R.string.account_hide_password
                                    } else {
                                        R.string.account_show_password
                                    },
                                ),
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (showValidationError) {
                    Text(
                        text = stringResource(R.string.account_login_fields_required),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                (sessionState as? SessionState.Anonymous)?.error?.let { error ->
                    Text(
                        text = loginErrorText(error),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                if (busy) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                        Text(
                            text = stringResource(
                                if (sessionState is SessionState.ValidatingSession) {
                                    R.string.account_login_validating
                                } else {
                                    R.string.account_login_submitting
                                },
                            ),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = submit, enabled = !busy) {
                Text(stringResource(R.string.account_login_submit))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) {
                Text(stringResource(R.string.account_login_cancel))
            }
        },
    )
}

@Composable
private fun loginErrorText(error: AppError): String = when (error) {
    is AppError.Network -> when (error.kind) {
        NetworkKind.NoConnection -> stringResource(R.string.account_error_no_connection)
        NetworkKind.Timeout -> stringResource(R.string.account_error_timeout)
        NetworkKind.TlsFailure -> stringResource(R.string.account_error_secure_connection)
        NetworkKind.Unknown -> stringResource(R.string.account_error_network)
    }
    is AppError.RateLimited -> stringResource(R.string.account_error_rate_limited)
    is AppError.Server -> when (error.code) {
        "40002" -> stringResource(R.string.account_login_fields_required)
        "40003" -> stringResource(R.string.account_error_credentials)
        "40026" -> stringResource(R.string.account_error_client_version)
        else -> stringResource(R.string.account_error_challenge)
    }
    is AppError.Http -> stringResource(R.string.account_error_server)
    is AppError.Parse,
    is AppError.UnsupportedContract,
    -> stringResource(R.string.account_error_contract)
    AppError.AuthenticationRequired,
    AppError.SessionExpired,
    -> stringResource(R.string.account_error_session_validation)
}

private fun SessionState.isBusy(): Boolean =
    this == SessionState.Restoring ||
        this == SessionState.SubmittingCredentials ||
        this == SessionState.ValidatingSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppInfoScreen(
    appVersion: String,
    onAboutClick: () -> Unit,
    onLicenseClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.account_app_info_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(DqdIcons.ArrowBack),
                            contentDescription = stringResource(DesignR.string.ds_action_back),
                            modifier = Modifier.size(DqdSize.iconMedium),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surfaceContainer),
        ) {
            SettingsRow(
                icon = DqdIcons.Info,
                label = stringResource(R.string.account_row_about),
                value = appVersion,
                onClick = onAboutClick,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            SettingsRow(
                icon = DqdIcons.Link,
                label = stringResource(R.string.account_row_source),
                subtitle = stringResource(R.string.account_repository_url),
                onClick = { runCatching { uriHandler.openUri(PROJECT_URL) } },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            SettingsRow(
                icon = DqdIcons.File,
                label = stringResource(R.string.account_row_license),
                value = "GPL-3.0",
                onClick = onLicenseClick,
            )
        }
    }
}

@Composable
private fun SettingsRow(
    @DrawableRes icon: Int,
    label: String,
    onClick: () -> Unit,
    value: String? = null,
    subtitle: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .defaultMinSize(minHeight = DqdSize.touchTarget)
            .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.md),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(DqdSize.iconSmall),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            painter = painterResource(DqdIcons.ChevronRight),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(DqdSize.iconSmall),
        )
    }
}

private const val PROJECT_URL = "https://github.com/CHOS1N11111/DongqiudiPure-Android"
private const val MAX_IDENTIFIER_LENGTH = 128
private const val MAX_PASSWORD_LENGTH = 256

@Preview(name = "我的 · 未登录", showBackground = true)
@Composable
private fun AccountAnonymousPreview() {
    DqdTheme(darkTheme = false) {
        AccountScreen(
            sessionState = SessionState.Anonymous(),
            onLoginClick = {},
            onRetrySession = {},
            onLogout = {},
            onSettingsClick = {},
            onAppInfoClick = {},
            appVersion = "0.1.0",
        )
    }
}

@Preview(name = "我的 · 已登录", showBackground = true)
@Composable
private fun AccountAuthenticatedPreview() {
    DqdTheme(darkTheme = true) {
        AccountScreen(
            sessionState = SessionState.Authenticated(
                io.github.chos1n11111.dongqiudipure.core.model.AccountSummary(),
            ),
            onLoginClick = {},
            onRetrySession = {},
            onLogout = {},
            onSettingsClick = {},
            onAppInfoClick = {},
            appVersion = "0.1.0",
        )
    }
}
