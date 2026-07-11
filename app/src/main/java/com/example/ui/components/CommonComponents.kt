package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskEntity

// Colors representing each Category with Emojis
object CategoryColors {
    val Personal = Color(0xFF3B82F6)   // Blue
    val Family = Color(0xFF10B981)     // Green
    val Office = Color(0xFF8B5CF6)     // Purple
    val Study = Color(0xFFEC4899)      // Pink
    val Team = Color(0xFF6366F1)       // Indigo
    val Meeting = Color(0xFFF97316)    // Orange
    val Event = Color(0xFFEAB308)      // Yellow
    val Shopping = Color(0xFF14B8A6)   // Teal
    val Health = Color(0xFFEF4444)     // Red
    val Travel = Color(0xFF06B6D4)     // Cyan
    val Business = Color(0xFF6B7280)   // Gray
    val Bills = Color(0xFF84CC16)      // Lime
    val Appointment = Color(0xFFD946EF) // Fuchsia
    val Others = Color(0xFF64748B)     // Slate

    fun getCategoryDetails(category: String): Pair<Color, String> {
        return when (category) {
            "Personal" -> Personal to "🏠 Personal"
            "Family" -> Family to "👨‍👩‍👧 Family"
            "Office", "Work" -> Office to "💼 Office"
            "Study" -> Study to "📚 Study"
            "Team" -> Team to "👥 Team"
            "Meeting" -> Meeting to "📅 Meeting"
            "Event" -> Event to "🎉 Event"
            "Shopping" -> Shopping to "🛒 Shopping"
            "Health" -> Health to "🏥 Health"
            "Travel" -> Travel to "✈️ Travel"
            "Business" -> Business to "👔 Business"
            "Bills" -> Bills to "💰 Bills"
            "Appointment" -> Appointment to "⏰ Appointment"
            else -> Others to "⚙️ $category"
        }
    }

    fun getColor(category: String): Color {
        return getCategoryDetails(category).first
    }
}

@Composable
fun CategoryChip(category: String, modifier: Modifier = Modifier) {
    val (color, label) = CategoryColors.getCategoryDetails(category)
    Surface(
        color = color.copy(alpha = 0.12f),
        contentColor = color,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun PriorityBadge(priority: String, modifier: Modifier = Modifier) {
    val (color, text) = when (priority) {
        "High" -> Color(0xFFEF4444) to "High"
        "Medium" -> Color(0xFFF59E0B) to "Medium"
        else -> Color(0xFF10B981) to "Low"
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        contentColor = color,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun ShimmerPlaceholder(
    height: Dp,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp)
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(10f, 10f),
        end = Offset(translateAnim, translateAnim)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(brush)
    )
}

@Composable
fun AnimatedPieChart(
    data: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    val total = data.values.sum()
    if (total == 0) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text("No data to show", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    val slices = data.entries.toList()
    val colors = slices.map { CategoryColors.getColor(it.key) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Canvas(modifier = Modifier.size(130.dp)) {
            var startAngle = -90f
            slices.forEachIndexed { index, entry ->
                val sweepAngle = (entry.value.toFloat() / total) * 360f * animatedProgress.value
                drawArc(
                    color = colors[index],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    size = Size(size.width, size.height),
                    style = Stroke(width = 24.dp.toPx())
                )
                startAngle += sweepAngle
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(start = 16.dp)
        ) {
            slices.forEachIndexed { index, entry ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(colors[index], CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${entry.key} (${(entry.value.toFloat() / total * 100).toInt()}%)",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedBarChart(
    weeklyData: List<Int>, // Mon to Sun completion count
    modifier: Modifier = Modifier
) {
    val maxVal = weeklyData.maxOrNull() ?: 1
    val limit = if (maxVal == 0) 1 else maxVal

    val animatedHeight = remember { Animatable(0f) }
    LaunchedEffect(weeklyData) {
        animatedHeight.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    val days = listOf("M", "T", "W", "T", "F", "S", "S")

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Weekly Activity (Completed Tasks)",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            weeklyData.forEachIndexed { index, count ->
                val heightPercent = count.toFloat() / limit
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(0.8f)
                            .fillMaxWidth(0.4f)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight(heightPercent * animatedHeight.value)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                        )
                                    )
                                )
                                .align(Alignment.BottomCenter)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = days[index],
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    description: String,
    buttonText: String? = null,
    onButtonClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Beautiful procedural Canvas illustration instead of external files!
        Canvas(modifier = Modifier.size(120.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            // Draw a floating stylized clipboard
            drawRoundRect(
                color = Color(0xFF6366F1).copy(alpha = 0.1f),
                topLeft = Offset(center.x - 40.dp.toPx(), center.y - 50.dp.toPx()),
                size = Size(80.dp.toPx(), 90.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()),
            )
            // Clipboard header clip
            drawRoundRect(
                color = Color(0xFF4F46E5),
                topLeft = Offset(center.x - 20.dp.toPx(), center.y - 58.dp.toPx()),
                size = Size(40.dp.toPx(), 16.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
            )
            // Checklist lines
            drawLine(
                color = Color(0xFF818CF8),
                start = Offset(center.x - 24.dp.toPx(), center.y - 20.dp.toPx()),
                end = Offset(center.x + 24.dp.toPx(), center.y - 20.dp.toPx()),
                strokeWidth = 3.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF818CF8),
                start = Offset(center.x - 24.dp.toPx(), center.y),
                end = Offset(center.x + 10.dp.toPx(), center.y),
                strokeWidth = 3.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF818CF8),
                start = Offset(center.x - 24.dp.toPx(), center.y + 20.dp.toPx()),
                end = Offset(center.x + 18.dp.toPx(), center.y + 20.dp.toPx()),
                strokeWidth = 3.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            // Glowing bubble decoration
            drawCircle(
                color = Color(0xFF10B981).copy(alpha = 0.15f),
                radius = 16.dp.toPx(),
                center = Offset(center.x + 35.dp.toPx(), center.y - 35.dp.toPx())
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (buttonText != null && onButtonClick != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onButtonClick,
                modifier = Modifier.testTag("empty_state_action_button")
            ) {
                Text(text = buttonText)
            }
        }
    }
}

@Composable
fun FavoriteAndPinButtons(
    task: TaskEntity,
    onPinToggle: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        IconButton(onClick = onPinToggle) {
            Icon(
                imageVector = if (task.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                contentDescription = if (task.isPinned) "Unpin task" else "Pin task",
                tint = if (task.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onFavoriteToggle) {
            Icon(
                imageVector = if (task.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (task.isFavorite) "Remove from favorites" else "Add to favorites",
                tint = if (task.isFavorite) Color(0xFFEC4899) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
