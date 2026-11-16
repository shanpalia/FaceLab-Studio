package com.shanpalia.facelab

import android.content.Intent
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.VideoView
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.BufferedInputStream
import java.io.BufferedOutputStream

private val Mint = Color(0xFF24C99A)
private val Ink = Color(0xFF14232D)
private val Muted = Color(0xFF71818A)
private val Page = Color(0xFFF6F9F8)
private const val APP_VERSION = "1.0.0"
private const val UPDATE_MANIFEST = "https://raw.githubusercontent.com/shanpalia/WebsitePaliaAPK_V.2/main/facelab-studio.json"
private const val API_BASE = "https://facelab-studio-api.shanpalia786.workers.dev"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent { FaceLabApp(onExit = { finish() }) }
    }
}

@Composable
fun FaceLabApp(onExit: () -> Unit) {
    val context = LocalContext.current
    var faceUri by remember { mutableStateOf<Uri?>(null) }
    var videoUri by remember { mutableStateOf<Uri?>(null) }
    var screen by remember { mutableStateOf("Home") }
    var showExit by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf("Choose your face photo and target video.") }
    var updateState by remember { mutableStateOf("Tap Check for Updates to check PaliaAPK HUB.") }
    var latestVersion by remember { mutableStateOf<String?>(null) }
    var updateUrl by remember { mutableStateOf<String?>(null) }
    var checking by remember { mutableStateOf(false) }
    var processing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    BackHandler(enabled = true) {
        if (showExit) showExit = false
        else if (screen != "Home") screen = "Home"
        else showExit = true
    }

    val facePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        faceUri = it
        if (it != null) notice = "Face photo selected."
    }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        videoUri = it
        if (it != null) notice = "Target video selected."
    }

    fun checkForUpdate() {
        if (checking) return
        checking = true
        updateState = "Checking PaliaAPK HUB…"
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val connection = (URL(UPDATE_MANIFEST).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 10000
                        readTimeout = 10000
                        requestMethod = "GET"
                    }
                    try {
                        if (connection.responseCode !in 200..299) error("Update service returned HTTP ${connection.responseCode}")
                        JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
                    } finally { connection.disconnect() }
                }
            }
            checking = false
            result.onSuccess { json ->
                latestVersion = json.optString("version").takeIf { it.isNotBlank() }
                updateUrl = json.optString("apk_url").takeIf { it.startsWith("https://") }
                val latest = latestVersion
                updateState = when {
                    latest == null -> "Update information is incomplete."
                    latest != APP_VERSION && updateUrl != null -> "Version $latest is available."
                    latest != APP_VERSION -> "Version $latest is listed, but its download link is not ready."
                    else -> "You're up to date — version $APP_VERSION."
                }
            }.onFailure { updateState = "Could not check updates. Please try again later." }
        }
    }

    MaterialTheme(colorScheme = lightColorScheme(
        primary = Mint, onPrimary = Color.White, background = Page, surface = Color.White,
        onSurface = Ink, secondary = Ink
    )) {
        Scaffold(
            containerColor = Page,
            bottomBar = {
                NavigationBar(containerColor = Color.White, tonalElevation = 5.dp) {
                    listOf("Home", "Creations", "Tools", "Settings").forEach { item ->
                        val icon = when (item) {
                            "Home" -> Icons.Rounded.Home
                            "Creations" -> Icons.Rounded.Collections
                            "Tools" -> Icons.Rounded.Tune
                            else -> Icons.Rounded.Settings
                        }
                        NavigationBarItem(
                            selected = screen == item,
                            onClick = { screen = item },
                            icon = { Icon(icon, contentDescription = item) },
                            label = { Text(item) }
                        )
                    }
                }
            }
        ) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(
                        Brush.linearGradient(listOf(Color(0xFF102A35), Color(0xFF1D806B)))
                    ), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.FaceRetouchingNatural, null, tint = Mint, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("FaceLab Studio", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
                        Text("By PaliaAPK HUB", fontSize = 11.sp, color = Muted, fontWeight = FontWeight.SemiBold)
                    }
                    IconButton(onClick = { screen = "Settings" }) { Icon(Icons.Rounded.Settings, "Settings", tint = Ink) }
                }

                when (screen) {
                    "Settings" -> {
                        Text("Settings", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Ink)
                        SettingRow("Branding", "By PaliaAPK HUB")
                        SettingRow("Developer", "Developer by ShanPalia")
                        SettingRow("App version", APP_VERSION)
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(20.dp)) {
                            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.SystemUpdate, null, tint = Mint)
                                    Spacer(Modifier.width(8.dp))
                                    Text("App Updates", fontWeight = FontWeight.Bold, color = Ink)
                                }
                                Text(updateState, color = Muted, fontSize = 13.sp)
                                Button(onClick = { checkForUpdate() }, enabled = !checking, modifier = Modifier.fillMaxWidth()) {
                                    if (checking) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                                    else Text("Check for Updates")
                                }
                                if (latestVersion != null && latestVersion != APP_VERSION && updateUrl != null) {
                                    Text("New version: $latestVersion", color = Mint, fontWeight = FontWeight.Bold)
                                    OutlinedButton(onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl))
                                        // Opens the official PaliaAPK HUB APK download URL.
                                        context.startActivity(intent)
                                    }, modifier = Modifier.fillMaxWidth()) { Text("Download Update") }
                                }
                            }
                        }
                        SettingRow("Ads", "AdMob-ready • Ads not enabled yet")
                        SettingRow("Privacy", "Media access uses Android system picker")
                        Text("Developer by ShanPalia", color = Muted, fontSize = 12.sp)
                    }
                    "Creations" -> {
                        Text("My Creations", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Ink)
                        EmptyPanel("Your processed videos will appear here.")
                    }
                    "Tools" -> {
                        Text("AI Tools", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Ink)
                        EmptyPanel("More AI tools will be added here.")
                        Text("Rewarded Ads are reserved for earning free credits. AdMob will be connected after your account is ready.", color = Muted, fontSize = 12.sp)
                    }
                    else -> {
                        Text("Create your face swap", fontSize = 23.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Text("Choose a source photo and a target video.", fontSize = 13.sp, color = Muted)
                        Box(
                            Modifier.fillMaxWidth().height(165.dp).clip(RoundedCornerShape(24.dp))
                                .background(Brush.linearGradient(listOf(Color(0xFF0C1D28), Color(0xFF17483F)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Rounded.FaceRetouchingNatural, null, tint = Mint, modifier = Modifier.size(46.dp))
                                Text("AI FACE PREVIEW", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                                Text("Your selected media will be used for processing", color = Color(0xFFB9D4CE), fontSize = 11.sp)
                            }
                            Surface(Modifier.align(Alignment.TopEnd).padding(12.dp), color = Color(0x3324C99A), shape = CircleShape) {
                                Text("AI STUDIO", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = Mint, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MediaCard("1", "Source Face", faceUri != null, Modifier.weight(1f)) { facePicker.launch("image/*") }
                            MediaCard("2", "Target Video", videoUri != null, Modifier.weight(1f)) { videoPicker.launch("video/*") }
                        }
                        if (faceUri != null || videoUri != null) {
                            Text("Selected Media Preview", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                if (faceUri != null) {
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(18.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White)
                                    ) {
                                        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("Source Photo", color = Ink, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            AsyncImage(
                                                model = faceUri,
                                                contentDescription = "Selected source face photo preview",
                                                modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                                if (videoUri != null) {
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(18.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White)
                                    ) {
                                        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("Target Video", color = Ink, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            AndroidView(
                                                factory = { viewContext ->
                                                    VideoView(viewContext).apply {
                                                        setVideoURI(videoUri)
                                                        setOnPreparedListener { player ->
                                                            player.isLooping = true
                                                            start()
                                                        }
                                                    }
                                                },
                                                update = { player ->
                                                    if (player.tag != videoUri.toString()) {
                                                        player.tag = videoUri.toString()
                                                        player.setVideoURI(videoUri)
                                                        player.setOnPreparedListener { prepared ->
                                                            prepared.isLooping = true
                                                            player.start()
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Button(
                            onClick = {
                                if (!processing && faceUri != null && videoUri != null) {
                                    processing = true
                                    notice = "Preparing secure upload…"
                                    scope.launch {
                                        val result = runCatching {
                                            uploadAndRequestProcessing(context, faceUri!!, videoUri!!) { message -> scope.launch { notice = message } }
                                        }
                                        notice = result.fold(
                                            onSuccess = { it },
                                            onFailure = { "Upload failed: ${it.message ?: "Please try again."}" }
                                        )
                                        processing = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            enabled = faceUri != null && videoUri != null && !processing,
                            shape = RoundedCornerShape(17.dp)
                        ) {
                            if (processing) CircularProgressIndicator(Modifier.size(19.dp), color = Color.White, strokeWidth = 2.dp)
                            else Icon(Icons.Rounded.AutoFixHigh, null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (processing) "Uploading…" else "Swap Face", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Text(notice, color = Muted, fontSize = 12.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Security, null, tint = Mint, modifier = Modifier.size(17.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Use only faces and videos you own or have permission to edit.", color = Muted, fontSize = 11.sp)
                        }
                        Text("Developer by ShanPalia", Modifier.fillMaxWidth().padding(bottom = 12.dp), color = Muted, fontSize = 11.sp)
                    }
                }
            }
        }
    }

    if (showExit) {
        AlertDialog(
            onDismissRequest = { showExit = false },
            title = { Text("Exit FaceLab Studio?") },
            text = { Text("Do you want to close the app?") },
            confirmButton = { TextButton(onClick = onExit) { Text("Exit") } },
            dismissButton = { TextButton(onClick = { showExit = false }) { Text("Stay") } }
        )
    }
}

@Composable
private fun SettingRow(title: String, value: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.fillMaxWidth().padding(15.dp)) {
            Text(title, color = Ink, fontWeight = FontWeight.Bold)
            Text(value, color = Muted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun EmptyPanel(message: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
        Text(message, Modifier.fillMaxWidth().padding(18.dp), color = Muted)
    }
}

@Composable
private fun MediaCard(number: String, title: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier.clip(RoundedCornerShape(20.dp)).background(Color.White)
            .border(1.dp, if (selected) Mint else Color(0xFFE5ECE9), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick).padding(13.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(number, color = Mint, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            Spacer(Modifier.weight(1f))
            Icon(if (selected) Icons.Rounded.CheckCircle else Icons.Rounded.AddCircleOutline, null, tint = Mint)
        }
        Icon(if (title == "Source Face") Icons.Rounded.Person else Icons.Rounded.VideoLibrary, null, tint = Ink, modifier = Modifier.size(27.dp))
        Text(title, color = Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(if (selected) "Selected" else "Tap to choose", color = if (selected) Mint else Muted, fontSize = 11.sp)
    }
}


private suspend fun uploadAndRequestProcessing(
    context: Context,
    faceUri: Uri,
    videoUri: Uri,
    onProgress: (String) -> Unit
): String = withContext(Dispatchers.IO) {
    fun request(url: String, method: String, token: String? = null, body: ByteArray? = null, contentType: String? = null): JSONObject {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 20000
            readTimeout = 60000
            requestMethod = method
            if (token != null) setRequestProperty("Authorization", "Bearer $token")
            if (contentType != null) setRequestProperty("Content-Type", contentType)
            if (body != null) {
                doOutput = true
                setFixedLengthStreamingMode(body.size)
                outputStream.use { it.write(body) }
            }
        }
        try {
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            val json = runCatching { JSONObject(text) }.getOrElse { JSONObject().put("error", text) }
            if (code !in 200..299 && code != 503) error(json.optString("error", "HTTP $code"))
            return json.put("_httpCode", code)
        } finally { connection.disconnect() }
    }

    fun cacheUri(uri: Uri, label: String): File {
        val file = File(context.cacheDir, "facelab-$label-${System.currentTimeMillis()}")
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Could not read selected $label." }
            FileOutputStream(file).use { output -> input.copyTo(output) }
        }
        return file
    }

    var faceFile: File? = null
    var videoFile: File? = null
    try {
        // Do not upload personal media unless the server confirms a real AI adapter is ready.
        val health = request("$API_BASE/health", "GET")
        require(health.optString("ai") == "ready") {
            "AI face-swap processing is not enabled yet. Your photo and video were not uploaded."
        }
        onProgress("Preparing media files…")
        faceFile = cacheUri(faceUri, "face")
        videoFile = cacheUri(videoUri, "video")
        require(faceFile.length() <= 12L * 1024 * 1024) { "Face photo must be under 12 MB." }
        require(videoFile.length() <= 80L * 1024 * 1024) { "Video must be under 80 MB." }

        onProgress("Creating secure upload job…")
        val created = request("$API_BASE/v1/jobs", "POST", body="{}".toByteArray(), contentType="application/json")
        val jobId = created.getString("jobId")
        val token = created.getString("uploadToken")

        fun upload(file: File, kind: String, mime: String) {
            onProgress("Uploading $kind to private Cloudflare R2…")
            val connection = (URL("$API_BASE/v1/jobs/$jobId/$kind").openConnection() as HttpURLConnection).apply {
                connectTimeout = 20000
                readTimeout = 120000
                requestMethod = "PUT"
                doOutput = true
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Content-Type", mime)
                setFixedLengthStreamingMode(file.length())
            }
            try {
                FileInputStream(file).use { input -> connection.outputStream.use { output -> input.copyTo(output) } }
                val code = connection.responseCode
                val text = (if (code in 200..299) connection.inputStream else connection.errorStream)?.bufferedReader()?.use { it.readText() }.orEmpty()
                if (code !in 200..299) error(JSONObject(text).optString("error", "Upload failed ($code)"))
            } finally { connection.disconnect() }
        }

        val faceMime = context.contentResolver.getType(faceUri) ?: "image/jpeg"
        val videoMime = context.contentResolver.getType(videoUri) ?: "video/mp4"
        upload(faceFile, "face", faceMime)
        upload(videoFile, "video", videoMime)
        onProgress("Files uploaded. Checking free AI provider…")
        val processed = request("$API_BASE/v1/jobs/$jobId/process", "POST", token, "{}".toByteArray(), "application/json")
        val status = processed.optString("status")
        if (status == "waiting_for_ai_provider") {
            "Your photo and video are uploaded securely. Free AI GPU processing is not connected yet, so no face-swapped video was created. Job ID: $jobId"
        } else {
            processed.optString("message", "Job status: $status")
        }
    } finally {
        faceFile?.delete()
        videoFile?.delete()
    }
}
