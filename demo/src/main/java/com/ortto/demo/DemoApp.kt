package com.ortto.demo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Token
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoApp(
    viewModel: DemoViewModel,
    requestNotificationPermission: () -> Unit,
) {
    val snackbar = remember { SnackbarHostState() }
    val toast = viewModel.toastMessage

    LaunchedEffect(toast) {
        if (toast != null) {
            snackbar.showSnackbar(toast)
            viewModel.consumeToast()
        }
    }
    LaunchedEffect(Unit) {
        if (viewModel.consumeFirstOpenPermissionRequest()) requestNotificationPermission()
    }

    Box(Modifier.fillMaxSize()) {
        AnimatedContent(targetState = viewModel.isSignedIn, label = "authentication") { signedIn ->
            if (signedIn) {
                MainShell(viewModel, requestNotificationPermission, snackbar)
            } else {
                LoginScreen(viewModel, snackbar)
            }
        }

        viewModel.pendingConfirmationLink?.let { link ->
            AlertDialog(
                onDismissRequest = viewModel::dismissDeepLink,
                icon = { Icon(Icons.Default.NotificationsActive, null) },
                title = { Text("Open from notification?") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("You tapped a push notification. Continue to Delivery?")
                        Text(DemoLogic.short(link, 18), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }
                },
                confirmButton = { Button(onClick = viewModel::confirmDeepLink) { Text("Open Delivery") } },
                dismissButton = { TextButton(onClick = viewModel::dismissDeepLink) { Text("Cancel") } },
            )
        }
    }
}

@Composable
private fun LoginScreen(viewModel: DemoViewModel, snackbar: SnackbarHostState) {
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val submit = {
        focusManager.clearFocus()
        keyboard?.hide()
        viewModel.signIn()
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF9270FF), Color(0xFFFF9D61), Color(0xFFFFD76F)),
                ),
            ),
    ) {
        Box(
            Modifier
                .size(320.dp)
                .align(Alignment.TopEnd)
                .background(Color.White.copy(alpha = 0.10f), CircleShape),
        )
        LazyColumn(
            Modifier
                .fillMaxSize()
                .imePadding(),
        ) {
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 86.dp, start = 28.dp, end = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Surface(
                        modifier = Modifier.size(82.dp),
                        shape = RoundedCornerShape(26.dp),
                        color = Color.White.copy(alpha = 0.94f),
                        shadowElevation = 12.dp,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("O", fontSize = 43.sp, fontWeight = FontWeight.Black, color = OrttoColors.Lilac)
                        }
                    }
                    Text("Push Demo", style = MaterialTheme.typography.headlineLarge, color = OrttoColors.Ink)
                    AssistChip(
                        onClick = {},
                        label = { Text("Firebase Cloud Messaging") },
                        leadingIcon = { Icon(Icons.Default.CloudDone, null, Modifier.size(18.dp)) },
                    )
                }
            }
            item { Spacer(Modifier.height(140.dp)) }

            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 42.dp, topEnd = 42.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White.copy(alpha = 0.96f)),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 18.dp),
                ) {
                    Column(
                        Modifier
                            .padding(28.dp)
                            .navigationBarsPadding(),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        Text(
                            if (viewModel.email.isBlank()) "Sign in" else "Jump back in!",
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        Text(
                            "Identify a demo contact and exercise every Android SDK push flow.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        OutlinedTextField(
                            value = viewModel.email,
                            onValueChange = { viewModel.email = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { contentDescription = "Email address" },
                            label = { Text("Email") },
                            leadingIcon = { Icon(Icons.Outlined.Email, null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(onDone = { submit() }),
                        )
                        Button(
                            onClick = submit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            enabled = viewModel.email.trim().isNotEmpty() && !viewModel.isIdentifying,
                        ) {
                            if (viewModel.isIdentifying) {
                                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(10.dp))
                            }
                            Text(if (viewModel.email.isBlank()) "Sign in" else "Continue")
                        }
                    }
                }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).navigationBarsPadding())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainShell(
    viewModel: DemoViewModel,
    requestNotificationPermission: () -> Unit,
    snackbar: SnackbarHostState,
) {
    val wide = LocalConfiguration.current.screenWidthDp >= 840
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            when (viewModel.selectedTab) {
                                DemoTab.Home -> "Push registration"
                                DemoTab.Delivery -> "Delivery"
                                DemoTab.Log -> "Log"
                            },
                        )
                        Text(
                            "Android SDK ${com.ortto.messaging.BuildConfig.SDK_VERSION}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showTechnicalDetails = true }) {
                        Icon(Icons.Default.Settings, "Technical details")
                    }
                    IconButton(onClick = viewModel::logout, enabled = !viewModel.isLoggingOut) {
                        Icon(Icons.AutoMirrored.Filled.Logout, "Sign out")
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            if (!wide) {
                NavigationBar {
                    DemoTab.entries.forEach { tab ->
                        NavigationBarItem(
                            modifier = Modifier.semantics { contentDescription = "${tab.title} tab" },
                            selected = viewModel.selectedTab == tab,
                            onClick = { viewModel.selectTab(tab) },
                            icon = { Icon(tab.icon, null) },
                            label = { Text(tab.title) },
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Row(Modifier.fillMaxSize().padding(padding)) {
            if (wide) {
                NavigationRail(Modifier.fillMaxHeight()) {
                    Spacer(Modifier.height(20.dp))
                    DemoTab.entries.forEach { tab ->
                        NavigationRailItem(
                            modifier = Modifier.semantics { contentDescription = "${tab.title} tab" },
                            selected = viewModel.selectedTab == tab,
                            onClick = { viewModel.selectTab(tab) },
                            icon = { Icon(tab.icon, null) },
                            label = { Text(tab.title) },
                        )
                    }
                }
            }
            AnimatedContent(viewModel.selectedTab, label = "tabs", modifier = Modifier.weight(1f)) { tab ->
                when (tab) {
                    DemoTab.Home -> HomeScreen(viewModel, requestNotificationPermission)
                    DemoTab.Delivery -> DeliveryScreen(viewModel, requestNotificationPermission)
                    DemoTab.Log -> LogScreen(viewModel.logEntries)
                }
            }
        }
    }

    if (viewModel.showTechnicalDetails) {
        ModalBottomSheet(onDismissRequest = { viewModel.showTechnicalDetails = false }) {
            TechnicalDetails(viewModel)
        }
    }
}

@Composable
private fun HomeScreen(viewModel: DemoViewModel, requestPermission: () -> Unit) {
    LazyColumn(
        Modifier.fillMaxSize().testTag("home-list"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionCard("Contact", Icons.Default.Person) {
                DetailRow("Email", viewModel.signedInEmail, copyValue = viewModel.signedInEmail)
                DetailRow("Remembered", "Yes")
                DetailRow("SDK session", if (viewModel.sessionId == null) "Anonymous" else "Identified")
                FilledTonalButton(
                    onClick = { viewModel.showTechnicalDetails = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.BugReport, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Technical details")
                }
            }
        }
        if (viewModel.configurationIssues.isNotEmpty()) {
            item {
                SectionCard("Configuration", Icons.Default.Settings) {
                    viewModel.configurationIssues.forEach { IssueRow(it) }
                }
            }
        }
        item {
            SectionCard("Device", Icons.Default.Devices) {
                DetailRow("Provider", "Firebase Cloud Messaging")
                DetailRow("Status", viewModel.registrationStatus)
                DetailRow("Permission", viewModel.permissionStatus)
                DetailRow("Android registration", viewModel.registrationStatus)
                if (viewModel.hasToken) {
                    DetailRow(
                        "Token",
                        DemoLogic.short(viewModel.fcmToken),
                        copyValue = viewModel.fcmToken,
                        monospace = true,
                    )
                    DetailRow("Fingerprint", DemoLogic.tokenFingerprint(viewModel.fcmToken) + "…", monospace = true)
                } else {
                    Button(
                        onClick = { viewModel.requestPushRegistration(requestPermission) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.NotificationsActive, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Request FCM token")
                    }
                }
            }
        }
    }
}

@Composable
private fun DeliveryScreen(viewModel: DemoViewModel, requestPermission: () -> Unit) {
    LazyColumn(
        Modifier.fillMaxSize().testTag("delivery-list"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionCard("Current status", Icons.Default.CloudDone) {
                DetailRow("Provider", "Firebase Cloud Messaging")
                DetailRow("Token", if (viewModel.hasToken) DemoLogic.short(viewModel.fcmToken) else "No SDK token", monospace = true)
                DetailRow("Permission", viewModel.permissionStatus)
                DetailRow("Remote registration", viewModel.registrationStatus)
            }
        }
        item {
            SectionCard("FCM token override", Icons.Outlined.Token) {
                OutlinedTextField(
                    value = viewModel.tokenOverride,
                    onValueChange = { viewModel.tokenOverride = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Firebase registration token") },
                    supportingText = { Text("Filled automatically; paste a token only as a fallback.") },
                    minLines = 2,
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                )
                FilledTonalButton(onClick = viewModel::registerTokenOverride, modifier = Modifier.fillMaxWidth()) {
                    Text("Register current token")
                }
            }
        }
        item {
            SectionCard("Click tracking", Icons.Default.Link) {
                OutlinedTextField(
                    value = viewModel.trackedDeepLink,
                    onValueChange = { viewModel.trackedDeepLink = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Tracked push-action deep link") },
                    placeholder = { Text("${DemoConfig.deepLinkScheme}://delivery?tracking_url=…") },
                    supportingText = { Text("Paste the action link from an Ortto push payload.") },
                    minLines = 3,
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                )
                Button(
                    onClick = { viewModel.trackLink() },
                    enabled = !viewModel.isTrackingLink,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (viewModel.isTrackingLink) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Icon(Icons.Default.Link, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Track link click")
                }
                ActionStatus(viewModel.actionStatuses["track"])
            }
        }
        item {
            SectionCard("In-app notifications", Icons.Default.RocketLaunch) {
                Text(
                    "Load popup widgets for this account, choose one, or enter a widget ID. Tab views also emit Ortto screen events.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FilledTonalButton(
                    onClick = viewModel::loadWidgets,
                    enabled = !viewModel.isLoadingWidgets,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (viewModel.isLoadingWidgets) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Icon(Icons.Default.Refresh, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Load widgets")
                }
                viewModel.widgets.forEach { widget ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { viewModel.showWidget(widget.id) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(widget.id, fontFamily = FontFamily.Monospace, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(widget.type, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.ChevronRight, null)
                    }
                }
                OutlinedTextField(
                    value = viewModel.widgetId,
                    onValueChange = { viewModel.widgetId = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Widget ID") },
                    singleLine = true,
                )
                Button(onClick = { viewModel.showWidget() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Show widget")
                }
                ActionStatus(viewModel.actionStatuses["widget"] ?: viewModel.actionStatuses["loadWidgets"])
            }
        }
        item {
            SectionCard("Actions", Icons.Default.RocketLaunch) {
                ActionButton(
                    "Identify contact",
                    "Attach the signed-in email to the SDK session.",
                    Icons.Default.Person,
                    viewModel.isIdentifying,
                    viewModel.actionStatuses["identify"],
                    viewModel::identifyCurrentContact,
                )
                ActionButton(
                    "Request and register FCM token",
                    "Ask Android for permission, mint a Firebase token, and forward it to Ortto.",
                    Icons.Default.NotificationsActive,
                    viewModel.isRegistering,
                    viewModel.actionStatuses["register"],
                ) { viewModel.requestPushRegistration(requestPermission) }
                ActionButton(
                    "Redispatch SDK token",
                    "Ask the SDK to process its cached Firebase token again.",
                    Icons.Default.Sync,
                    false,
                    viewModel.actionStatuses["redispatch"],
                    viewModel::redispatchToken,
                )
                ActionButton(
                    "Refresh permission",
                    "Read app and notification-channel permission state from Android.",
                    Icons.Default.Refresh,
                    false,
                    viewModel.actionStatuses["permission"],
                    viewModel::refreshPermission,
                )
            }
        }
        item { Spacer(Modifier.height(18.dp)) }
    }
}

@Composable
private fun LogScreen(entries: List<DemoLogEntry>) {
    val listState = rememberLazyListState()
    LaunchedEffect(entries.size) {
        if (entries.isNotEmpty()) listState.animateScrollToItem(entries.lastIndex)
    }
    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(OrttoColors.Matrix),
        state = listState,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
    ) {
        if (entries.isEmpty()) {
            item {
                Text(
                    "demo@push-demo % waiting for log events\nortto@android-sdk % idle",
                    color = OrttoColors.MatrixGreen,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                )
            }
        }
        itemsIndexed(entries) { _, entry ->
            SelectionContainer {
                Text(
                    buildString {
                        append(entry.source)
                        append(if (entry.source == "ortto") "@android-sdk" else "@push-demo")
                        append(" [${entry.timestamp}] % ")
                        if (entry.level != "info") append("${entry.level} ")
                        append(entry.message)
                    },
                    color = when (entry.level) {
                        "err" -> Color(0xFFFF756E)
                        "warn" -> Color(0xFFFFC74F)
                        "debug" -> Color(0xFF789581)
                        else -> if (entry.source == "ortto") Color(0xFF8DDFFF) else Color(0xFFDDEAE0)
                    },
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(vertical = 3.dp),
                )
            }
        }
    }
}

@Composable
private fun TechnicalDetails(viewModel: DemoViewModel) {
    LazyColumn(
        Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text("Technical details", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Text("Everything support needs to reproduce the current integration state.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            SectionCard("SDK", Icons.Default.BugReport) {
                DetailRow("Target", "Android FCM")
                DetailRow("SDK version", com.ortto.messaging.BuildConfig.SDK_VERSION)
                DetailRow("Endpoint", DemoConfig.endpoint.ifBlank { "Missing" }, copyValue = DemoConfig.endpoint)
                DetailRow("App key", if (DemoConfig.appKey.isBlank()) "Missing" else "Configured")
                DetailRow("Firebase config", if (BuildConfig.HAS_GOOGLE_SERVICES) "google-services.json" else "Missing")
            }
        }
        item {
            SectionCard("Session", Icons.Default.Person) {
                DetailRow("Email", viewModel.signedInEmail)
                DetailRow("Session", DemoLogic.short(viewModel.sessionId), copyValue = viewModel.sessionId, monospace = true)
                DetailRow("FCM token", DemoLogic.short(viewModel.fcmToken), copyValue = viewModel.fcmToken, monospace = true)
                DetailRow("Fingerprint", DemoLogic.tokenFingerprint(viewModel.fcmToken) + "…", monospace = true)
            }
        }
        item {
            SectionCard("Notifications", Icons.Default.NotificationsActive) {
                DetailRow("Package", BuildConfig.APPLICATION_ID)
                DetailRow("Deep-link scheme", DemoConfig.deepLinkScheme)
                DetailRow("Permission", viewModel.permissionStatus)
                DetailRow("Channel", com.ortto.messaging.PushNotificationHandler.PUSH_ACTION)
            }
        }
        if (viewModel.configurationIssues.isNotEmpty()) {
            item {
                SectionCard("Checks", Icons.Default.Warning) {
                    viewModel.configurationIssues.forEach { IssueRow(it) }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Icon(icon, null, Modifier.padding(8.dp).size(20.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(Modifier.width(10.dp))
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            content()
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    copyValue: String? = null,
    monospace: Boolean = false,
) {
    val context = LocalContext.current
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(0.42f), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value.ifBlank { "None" },
            modifier = Modifier.weight(0.48f),
            fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (!copyValue.isNullOrBlank()) {
            IconButton(onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText(label, copyValue))
            }) {
                Icon(Icons.Default.ContentCopy, "Copy $label", Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun IssueRow(issue: ConfigurationIssue) {
    val tint = if (issue.critical) OrttoColors.Coral else OrttoColors.Orange
    Row(
        Modifier
            .fillMaxWidth()
            .background(tint.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(if (issue.critical) Icons.Default.Error else Icons.Default.Warning, null, tint = tint)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(issue.title, fontWeight = FontWeight.SemiBold, color = tint)
            Text(issue.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ActionButton(
    title: String,
    detail: String,
    icon: ImageVector,
    loading: Boolean,
    status: DemoActionStatus?,
    action: () -> Unit,
) {
    Card(
        onClick = action,
        enabled = !loading,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            if (loading) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
            else Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                ActionStatus(status)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
        }
    }
}

@Composable
private fun ActionStatus(status: DemoActionStatus?) {
    AnimatedVisibility(
        visible = status != null,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut(),
    ) {
        status ?: return@AnimatedVisibility
        val color = when (status.tone) {
            DemoTone.Success -> OrttoColors.Green
            DemoTone.Warning -> OrttoColors.Orange
            DemoTone.Blocked -> OrttoColors.Coral
            DemoTone.Working -> OrttoColors.Lilac
            DemoTone.Ready -> MaterialTheme.colorScheme.onSurfaceVariant
        }
        Row(Modifier.padding(top = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                when (status.tone) {
                    DemoTone.Success -> Icons.Default.CheckCircle
                    DemoTone.Warning, DemoTone.Blocked -> Icons.Default.Warning
                    DemoTone.Working -> Icons.Default.Sync
                    DemoTone.Ready -> Icons.Default.Info
                },
                null,
                Modifier.size(14.dp),
                tint = color,
            )
            Spacer(Modifier.width(5.dp))
            Text(status.text, style = MaterialTheme.typography.labelMedium, color = color)
        }
    }
}
