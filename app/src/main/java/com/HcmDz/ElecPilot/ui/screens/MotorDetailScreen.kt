package com.HcmDz.ElecPilot.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.HcmDz.ElecPilot.data.db.MotorEntity
import com.HcmDz.ElecPilot.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotorDetailScreen(
    motor: MotorEntity,
    onBack: () -> Unit,
    onCopyValue: (String) -> Unit = {},
    onShare: () -> Unit = {},
    onDuplicate: () -> Unit = {},
    onToggleFavorite: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.detail_depart_title), fontSize = 17.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.detail_back_cd))
                    }
                },
                actions = {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            if (motor.favorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = stringResource(if (motor.favorite) R.string.detail_favorite_remove_cd else R.string.detail_favorite_add_cd),
                            tint = if (motor.favorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = onDuplicate) {
                        Icon(Icons.Default.FileCopy, contentDescription = stringResource(R.string.detail_duplicate_cd), tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = onShare) {
                        Icon(Icons.Default.Share, contentDescription = stringResource(R.string.detail_share_cd), tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = motor.designation.ifBlank { stringResource(R.string.detail_no_designation) },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${motor.atelier} · ${motor.tgbt}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            InfoSection(
                icon = Icons.Default.Factory,
                title = stringResource(R.string.detail_section_localisation),
                items = listOf(
                    stringResource(R.string.detail_field_atelier) to motor.atelier,
                    stringResource(R.string.detail_field_tgbt) to motor.tgbt,
                    stringResource(R.string.detail_field_position_tgbt) to motor.positionTGBT
                ),
                onCopyValue = onCopyValue
            )
            InfoSection(
                icon = Icons.Default.Bolt,
                title = stringResource(R.string.detail_section_electrical),
                items = listOf(
                    stringResource(R.string.detail_field_power) to if (motor.puissanceKW.isBlank()) "-" else "${motor.puissanceKW} kW",
                    stringResource(R.string.detail_field_depart_type) to motor.typesDeparts,
                    stringResource(R.string.detail_field_item) to motor.item
                ),
                onCopyValue = onCopyValue
            )
            InfoSection(
                icon = Icons.Default.Category,
                title = stringResource(R.string.detail_section_type),
                items = listOf(
                    stringResource(R.string.detail_field_motor_type) to motor.types
                ),
                onCopyValue = onCopyValue
            )
            InfoSection(
                icon = Icons.Default.Cable,
                title = stringResource(R.string.detail_section_cable),
                items = listOf(
                    stringResource(R.string.detail_field_cable) to motor.cable,
                    stringResource(R.string.detail_field_cable_type) to motor.typeCable
                ),
                onCopyValue = onCopyValue
            )
        }
    }
}

@Composable
private fun InfoSection(
    icon: ImageVector,
    title: String,
    items: List<Pair<String, String>>,
    onCopyValue: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            items.forEachIndexed { index, (label, value) ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                }
                DetailRow(label = label, value = value, onCopyValue = onCopyValue)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DetailRow(label: String, value: String, onCopyValue: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            )
            .combinedClickable(
                onClick = { onCopyValue("$label: ${value.ifBlank { "-" }}") },
                onLongClick = { onCopyValue("$label: ${value.ifBlank { "-" }}") }
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value.ifBlank { "-" },
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
