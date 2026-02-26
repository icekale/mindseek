package com.mindseek.podcast.presentation.accessibility

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import java.util.*

/**
 * Accessibility utilities for the podcast app
 */

/**
 * Enhanced clickable modifier with accessibility support
 */
@Composable
fun Modifier.accessibleClickable(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val indication = rememberRipple()
    
    return this
        .clip(RectangleShape)
        .clickable(
            enabled = enabled,
            onClickLabel = onClickLabel,
            role = role,
            interactionSource = interactionSource,
            indication = indication,
            onClick = onClick
        )
        .semantics {
            if (!enabled) {
                disabled()
            }
        }
}

/**
 * Accessible button with enhanced semantics
 */
@Composable
fun AccessibleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
    role: Role = Role.Button,
    content: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.semantics {
            contentDescription?.let { this.contentDescription = it }
            this.role = role
            if (!enabled) {
                disabled()
            }
        },
        enabled = enabled
    ) {
        content()
    }
}

/**
 * Accessible icon button with content description
 */
@Composable
fun AccessibleIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.semantics {
            this.contentDescription = contentDescription
            this.role = Role.Button
            if (!enabled) {
                disabled()
            }
        },
        enabled = enabled
    ) {
        content()
    }
}

/**
 * Accessible text with semantic information
 */
@Composable
fun AccessibleText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = LocalTextStyle.current,
    role: Role? = null,
    isHeading: Boolean = false,
    headingLevel: Int = 1
) {
    Text(
        text = text,
        modifier = modifier.semantics {
            this.contentDescription = text
            role?.let { this.role = it }
            if (isHeading) {
                this.heading()
                // Add heading level information
                this.testTag = "heading_level_$headingLevel"
            }
        },
        style = style
    )
}

/**
 * Accessible image with content description
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AccessibleImage(
    contentDescription: String?,
    modifier: Modifier = Modifier,
    isDecorative: Boolean = false,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.semantics {
            if (isDecorative) {
                // Mark as decorative - screen readers will skip
                this.invisibleToUser()
            } else {
                contentDescription?.let { 
                    this.contentDescription = it 
                    this.role = Role.Image
                }
            }
        }
    ) {
        content()
    }
}

/**
 * Accessible slider with value announcements
 * Note: Slider component may not be available in all Compose versions
 */
/*
@Composable
fun AccessibleSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    contentDescription: String? = null,
    valueDescription: (Float) -> String = { "${(it * 100).toInt()}%" }
) {
    // Slider implementation commented out due to compatibility issues
}
*/

/**
 * Accessible switch with state description
 */
@Composable
fun AccessibleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier.semantics {
            contentDescription?.let { this.contentDescription = it }
            this.stateDescription = if (checked) "开启" else "关闭"
            this.role = Role.Switch
            if (!enabled) {
                disabled()
            }
        },
        enabled = enabled
    )
}

/**
 * Accessible radio button group
 */
@Composable
fun AccessibleRadioGroup(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    groupLabel: String? = null
) {
    Column(
        modifier = modifier
            .selectableGroup()
            .semantics {
                groupLabel?.let { this.contentDescription = it }
            }
    ) {
        options.forEach { option ->
            Row(
                modifier = Modifier
                    .selectable(
                        selected = (option == selectedOption),
                        onClick = { onOptionSelected(option) },
                        role = Role.RadioButton
                    )
                    .semantics {
                        this.contentDescription = option
                        this.stateDescription = if (option == selectedOption) "已选择" else "未选择"
                    }
            ) {
                RadioButton(
                    selected = (option == selectedOption),
                    onClick = null // handled by selectable modifier
                )
                Text(
                    text = option,
                    modifier = Modifier.semantics {
                        this.contentDescription = option
                    }
                )
            }
        }
    }
}

/**
 * Accessible progress indicator with value announcement
 */
@Composable
fun AccessibleProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    contentDescription: String = "加载进度",
    progressDescription: (Float) -> String = { "${(it * 100).toInt()}%" }
) {
    LinearProgressIndicator(
        progress = progress,
        modifier = modifier.semantics {
            this.contentDescription = contentDescription
            this.stateDescription = progressDescription(progress)
            // ProgressBarRangeInfo not available in this Compose version
        }
    )
}

/**
 * Accessible list item with proper semantics
 */
@Composable
fun AccessibleListItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
    role: Role = Role.Button,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.semantics {
            contentDescription?.let { this.contentDescription = it }
            this.role = role
            if (!enabled) {
                disabled()
            }
        },
        enabled = enabled
    ) {
        content()
    }
}

/**
 * Focus management utilities
 */
class FocusManager {
    private val focusRequesters = mutableMapOf<String, FocusRequester>()
    
    fun getFocusRequester(key: String): FocusRequester {
        return focusRequesters.getOrPut(key) { FocusRequester() }
    }
    
    fun requestFocus(key: String) {
        focusRequesters[key]?.requestFocus()
    }
    
    fun clearFocus() {
        focusRequesters.values.forEach { it.freeFocus() }
    }
}

/**
 * Remember focus manager
 */
@Composable
fun rememberFocusManager(): FocusManager {
    return remember { FocusManager() }
}

/**
 * Accessible text field with enhanced semantics
 */
@Composable
fun AccessibleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: String? = null,
    placeholder: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardActions: androidx.compose.foundation.text.KeyboardActions = androidx.compose.foundation.text.KeyboardActions.Default,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.semantics {
            label?.let { this.contentDescription = it }
            if (isError && errorMessage != null) {
                this.error(errorMessage)
            }
            supportingText?.let { this.stateDescription = it }
            if (!enabled) {
                disabled()
            }
            if (readOnly) {
                this.disabled()
            }
        },
        enabled = enabled,
        readOnly = readOnly,
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it) } },
        supportingText = supportingText?.let { { Text(it) } },
        isError = isError,
        keyboardActions = keyboardActions,
        keyboardOptions = keyboardOptions
    )
}

/**
 * Announce content changes to screen readers
 */
@Composable
fun AccessibilityAnnouncement(
    message: String,
    priority: AccessibilityAnnouncementPriority = AccessibilityAnnouncementPriority.Polite
) {
    val context = LocalContext.current
    
    LaunchedEffect(message) {
        if (message.isNotEmpty()) {
            // Use Android's accessibility manager to announce
            val accessibilityManager = context.getSystemService(android.content.Context.ACCESSIBILITY_SERVICE) 
                as android.view.accessibility.AccessibilityManager
            
            if (accessibilityManager.isEnabled) {
                val event = android.view.accessibility.AccessibilityEvent.obtain().apply {
                    eventType = android.view.accessibility.AccessibilityEvent.TYPE_ANNOUNCEMENT
                    text.add(message)
                }
                accessibilityManager.sendAccessibilityEvent(event)
            }
        }
    }
}

/**
 * Accessibility announcement priority
 */
enum class AccessibilityAnnouncementPriority {
    Polite,
    Assertive
}

/**
 * Live region for dynamic content updates
 */
@Composable
fun LiveRegion(
    content: String,
    modifier: Modifier = Modifier,
    priority: AccessibilityAnnouncementPriority = AccessibilityAnnouncementPriority.Polite
) {
    Text(
        text = content,
        modifier = modifier.semantics {
            this.liveRegion = when (priority) {
                AccessibilityAnnouncementPriority.Polite -> LiveRegionMode.Polite
                AccessibilityAnnouncementPriority.Assertive -> LiveRegionMode.Assertive
            }
        }
    )
}