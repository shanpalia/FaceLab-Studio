package com.shanpalia.facelab

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Mint = Color(0xFF24C99A)
private val Ink = Color(0xFF14232D)
private val Muted = Color(0xFF71818A)
private val Page = Color(0xFFF6F9F8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { FaceLabApp() }
    }
}

@Composable
fun FaceLabApp() {
    var faceUri by remember { mutableStateOf<Uri?>(null) }
    var videoUri by remember { mutableStateOf<Uri?>(null) }
    var notice by remember { mutableStateOf("Choose a face photo and a target video to get started.") }
    val facePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        faceUri = it
        if (it != null) notice = "Face photo selected. Now choose your target video."
    }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        videoUri = it
        if (it != null) notice = "Video selected. Review your media before processing."
    }

    MaterialTheme(colorScheme = lightColorScheme(
        primary = Mint, onPrimary = Color.White, background = Page, surface = Color.White,
        onSurface = Ink, secondary = Ink
    )) {
        Scaffold(containerColor = Page, bottomBar = { BottomBar() }) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(46.dp).clip(RoundedCornerShape(15.dp)).background(
                        Brush.linearGradient(listOf(Color(0xFF102A35), Color(0xFF1D806B)))
                    ), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.FaceRetouchingNatural, null, tint = Mint, modifier = Modifier.size(29.dp))
                    }
                    Spacer(Modifier.width(11.dp))
                    Column(Modifier.weight(1f)) {
                        Text("FaceLab Studio", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
                        Text("By PaliaAPK HUB", fontSize = 11.sp, color = Muted, fontWeight = FontWeight.SemiBold)
                    }
                    Icon(Icons.Rounded.Settings, "Settings", tint = Ink)
                }

                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("Create your face swap", fontSize = 23.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Text("Your media stays on your device in this starter build.", fontSize = 13.sp, color = Muted)
                }

                // Cinematic preview panel from the approved Neon AI visual direction.
                Box(
                    Modifier.fillMaxWidth().height(178.dp).clip(RoundedCornerShape(24.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF0C1D28), Color(0xFF17483F)))),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Icon(Icons.Rounded.FaceRetouchingNatural, null, tint = Mint, modifier = Modifier.size(48.dp))
                        Text("AI FACE PREVIEW", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Text("Select media to prepare your preview", color = Color(0xFFB9D4CE), fontSize = 12.sp)
                    }
                    Surface(Modifier.align(Alignment.TopEnd).padding(13.dp), color = Color(0x3324C99A), shape = CircleShape) {
                        Text("AI STUDIO", Modifier.padding(horizontal = 11.dp, vertical = 6.dp), color = Mint, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MediaCard("1", "Source Face", "Choose a photo", faceUri != null, Modifier.weight(1f)) { facePicker.launch("image/*") }
                    MediaCard("2", "Target Video", "Choose a video", videoUri != null, Modifier.weight(1f)) { videoPicker.launch("video/*") }
                }

                Button(
                    onClick = { notice = "The face-swap processing engine is not connected yet. This build is the UI and media-selection foundation." },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = faceUri != null && videoUri != null,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Mint, disabledContainerColor = Color(0xFFB8D9CF))
                ) {
                    Icon(Icons.Rounded.AutoFixHigh, null)
                    Spacer(Modifier.width(9.dp))
                    Text("Swap Face", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Text(notice, color = Muted, fontSize = 12.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Security, null, tint = Mint, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Use only faces and videos you own or have permission to edit.", color = Muted, fontSize = 11.sp)
                }
                Spacer(Modifier.weight(1f))
                Text("Developer by ShanPalia", Modifier.fillMaxWidth().padding(bottom = 10.dp), color = Muted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun MediaCard(number: String, title: String, subtitle: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier.clip(RoundedCornerShape(20.dp)).background(Color.White)
            .border(1.dp, if (selected) Mint else Color(0xFFE5ECE9), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(number, color = Mint, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            Spacer(Modifier.weight(1f))
            Icon(if (selected) Icons.Rounded.CheckCircle else Icons.Rounded.AddCircleOutline, null, tint = Mint)
        }
        Icon(if (title == "Source Face") Icons.Rounded.Person else Icons.Rounded.VideoLibrary,
            null, tint = Ink, modifier = Modifier.size(28.dp))
        Text(title, color = Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(if (selected) "Selected" else subtitle, color = if (selected) Mint else Muted, fontSize = 11.sp)
    }
}

@Composable
private fun BottomBar() {
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Rounded.Home, null) }, label = { Text("Home") })
        NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Rounded.Collections, null) }, label = { Text("Creations") })
        NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Rounded.Tune, null) }, label = { Text("Tools") })
        NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Rounded.Settings, null) }, label = { Text("Settings") })
    }
}
