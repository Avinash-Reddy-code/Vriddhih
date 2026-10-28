package com.vrddhih.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vrddhih.app.R
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihDimens
import com.vrddhih.app.core.theme.VrddhihRadii
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.theme.VrddhihTextStyles
import com.vrddhih.app.core.theme.VrddhihTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Standardized uppercase system field label with optional required indicator.
 *
 * @param text The label text.
 * @param modifier Modifier applied to the label.
 * @param isRequired When true, displays an accessible red asterisk indicator.
 * @param isError When true, colors the label in [VrddhihColors.Danger].
 */
@Composable
fun VrddhihFieldLabel(
    text: String,
    modifier: Modifier = Modifier,
    isRequired: Boolean = false,
    isError: Boolean = false,
) {
    val requiredDescription = stringResource(R.string.form_required_indicator)
    Row(
        modifier = modifier.semantics(mergeDescendants = true) {
            if (isRequired) {
                contentDescription = "$text, $requiredDescription"
            }
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xxs),
    ) {
        Text(
            text = text.uppercase(),
            style = VrddhihTextStyles.SystemLabel,
            color = if (isError) VrddhihColors.Danger else VrddhihColors.Ice,
        )
        if (isRequired) {
            Text(
                text = "*",
                style = VrddhihTextStyles.SystemLabel,
                color = VrddhihColors.Danger,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/**
 * Solo Leveling "Status Window" styled text field for all form inputs.
 *
 * Visual & Accessibility Specification:
 * - **Label**: External [VrddhihFieldLabel] uppercase with 2 sp letter spacing.
 *   Displays a red `*` indicator when [isRequired] is true.
 * - **Container**: [VrddhihColors.Void] (`#04060C`) background ensuring consistent dark contrast.
 * - **Borders**: 1 dp [VrddhihColors.Outline] unfocused, 1.5 dp [VrddhihColors.Electric] focused,
 *   1 dp [VrddhihColors.Danger] on error.
 * - **Typography**: [MaterialTheme.typography.bodyLarge] in [VrddhihColors.TextPrimary].
 * - **Error Presentation**: Displays an alert icon alongside the error message in [VrddhihColors.Danger]
 *   satisfying WCAG non-reliance on color alone.
 * - **Touch & Accessibility**: Minimum 48 dp interactive touch height, full keyboard options support,
 *   and TalkBack error semantics.
 *
 * @param value Current input string.
 * @param onValueChange Callback invoked on text edits.
 * @param modifier Modifier applied to the outer column.
 * @param label Optional uppercase label placed above the field.
 * @param placeholder Hint text displayed when empty.
 * @param errorMessage Non-null string puts the field in an error state with an alert message.
 * @param helperText Optional supportive guidance text displayed beneath the field when no error exists.
 * @param isRequired When true, renders a required indicator on the label.
 * @param singleLine Whether the text field is constrained to a single horizontal line.
 * @param maxLines Maximum visible lines before scrolling.
 * @param minLines Minimum visible lines.
 * @param enabled Whether the field is editable.
 * @param readOnly Whether the field is read-only.
 * @param leadingIcon Optional leading icon slot.
 * @param trailingIcon Optional trailing icon slot.
 * @param keyboardOptions Software keyboard configuration.
 * @param keyboardActions Actions triggered by keyboard IME buttons.
 */
@Composable
fun VrddhihTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    errorMessage: String? = null,
    helperText: String? = null,
    isRequired: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 4,
    minLines: Int = 1,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val isError = !errorMessage.isNullOrBlank()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
    ) {
        if (!label.isNullOrBlank()) {
            VrddhihFieldLabel(
                text = label,
                isRequired = isRequired,
                isError = isError,
            )
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp),
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            placeholder = placeholder?.let {
                {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = VrddhihColors.TextMuted,
                    )
                }
            },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            isError = isError,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            shape = RoundedCornerShape(VrddhihRadii.md),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = VrddhihColors.TextPrimary,
                unfocusedTextColor = VrddhihColors.TextPrimary,
                disabledTextColor = VrddhihColors.TextMuted,
                errorTextColor = VrddhihColors.TextPrimary,
                focusedContainerColor = VrddhihColors.Panel,
                unfocusedContainerColor = VrddhihColors.Panel,
                disabledContainerColor = VrddhihColors.PanelRaised.copy(alpha = 0.5f),
                errorContainerColor = VrddhihColors.Panel,
                cursorColor = VrddhihColors.Electric,
                errorCursorColor = VrddhihColors.Danger,
                focusedBorderColor = VrddhihColors.Electric,
                unfocusedBorderColor = VrddhihColors.Outline,
                disabledBorderColor = VrddhihColors.Outline.copy(alpha = 0.4f),
                errorBorderColor = VrddhihColors.Danger,
                focusedPlaceholderColor = VrddhihColors.TextMuted,
                unfocusedPlaceholderColor = VrddhihColors.TextMuted,
                disabledPlaceholderColor = VrddhihColors.TextMuted.copy(alpha = 0.5f),
                errorPlaceholderColor = VrddhihColors.TextMuted,
            ),
        )

        if (isError) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
                modifier = Modifier.padding(start = VrddhihSpacing.xs, top = VrddhihSpacing.xxs),
            ) {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    tint = VrddhihColors.Danger,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = errorMessage.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = VrddhihColors.Danger,
                )
            }
        } else if (!helperText.isNullOrBlank()) {
            Text(
                text = helperText,
                style = MaterialTheme.typography.bodySmall,
                color = VrddhihColors.TextSecondary,
                modifier = Modifier.padding(start = VrddhihSpacing.xs, top = VrddhihSpacing.xxs),
            )
        }
    }
}

/**
 * Standardized selection chip for filter bars, option groups, and protocol pickers.
 *
 * Enforces a minimum 48 × 48 dp interactive touch target envelope while rendering a compact,
 * glowing pill-shaped card matching the hunter system aesthetic.
 *
 * @param selected Whether the chip is active.
 * @param onClick Invoked when clicked.
 * @param label The chip text label.
 * @param modifier Modifier applied to the touch target.
 * @param enabled Whether the chip is interactive.
 * @param leadingIcon Optional leading icon.
 * @param isRadio Whether the semantic role should be RadioButton (single-select) vs Checkbox (multi-select).
 */
@Composable
fun VrddhihFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    isRadio: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(VrddhihRadii.sm)

    val containerColor = when {
        !enabled -> VrddhihColors.PanelRaised.copy(alpha = 0.5f)
        selected -> VrddhihColors.Electric.copy(alpha = 0.18f)
        else -> VrddhihColors.PanelRaised
    }

    val borderColor = when {
        !enabled -> VrddhihColors.Outline.copy(alpha = 0.3f)
        selected -> VrddhihColors.Electric
        else -> VrddhihColors.Outline
    }

    val contentColor = when {
        !enabled -> VrddhihColors.TextMuted
        selected -> VrddhihColors.Ice
        else -> VrddhihColors.TextSecondary
    }

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp, minWidth = 48.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = VrddhihColors.Ice),
                enabled = enabled,
                role = if (isRadio) Role.RadioButton else Role.Checkbox,
                onClick = onClick,
            )
            .semantics {
                this.selected = selected
                role = if (isRadio) Role.RadioButton else Role.Checkbox
            },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .clip(shape)
                .background(containerColor)
                .border(VrddhihDimens.BorderWidth, borderColor, shape)
                .padding(horizontal = VrddhihSpacing.md, vertical = VrddhihSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                text = label.uppercase(),
                style = VrddhihTextStyles.TagLabel,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                color = contentColor,
            )
        }
    }
}

/**
 * Accessible date selection tile for forms and dialogs.
 *
 * Displays the currently selected [date] formatted as "MMM d, yyyy" with a calendar icon.
 * Tapping anywhere on the tile invokes [onClick] (typically opening [VrddhihCalendarDialog]).
 * If [onClear] is non-null and [date] is non-null, displays an accessible clear button.
 *
 * @param date The currently selected [LocalDate], or null if unassigned.
 * @param onClick Invoked when the user taps to pick a date.
 * @param modifier Modifier applied to the outer container.
 * @param label Optional uppercase system label displayed above the field.
 * @param placeholder Text shown when [date] is null (e.g. "No target date").
 * @param isRequired Whether to show a red asterisk next to the label.
 * @param onClear Optional callback to clear the date when set.
 * @param enabled Whether the field is interactive.
 */
@Composable
fun VrddhihDateField(
    date: LocalDate?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = stringResource(R.string.form_select_date),
    isRequired: Boolean = false,
    onClear: (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    val dateText = date?.format(DateTimeFormatter.ofPattern("MMM d, yyyy")) ?: placeholder
    val shape = RoundedCornerShape(VrddhihRadii.md)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
    ) {
        if (!label.isNullOrBlank()) {
            VrddhihFieldLabel(
                text = label,
                isRequired = isRequired,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .clip(shape)
                .background(if (enabled) VrddhihColors.Panel else VrddhihColors.PanelRaised.copy(alpha = 0.5f))
                .border(
                    width = VrddhihDimens.BorderWidth,
                    color = if (enabled) VrddhihColors.Outline else VrddhihColors.Outline.copy(alpha = 0.4f),
                    shape = shape,
                )
                .clickable(
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick,
                )
                .padding(horizontal = VrddhihSpacing.md, vertical = VrddhihSpacing.sm),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        tint = if (date != null && enabled) VrddhihColors.Electric else VrddhihColors.TextMuted,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (date != null && enabled) VrddhihColors.TextPrimary else VrddhihColors.TextMuted,
                        fontFamily = if (date != null) FontFamily.Monospace else FontFamily.SansSerif,
                        fontWeight = if (date != null) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }

                if (date != null && onClear != null && enabled) {
                    VrddhihIconButton(
                        icon = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.form_clear_date),
                        onClick = onClear,
                        tint = VrddhihColors.TextMuted,
                        visualSize = 32.dp,
                    )
                }
            }
        }
    }
}

// region Previews

@Preview(name = "Form Controls Showcase", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun FormControlsShowcasePreview() {
    VrddhihTheme {
        Surface(color = VrddhihColors.Void) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(VrddhihSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.lg),
            ) {
                Text(
                    text = "FORM CONTROLS",
                    style = VrddhihTextStyles.SystemLabel,
                    color = VrddhihColors.Ice,
                )

                // 1. Text Field (Standard)
                VrddhihTextField(
                    value = "Defeat the Dungeon Boss",
                    onValueChange = {},
                    label = "Quest Title",
                    isRequired = true,
                    placeholder = "Enter directive title…",
                )

                // 2. Text Field with Error
                VrddhihTextField(
                    value = "",
                    onValueChange = {},
                    label = "Habit Name",
                    isRequired = true,
                    placeholder = "e.g., Morning Meditation",
                    errorMessage = "Habit name cannot be blank",
                )

                // 3. Selection Chips
                Column(verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs)) {
                    VrddhihFieldLabel(text = "Habit Protocol")
                    Row(horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs)) {
                        VrddhihFilterChip(selected = true, onClick = {}, label = "Counter")
                        VrddhihFilterChip(selected = false, onClick = {}, label = "Timer")
                        VrddhihFilterChip(selected = false, onClick = {}, label = "Checklist")
                    }
                }

                // 4. Date Field
                VrddhihDateField(
                    date = LocalDate.now(),
                    onClick = {},
                    label = "Target Deadline",
                    isRequired = false,
                    onClear = {},
                )

                // 5. Unset Date Field
                VrddhihDateField(
                    date = null,
                    onClick = {},
                    label = "Scheduled Date",
                    isRequired = true,
                )
            }
        }
    }
}

// endregion
