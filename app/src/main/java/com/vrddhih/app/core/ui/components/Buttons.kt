package com.vrddhih.app.core.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vrddhih.app.R
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihDimens
import com.vrddhih.app.core.theme.VrddhihRadii
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.theme.VrddhihTextStyles
import com.vrddhih.app.core.theme.VrddhihTheme

/**
 * Standardized secondary button styled with the Solo Leveling Hunter System interface aesthetic.
 *
 * Follows the approved design specification:
 * - **Container**: [VrddhihColors.PanelRaised] (or custom/transparent for ghost variant).
 * - **Border**: 1 dp [VrddhihColors.Outline] ([VrddhihColors.Danger] when [isDestructive]).
 * - **Foreground**: [VrddhihColors.Ice] ([VrddhihColors.Danger] when [isDestructive]).
 * - **Height**: 48 dp visual height with strict minimum 48 dp touch target bounds.
 * - **Shape**: [RoundedCornerShape] with [VrddhihRadii.md] (14 dp).
 * - **Typography**: [VrddhihTextStyles.TagLabel] uppercase with bold weight.
 * - **Interaction States**: Supports enabled, disabled, loading, and destructive modes with full
 *   accessibility semantics and TalkBack state descriptions.
 *
 * @param text The uppercase button label.
 * @param onClick Invoked when clicked.
 * @param modifier Modifier applied to the button.
 * @param icon Optional leading icon.
 * @param enabled Whether the button is active and interactive.
 * @param isLoading Whether an asynchronous operation is in flight.
 * @param isDestructive Whether the action is destructive (e.g. Delete, Discard).
 * @param containerColor Container background color; defaults to [VrddhihColors.PanelRaised].
 */
@Composable
fun VrddhihSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    isDestructive: Boolean = false,
    containerColor: Color = VrddhihColors.PanelRaised,
) {
    val loadingDescription = stringResource(R.string.action_loading)
    val contentColor = when {
        !enabled -> VrddhihColors.TextMuted
        isDestructive -> VrddhihColors.Danger
        else -> VrddhihColors.Ice
    }

    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp, minWidth = 48.dp)
            .height(48.dp)
            .semantics {
                if (isLoading) {
                    stateDescription = loadingDescription
                }
            },
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(VrddhihRadii.md),
        border = BorderStroke(
            width = VrddhihDimens.BorderWidth,
            color = when {
                !enabled -> VrddhihColors.Outline.copy(alpha = 0.4f)
                isDestructive -> VrddhihColors.Danger.copy(alpha = 0.65f)
                else -> VrddhihColors.Outline
            },
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (enabled) containerColor else containerColor.copy(alpha = 0.5f),
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor = VrddhihColors.TextMuted,
        ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = contentColor,
            )
            Spacer(Modifier.width(VrddhihSpacing.sm))
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = contentColor,
            )
            Spacer(Modifier.width(VrddhihSpacing.sm))
        }
        Text(
            text = text.uppercase(),
            style = VrddhihTextStyles.TagLabel,
            fontWeight = FontWeight.Bold,
            color = contentColor,
        )
    }
}

/**
 * Accessible icon button styled with the Solo Leveling Hunter System interface aesthetic.
 *
 * Enforces a strict minimum interactive touch target of 48 × 48 dp meeting accessibility guidelines,
 * while allowing the visual icon graphic and optional background container to remain compact.
 *
 * @param icon The [ImageVector] to display.
 * @param contentDescription Mandatory accessible content description for screen readers.
 * @param onClick Invoked when clicked.
 * @param modifier Modifier applied to the outer touch target container.
 * @param enabled Whether the icon button is interactive.
 * @param isLoading Whether an operation is in progress (shows a progress indicator and prevents clicks).
 * @param tint Tint color for the icon graphic when enabled.
 * @param containerColor Background color for the button container. Defaults to transparent for ghost style.
 * @param borderColor Optional border stroke color (e.g. [VrddhihColors.Outline]).
 * @param shape Corner shape for the container and ripple boundary. Defaults to [VrddhihRadii.sm].
 * @param iconSize Size of the icon graphic; defaults to 20 dp.
 * @param visualSize Optional visual bounding size (e.g. 36 dp) for compact cards, while preserving
 *   the 48 × 48 dp touch envelope on the outer container.
 */
@Composable
fun VrddhihIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    tint: Color = VrddhihColors.Ice,
    containerColor: Color = Color.Transparent,
    borderColor: Color? = null,
    shape: Shape = RoundedCornerShape(VrddhihRadii.sm),
    iconSize: Dp = 20.dp,
    visualSize: Dp? = null,
) {
    val loadingDescription = stringResource(R.string.action_loading)
    val interactionSource = remember { MutableInteractionSource() }

    if (visualSize != null) {
        Box(
            modifier = modifier
                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = false, radius = 24.dp, color = tint),
                    enabled = enabled && !isLoading,
                    role = Role.Button,
                    onClick = onClick,
                )
                .semantics {
                    this.contentDescription = contentDescription
                    if (isLoading) {
                        stateDescription = loadingDescription
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(visualSize)
                    .clip(shape)
                    .then(
                        if (borderColor != null) {
                            Modifier.border(
                                width = VrddhihDimens.BorderWidth,
                                color = if (enabled) borderColor else borderColor.copy(alpha = 0.4f),
                                shape = shape,
                            )
                        } else {
                            Modifier
                        },
                    )
                    .background(if (enabled) containerColor else containerColor.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(iconSize),
                        strokeWidth = 2.dp,
                        color = tint,
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) tint else VrddhihColors.TextMuted,
                        modifier = Modifier.size(iconSize),
                    )
                }
            }
        }
    } else {
        Box(
            modifier = modifier
                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                .clip(shape)
                .then(
                    if (borderColor != null) {
                        Modifier.border(
                            width = VrddhihDimens.BorderWidth,
                            color = if (enabled) borderColor else borderColor.copy(alpha = 0.4f),
                            shape = shape,
                        )
                    } else {
                        Modifier
                    },
                )
                .background(if (enabled) containerColor else containerColor.copy(alpha = 0.5f))
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(color = tint),
                    enabled = enabled && !isLoading,
                    role = Role.Button,
                    onClick = onClick,
                )
                .semantics {
                    this.contentDescription = contentDescription
                    if (isLoading) {
                        stateDescription = loadingDescription
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(iconSize),
                    strokeWidth = 2.dp,
                    color = tint,
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) tint else VrddhihColors.TextMuted,
                    modifier = Modifier.size(iconSize),
                )
            }
        }
    }
}

/**
 * Reusable numeric stepper primitive providing increment/decrement controls and monospace readout.
 *
 * Ensures accessible touch bounds (at least 48 × 48 dp) for both decrement and increment controls
 * while keeping visual proportions balanced for cards and forms.
 *
 * @param value The current numerical value.
 * @param onValueChange Callback invoked with updated value when incremented or decremented.
 * @param modifier Modifier applied to the outer row.
 * @param step Amount to add or subtract on each button press.
 * @param minValue Minimum allowable value (disables decrement at or below this value).
 * @param maxValue Maximum allowable value (disables increment at or above this value).
 * @param unit Optional measurement or metric unit (e.g. `"MIN"`, `"XP"`, `"KM"`).
 * @param enabled Whether the entire stepper is interactive.
 * @param decrementContentDescription Accessible description for decrement button.
 * @param incrementContentDescription Accessible description for increment button.
 */
@Composable
fun VrddhihStepper(
    value: Long,
    onValueChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
    step: Long = 1L,
    minValue: Long = 0L,
    maxValue: Long = Long.MAX_VALUE,
    unit: String? = null,
    enabled: Boolean = true,
    decrementContentDescription: String = stringResource(R.string.stepper_decrease),
    incrementContentDescription: String = stringResource(R.string.stepper_increase),
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
    ) {
        VrddhihIconButton(
            icon = Icons.Outlined.Remove,
            contentDescription = decrementContentDescription,
            onClick = { onValueChange((value - step).coerceAtLeast(minValue)) },
            enabled = enabled && value > minValue,
            tint = VrddhihColors.Ice,
            containerColor = VrddhihColors.PanelRaised,
            borderColor = VrddhihColors.Outline,
            shape = RoundedCornerShape(VrddhihRadii.xs),
            visualSize = 36.dp,
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = VrddhihSpacing.sm),
        ) {
            Text(
                text = value.toString(),
                style = VrddhihTextStyles.NumericMedium,
                color = if (enabled) VrddhihColors.TextPrimary else VrddhihColors.TextMuted,
            )
            if (!unit.isNullOrBlank()) {
                Text(
                    text = unit.uppercase(),
                    style = VrddhihTextStyles.TagLabel,
                    color = VrddhihColors.TextSecondary,
                )
            }
        }

        VrddhihIconButton(
            icon = Icons.Outlined.Add,
            contentDescription = incrementContentDescription,
            onClick = { onValueChange((value + step).coerceAtMost(maxValue)) },
            enabled = enabled && value < maxValue,
            tint = VrddhihColors.Electric,
            containerColor = VrddhihColors.PanelRaised,
            borderColor = VrddhihColors.Outline,
            shape = RoundedCornerShape(VrddhihRadii.xs),
            visualSize = 36.dp,
        )
    }
}

/**
 * Discrete stepper delta button (e.g. `"-10"`, `"-1"`, `"+1"`, `"+10"`) for multi-step incrementing.
 *
 * Enforces a strict minimum touch target of 48 × 48 dp, styled with monospace font and
 * dark status panel styling.
 *
 * @param text The step text to display.
 * @param onClick Invoked when clicked.
 * @param modifier Modifier applied to the button.
 * @param enabled Whether the button is active.
 * @param contentDescription Optional accessibility description for screen readers.
 */
@Composable
fun VrddhihStepperButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(VrddhihRadii.xs)

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .height(48.dp)
            .clip(shape)
            .background(if (enabled) VrddhihColors.PanelRaised else VrddhihColors.PanelRaised.copy(alpha = 0.5f))
            .border(
                width = VrddhihDimens.BorderWidth,
                color = if (enabled) VrddhihColors.Outline else VrddhihColors.Outline.copy(alpha = 0.4f),
                shape = shape,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = VrddhihColors.Ice),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics {
                role = Role.Button
                if (contentDescription != null) {
                    this.contentDescription = contentDescription
                }
            }
            .padding(horizontal = VrddhihSpacing.md),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontFamily = FontFamily.Monospace,
            color = if (enabled) VrddhihColors.Ice else VrddhihColors.TextMuted,
            fontWeight = FontWeight.Bold,
        )
    }
}

// region Previews

@Preview(name = "Shared Buttons Showcase", showBackground = true, backgroundColor = 0xFF04060C)
@Composable
private fun ButtonsShowcasePreview() {
    var stepperValue by remember { mutableLongStateOf(5L) }

    VrddhihTheme {
        Surface(color = VrddhihColors.Void) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(VrddhihSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.md),
            ) {
                // Secondary buttons
                Text(
                    text = "SECONDARY BUTTONS",
                    style = VrddhihTextStyles.SystemLabel,
                    color = VrddhihColors.Ice,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
                ) {
                    VrddhihSecondaryButton(
                        text = "Cancel",
                        onClick = {},
                        modifier = Modifier.weight(1f),
                    )
                    VrddhihSecondaryButton(
                        text = "Delete",
                        icon = Icons.Outlined.Delete,
                        onClick = {},
                        isDestructive = true,
                        modifier = Modifier.weight(1f),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
                ) {
                    VrddhihSecondaryButton(
                        text = "Saving…",
                        onClick = {},
                        isLoading = true,
                        modifier = Modifier.weight(1f),
                    )
                    VrddhihSecondaryButton(
                        text = "Disabled",
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.weight(1f),
                    )
                }

                // Icon buttons
                Text(
                    text = "ICON BUTTONS (MIN 48 DP TOUCH)",
                    style = VrddhihTextStyles.SystemLabel,
                    color = VrddhihColors.Ice,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Ghost icon button
                    VrddhihIconButton(
                        icon = Icons.Outlined.Close,
                        contentDescription = "Close",
                        onClick = {},
                    )
                    // Panel-backed icon button
                    VrddhihIconButton(
                        icon = Icons.Outlined.Edit,
                        contentDescription = "Edit",
                        onClick = {},
                        containerColor = VrddhihColors.PanelRaised,
                        borderColor = VrddhihColors.Outline,
                    )
                    // Compact visual (36 dp visual, 48 dp touch target)
                    VrddhihIconButton(
                        icon = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        onClick = {},
                        tint = VrddhihColors.Danger,
                        containerColor = VrddhihColors.PanelRaised,
                        borderColor = VrddhihColors.Outline,
                        visualSize = 36.dp,
                    )
                    // Loading icon button
                    VrddhihIconButton(
                        icon = Icons.Outlined.Edit,
                        contentDescription = "Loading",
                        onClick = {},
                        isLoading = true,
                        containerColor = VrddhihColors.PanelRaised,
                        borderColor = VrddhihColors.Outline,
                    )
                    // Disabled icon button
                    VrddhihIconButton(
                        icon = Icons.Outlined.Delete,
                        contentDescription = "Disabled",
                        onClick = {},
                        enabled = false,
                        containerColor = VrddhihColors.PanelRaised,
                        borderColor = VrddhihColors.Outline,
                    )
                }

                // Numeric Stepper
                Text(
                    text = "NUMERIC STEPPER",
                    style = VrddhihTextStyles.SystemLabel,
                    color = VrddhihColors.Ice,
                )
                VrddhihStepper(
                    value = stepperValue,
                    onValueChange = { stepperValue = it },
                    unit = "Times",
                    minValue = 0L,
                    maxValue = 10L,
                )

                // Discrete Stepper Buttons
                Text(
                    text = "DISCRETE STEPPER BUTTONS",
                    style = VrddhihTextStyles.SystemLabel,
                    color = VrddhihColors.Ice,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.sm)) {
                    VrddhihStepperButton(text = "-10", onClick = {})
                    VrddhihStepperButton(text = "-1", onClick = {})
                    VrddhihStepperButton(text = "+1", onClick = {})
                    VrddhihStepperButton(text = "+10", onClick = {})
                    VrddhihStepperButton(text = "+100", onClick = {}, enabled = false)
                }
            }
        }
    }
}

// endregion
