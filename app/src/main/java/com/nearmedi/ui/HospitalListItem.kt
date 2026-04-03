package com.nearmedi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nearmedi.data.model.Hospital

@Composable
fun HospitalListItem(
    hospital: Hospital,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    onCallPhone: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val isPharmacy = hospital.type.contains("약국")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp,
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            // Category icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (isPharmacy)
                            MaterialTheme.colorScheme.tertiaryContainer
                        else
                            MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isPharmacy) Icons.Default.LocalPharmacy
                    else Icons.Default.LocalHospital,
                    contentDescription = if (isPharmacy) "약국" else "병원",
                    tint = if (isPharmacy)
                        MaterialTheme.colorScheme.onTertiaryContainer
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = hospital.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (hospital.type.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        TypeBadge(type = hospital.type)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = hospital.address,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (hospital.tel.isNotEmpty()) {
                    Text(
                        text = hospital.tel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .clickable { onCallPhone(hospital.tel) }
                            .defaultMinSize(minHeight = 36.dp)
                            .wrapContentHeight(Alignment.CenterVertically),
                    )
                }
            }

            Text(
                text = formatDistance(hospital.distance),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun TypeBadge(type: String) {
    val isPharmacy = type.contains("약국")
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = if (isPharmacy)
            MaterialTheme.colorScheme.tertiaryContainer
        else
            MaterialTheme.colorScheme.primaryContainer,
    ) {
        Text(
            text = type,
            style = MaterialTheme.typography.labelSmall,
            color = if (isPharmacy)
                MaterialTheme.colorScheme.onTertiaryContainer
            else
                MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

private fun formatDistance(meters: Float): String {
    return if (meters < 1000) {
        "${meters.toInt()}m"
    } else {
        String.format("%.1fkm", meters / 1000)
    }
}
