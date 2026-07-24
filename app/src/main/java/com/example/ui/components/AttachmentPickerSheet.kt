package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SecurityGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentPickerSheet(
    onDismiss: () -> Unit,
    onSelectImage: (sampleUrl: String) -> Unit,
    onSelectVoiceNote: () -> Unit,
    onSelectDocument: (fileName: String, fileSize: String) -> Unit,
    onSelectLocation: (lat: Double, lng: Double, address: String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Send Encrypted Media Attachment",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AttachmentOptionItem(
                    icon = Icons.Default.Image,
                    label = "Photo Gallery",
                    color = CyberCyan,
                    onClick = {
                        val sampleImages = listOf(
                            "https://picsum.photos/seed/cyber1/600/400",
                            "https://picsum.photos/seed/security2/600/400",
                            "https://picsum.photos/seed/code3/600/400"
                        )
                        onSelectImage(sampleImages.random())
                        onDismiss()
                    }
                )

                AttachmentOptionItem(
                    icon = Icons.Default.CameraAlt,
                    label = "Camera",
                    color = EmeraldPrimary,
                    onClick = {
                        onSelectImage("https://picsum.photos/seed/camera_snapshot/600/400")
                        onDismiss()
                    }
                )

                AttachmentOptionItem(
                    icon = Icons.Default.Mic,
                    label = "Voice Note",
                    color = SecurityGold,
                    onClick = {
                        onSelectVoiceNote()
                        onDismiss()
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AttachmentOptionItem(
                    icon = Icons.Default.Description,
                    label = "Encrypted File",
                    color = Color(0xFFEC4899),
                    onClick = {
                        val docs = listOf(
                            "e2ee_protocol_v2.pdf" to "2.4 MB",
                            "public_key_bundle.asc" to "12 KB",
                            "security_audit_report.docx" to "1.1 MB"
                        )
                        val sample = docs.random()
                        onSelectDocument(sample.first, sample.second)
                        onDismiss()
                    }
                )

                AttachmentOptionItem(
                    icon = Icons.Default.LocationOn,
                    label = "Location Pin",
                    color = Color(0xFF8B5CF6),
                    onClick = {
                        onSelectLocation(37.7749, -122.4194, "San Francisco, CA (Encrypted Pin)")
                        onDismiss()
                    }
                )

                // Placeholder balancer spacing
                Spacer(modifier = Modifier.width(64.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AttachmentOptionItem(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(color.copy(alpha = 0.2f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
