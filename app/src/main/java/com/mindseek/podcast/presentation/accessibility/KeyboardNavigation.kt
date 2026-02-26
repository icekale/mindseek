package com.mindseek.podcast.presentation.accessibility

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.*
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Keyboard navigation utilities for accessibility
 */

/**
 * Enhanced focusable modifier with keyboard navigation
 */
fun Modifier.keyboardNavigable(
    onEnterPressed: (() -> Unit)? = null,
    onSpacePressed: (() -> Unit)? = null,
    onArrowUp: (() -> Unit)? = null,
    onArrowDown: (() -> Unit)? = null,
    onArrowLeft: (() -> Unit)? = null,
    onArrowRight: (() -> Unit)? = null,
    onEscapePressed: (() -> Unit)? = null
) = this
    .focusable()
    .onKeyEvent { keyEvent ->
        when {
            keyEvent.type == KeyEventType.KeyDown -> {
                when (keyEvent.key) {
                    Key.Enter -> {
                        onEnterPressed?.invoke()
                        true
                    }
                    Key.Spacebar -> {
                        onSpacePressed?.invoke()
                        true
                    }
                    Key.DirectionUp -> {
                        onArrowUp?.invoke()
                        true
                    }
                    Key.DirectionDown -> {
                        onArrowDown?.invoke()
                        true
                    }
                    Key.DirectionLeft -> {
                        onArrowLeft?.invoke()
                        true
                    }
                    Key.DirectionRight -> {
                        onArrowRight?.invoke()
                        true
                    }
                    Key.Escape -> {
                        onEscapePressed?.invoke()
                        true
                    }
                    else -> false
                }
            }
            else -> false
        }
    }

/**
 * Keyboard navigable list
 */
@Composable
fun <T> KeyboardNavigableList(
    items: List<T>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    onItemSelected: (T) -> Unit = {},
    itemContent: @Composable (item: T, isSelected: Boolean, focusRequester: FocusRequester) -> Unit
) {
    var selectedIndex by remember { mutableStateOf(0) }
    val focusRequesters = remember { items.map { FocusRequester() } }
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(selectedIndex) {
        if (selectedIndex in items.indices) {
            focusRequesters[selectedIndex].requestFocus()
            coroutineScope.launch {
                state.animateScrollToItem(selectedIndex)
            }
        }
    }
    
    LazyColumn(
        modifier = modifier
            .onKeyEvent { keyEvent ->
                when {
                    keyEvent.type == KeyEventType.KeyDown -> {
                        when (keyEvent.key) {
                            Key.DirectionUp -> {
                                if (selectedIndex > 0) {
                                    selectedIndex--
                                }
                                true
                            }
                            Key.DirectionDown -> {
                                if (selectedIndex < items.size - 1) {
                                    selectedIndex++
                                }
                                true
                            }
                            Key.Enter, Key.Spacebar -> {
                                if (selectedIndex in items.indices) {
                                    onItemSelected(items[selectedIndex])
                                }
                                true
                            }
                            else -> false
                        }
                    }
                    else -> false
                }
            },
        state = state,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items.size) { index ->
            val item = items[index]
            val isSelected = index == selectedIndex
            val focusRequester = focusRequesters[index]
            
            itemContent(item, isSelected, focusRequester)
        }
    }
}

/**
 * Keyboard navigable button
 */
@Composable
fun KeyboardNavigableButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    focusRequester: FocusRequester = remember { FocusRequester() },
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .focusRequester(focusRequester)
            .keyboardNavigable(
                onEnterPressed = if (enabled) onClick else null,
                onSpacePressed = if (enabled) onClick else null
            ),
        enabled = enabled,
        content = content
    )
}

/**
 * Keyboard navigable icon button
 */
@Composable
fun KeyboardNavigableIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    focusRequester: FocusRequester = remember { FocusRequester() },
    content: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .focusRequester(focusRequester)
            .keyboardNavigable(
                onEnterPressed = if (enabled) onClick else null,
                onSpacePressed = if (enabled) onClick else null
            ),
        enabled = enabled,
        content = content
    )
}

/**
 * Keyboard navigable card
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyboardNavigableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    focusRequester: FocusRequester = remember { FocusRequester() },
    colors: CardColors = CardDefaults.cardColors(),
    elevation: CardElevation = CardDefaults.cardElevation(),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .focusRequester(focusRequester)
            .keyboardNavigable(
                onEnterPressed = if (enabled) onClick else null,
                onSpacePressed = if (enabled) onClick else null
            ),
        enabled = enabled,
        colors = colors,
        elevation = elevation,
        content = content
    )
}

/**
 * Focus group for managing focus within a container
 */
@Composable
fun FocusGroup(
    modifier: Modifier = Modifier,
    content: @Composable FocusGroupScope.() -> Unit
) {
    val focusManager = LocalFocusManager.current
    val focusRequesters = remember { mutableListOf<FocusRequester>() }
    var currentFocusIndex by remember { mutableStateOf(0) }
    
    val scope = remember {
        object : FocusGroupScope {
            override fun addFocusRequester(focusRequester: FocusRequester) {
                focusRequesters.add(focusRequester)
            }
            
            override fun requestFocus(index: Int) {
                if (index in focusRequesters.indices) {
                    currentFocusIndex = index
                    focusRequesters[index].requestFocus()
                }
            }
            
            override fun moveFocusNext() {
                val nextIndex = (currentFocusIndex + 1) % focusRequesters.size
                requestFocus(nextIndex)
            }
            
            override fun moveFocusPrevious() {
                val prevIndex = if (currentFocusIndex > 0) currentFocusIndex - 1 else focusRequesters.size - 1
                requestFocus(prevIndex)
            }
        }
    }
    
    Box(
        modifier = modifier
            .onKeyEvent { keyEvent ->
                when {
                    keyEvent.type == KeyEventType.KeyDown -> {
                        when (keyEvent.key) {
                            Key.Tab -> {
                                if (keyEvent.isShiftPressed) {
                                    scope.moveFocusPrevious()
                                } else {
                                    scope.moveFocusNext()
                                }
                                true
                            }
                            Key.DirectionUp -> {
                                scope.moveFocusPrevious()
                                true
                            }
                            Key.DirectionDown -> {
                                scope.moveFocusNext()
                                true
                            }
                            else -> false
                        }
                    }
                    else -> false
                }
            }
    ) {
        scope.content()
    }
}

/**
 * Scope for focus group
 */
interface FocusGroupScope {
    fun addFocusRequester(focusRequester: FocusRequester)
    fun requestFocus(index: Int)
    fun moveFocusNext()
    fun moveFocusPrevious()
}

/**
 * Skip link for keyboard navigation
 */
@Composable
fun SkipLink(
    text: String,
    targetFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    if (isVisible) {
        Button(
            onClick = { targetFocusRequester.requestFocus() },
            modifier = modifier
                .onFocusChanged { focusState ->
                    isVisible = focusState.isFocused
                }
        ) {
            Text(text)
        }
    } else {
        // Invisible but focusable skip link
        Box(
            modifier = modifier
                .size(1.dp)
                .focusable()
                .onFocusChanged { focusState ->
                    isVisible = focusState.isFocused
                }
        )
    }
}

/**
 * Keyboard shortcut handler
 */
@Composable
fun KeyboardShortcutHandler(
    shortcuts: Map<KeyShortcut, () -> Unit>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    val shortcut = KeyShortcut(
                        key = keyEvent.key,
                        ctrl = keyEvent.isCtrlPressed,
                        alt = keyEvent.isAltPressed,
                        shift = keyEvent.isShiftPressed
                    )
                    
                    shortcuts[shortcut]?.let { action ->
                        action()
                        return@onKeyEvent true
                    }
                }
                false
            }
    ) {
        content()
    }
}

/**
 * Data class for keyboard shortcuts
 */
data class KeyShortcut(
    val key: Key,
    val ctrl: Boolean = false,
    val alt: Boolean = false,
    val shift: Boolean = false
)

/**
 * Common keyboard shortcuts
 */
object CommonShortcuts {
    val PLAY_PAUSE = KeyShortcut(Key.Spacebar)
    val NEXT_TRACK = KeyShortcut(Key.DirectionRight, ctrl = true)
    val PREVIOUS_TRACK = KeyShortcut(Key.DirectionLeft, ctrl = true)
    val VOLUME_UP = KeyShortcut(Key.DirectionUp, ctrl = true)
    val VOLUME_DOWN = KeyShortcut(Key.DirectionDown, ctrl = true)
    val SEARCH = KeyShortcut(Key.F, ctrl = true)
    val REFRESH = KeyShortcut(Key.F5)
    val ESCAPE = KeyShortcut(Key.Escape)
}