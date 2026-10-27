package com.vrddhih.app.features.quests.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.vrddhih.app.R
import com.vrddhih.app.core.data.quest.QuestOccurrence
import com.vrddhih.app.core.data.quest.QuestScope
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihRadii
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.theme.VrddhihTextStyles
import com.vrddhih.app.core.ui.components.SystemPanel

@Composable
fun QuestDeleteDialog(
    target: QuestOccurrence,
    onConfirm: (QuestScope) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedScope by remember { mutableStateOf(QuestScope.OCCURRENCE) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        SystemPanel(
            modifier = modifier
                .padding(horizontal = VrddhihSpacing.lg)
                .widthIn(max = 440.dp)
                .fillMaxWidth(),
            glow = true,
            accent = VrddhihColors.Danger,
        ) {
            Text(
                text = stringResource(R.string.quest_delete_title),
                style = MaterialTheme.typography.titleLarge,
                color = VrddhihColors.Danger,
            )

            Spacer(Modifier.height(VrddhihSpacing.sm))

            Text(
                text = target.title,
                style = MaterialTheme.typography.bodyLarge,
                color = VrddhihColors.TextPrimary,
            )

            Spacer(Modifier.height(VrddhihSpacing.md))

            if (!target.isRecurring) {
                Text(
                    text = stringResource(R.string.quest_delete_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = VrddhihColors.TextSecondary,
                )
            } else {
                Text(
                    text = stringResource(R.string.quest_delete_scope_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = VrddhihColors.TextSecondary,
                )
                Spacer(Modifier.height(VrddhihSpacing.sm))

                Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs)) {
                    QuestScope.entries.forEach { scope ->
                        val isSelected = selectedScope == scope
                        val labelRes = when (scope) {
                            QuestScope.OCCURRENCE -> R.string.quest_scope_occurrence
                            QuestScope.FUTURE -> R.string.quest_scope_future
                            QuestScope.ALL -> R.string.quest_scope_all
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(VrddhihRadii.sm))
                                .background(if (isSelected) VrddhihColors.Danger.copy(alpha = 0.15f) else VrddhihColors.Panel)
                                .border(
                                    1.dp,
                                    if (isSelected) VrddhihColors.Danger else VrddhihColors.Outline,
                                    RoundedCornerShape(VrddhihRadii.sm),
                                )
                                .clickable { selectedScope = scope }
                                .padding(horizontal = VrddhihSpacing.sm, vertical = VrddhihSpacing.xs),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedScope = scope },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = VrddhihColors.Danger,
                                    unselectedColor = VrddhihColors.TextMuted,
                                ),
                            )
                            Spacer(Modifier.width(VrddhihSpacing.xs))
                            Text(
                                text = stringResource(labelRes),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) VrddhihColors.TextPrimary else VrddhihColors.TextSecondary,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(VrddhihSpacing.lg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.quest_cancel), color = VrddhihColors.TextSecondary)
                }
                Spacer(Modifier.width(VrddhihSpacing.md))
                Button(
                    onClick = { onConfirm(if (target.isRecurring) selectedScope else QuestScope.ALL) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VrddhihColors.Danger,
                        contentColor = VrddhihColors.Void,
                    ),
                ) {
                    Text(text = stringResource(R.string.quest_delete_confirm))
                }
            }
        }
    }
}
