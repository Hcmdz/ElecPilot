package com.HcmDz.ElecPilot.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.HcmDz.ElecPilot.R
import com.HcmDz.ElecPilot.data.db.MotorEntity
import com.HcmDz.ElecPilot.data.db.PlcEntity

data class StatisticsData(
    val totalCount: Int,
    val byAtelier: Map<String, Int>,
    val byType: Map<String, Int>,
    val byTypesDeparts: Map<String, Int>,
    val byTGBT: Map<String, Int>
)

fun computeStatistics(motors: List<MotorEntity>, naLabel: String = "N/A"): StatisticsData {
    return StatisticsData(
        totalCount = motors.size,
        byAtelier = motors.groupBy { it.atelier.ifBlank { naLabel } }.mapValues { it.value.size }.toSortedMap(),
        byType = motors.groupBy { it.types.ifBlank { naLabel } }.mapValues { it.value.size }.toSortedMap(),
        byTypesDeparts = motors.groupBy { it.typesDeparts.ifBlank { naLabel } }.mapValues { it.value.size }.toSortedMap(),
        byTGBT = motors.groupBy { it.tgbt.ifBlank { naLabel } }.mapValues { it.value.size }.toSortedMap()
    )
}

fun computePlcStatistics(plcList: List<PlcEntity>, naLabel: String = "N/A"): StatisticsData {
    return StatisticsData(
        totalCount = plcList.size,
        byAtelier = plcList.groupBy { it.atelier.ifBlank { naLabel } }.mapValues { it.value.size }.toSortedMap(),
        byType = plcList.groupBy { it.dp.ifBlank { naLabel } }.mapValues { it.value.size }.toSortedMap(),
        byTypesDeparts = plcList.groupBy { it.carte.ifBlank { naLabel } }.mapValues { it.value.size }.toSortedMap(),
        byTGBT = plcList.groupBy { it.position.ifBlank { naLabel } }.mapValues { it.value.size }.toSortedMap()
    )
}

@Composable
private fun chartColor(index: Int): Color {
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer,
        MaterialTheme.colorScheme.errorContainer,
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.outline,
    )
    return colors[index % colors.size]
}

@Composable
fun StatisticsDialog(
    stats: StatisticsData,
    totalLabel: String = stringResource(R.string.statistics_motors),
    sectionLabels: List<String> = listOf(stringResource(R.string.statistics_by_atelier), stringResource(R.string.statistics_by_type), stringResource(R.string.statistics_by_type_depart), stringResource(R.string.statistics_by_tgbt)),
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.statistics_title),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                StatCard(totalLabel, "${stats.totalCount}")
                Spacer(modifier = Modifier.height(12.dp))
                PieChartSection(sectionLabels.getOrElse(0) { stringResource(R.string.statistics_by_atelier) }, stats.byAtelier)
                Spacer(modifier = Modifier.height(12.dp))
                PieChartSection(sectionLabels.getOrElse(1) { stringResource(R.string.statistics_by_type) }, stats.byType)
                Spacer(modifier = Modifier.height(12.dp))
                PieChartSection(sectionLabels.getOrElse(2) { stringResource(R.string.statistics_by_type_depart) }, stats.byTypesDeparts)
                Spacer(modifier = Modifier.height(12.dp))
                PieChartSection(sectionLabels.getOrElse(3) { stringResource(R.string.statistics_by_tgbt) }, stats.byTGBT)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
private fun PieChartSection(title: String, data: Map<String, Int>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(6.dp))
        if (data.isEmpty()) {
            Text(stringResource(R.string.statistics_no_data), fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            return
        }
        val surfaceColor = MaterialTheme.colorScheme.surface
        val chartColors = (0 until maxOf(data.size, 6)).map { chartColor(it) }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Canvas(
                modifier = Modifier.size(120.dp)
            ) {
                val total = data.values.sum().toFloat()
                var startAngle = -90f
                data.values.forEachIndexed { index, count ->
                    val sweep = (count / total) * 360f
                    drawArc(
                        color = chartColors[index % chartColors.size],
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = true,
                        style = Fill
                    )
                    startAngle += sweep
                }
                drawCircle(surfaceColor, radius = size.minDimension / 4)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                data.entries.take(6).forEachIndexed { index, (key, count) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Canvas(modifier = Modifier.size(8.dp)) {
                                drawCircle(chartColors[index % chartColors.size])
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(key, fontSize = 11.sp, maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface)
                        }
                        Text("$count", fontSize = 11.sp, fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
                if (data.size > 6) {
                    Text(stringResource(R.string.statistics_more, data.size - 6), fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary)
        }
    }
}
