package com.mindseek.podcast.presentation.accessibility

import androidx.compose.material3.Text
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class AccessibilityUtilsTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun `AccessibleButton should have correct semantics`() {
        // Given
        val buttonText = "Test Button"
        val contentDescription = "Test button for accessibility"
        var clicked = false
        
        // When
        composeTestRule.setContent {
            AccessibleButton(
                onClick = { clicked = true },
                contentDescription = contentDescription
            ) {
                Text(buttonText)
            }
        }
        
        // Then
        composeTestRule
            .onNodeWithContentDescription(contentDescription)
            .assertExists()
            .assertHasClickAction()
            .assertIsEnabled()
            .performClick()
        
        assert(clicked)
    }
    
    @Test
    fun `AccessibleIconButton should have correct content description`() {
        // Given
        val contentDescription = "Play button"
        var clicked = false
        
        // When
        composeTestRule.setContent {
            AccessibleIconButton(
                onClick = { clicked = true },
                contentDescription = contentDescription
            ) {
                Text("按钮")
            }
        }
        
        // Then
        composeTestRule
            .onNodeWithContentDescription(contentDescription)
            .assertExists()
            .assertHasClickAction()
            .performClick()
        
        assert(clicked)
    }
    
    @Test
    fun `AccessibleText with heading should have correct semantics`() {
        // Given
        val headingText = "Main Heading"
        val headingLevel = 1
        
        // When
        composeTestRule.setContent {
            AccessibleText(
                text = headingText,
                isHeading = true,
                headingLevel = headingLevel
            )
        }
        
        // Then
        composeTestRule
            .onNodeWithText(headingText)
            .assertExists()
            .assert(hasTestTag("heading_level_$headingLevel"))
    }
    
    @Test
    fun `AccessibleSlider should announce value changes`() {
        // Given
        var sliderValue = 0.5f
        val contentDescription = "Volume slider"
        
        // When
        composeTestRule.setContent {
            AccessibleSlider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                contentDescription = contentDescription,
                valueDescription = { "${(it * 100).toInt()}%" }
            )
        }
        
        // Then
        composeTestRule
            .onNodeWithContentDescription(contentDescription)
            .assertExists()
            .assertRangeInfoEquals(ClosedFloatingPointRange(0f, 1f))
    }
    
    @Test
    fun `AccessibleSwitch should have correct state description`() {
        // Given
        var switchState = false
        val contentDescription = "Enable notifications"
        
        // When
        composeTestRule.setContent {
            AccessibleSwitch(
                checked = switchState,
                onCheckedChange = { switchState = it },
                contentDescription = contentDescription
            )
        }
        
        // Then
        val node = composeTestRule
            .onNodeWithContentDescription(contentDescription)
            .assertExists()
            .assertIsToggleable()
            .assertIsOff()
        
        // Perform toggle
        node.performClick()
        
        // Verify state changed
        node.assertIsOn()
        assert(switchState)
    }
    
    @Test
    fun `AccessibleProgressIndicator should have correct progress info`() {
        // Given
        val progress = 0.75f
        val contentDescription = "Download progress"
        
        // When
        composeTestRule.setContent {
            AccessibleProgressIndicator(
                progress = progress,
                contentDescription = contentDescription
            )
        }
        
        // Then
        composeTestRule
            .onNodeWithContentDescription(contentDescription)
            .assertExists()
            .assertRangeInfoEquals(ClosedFloatingPointRange(0f, 1f))
    }
    
    @Test
    fun `AccessibleTextField should have correct semantics when error`() {
        // Given
        var textValue = ""
        val label = "Email"
        val errorMessage = "Invalid email format"
        
        // When
        composeTestRule.setContent {
            AccessibleTextField(
                value = textValue,
                onValueChange = { textValue = it },
                label = label,
                isError = true,
                errorMessage = errorMessage
            )
        }
        
        // Then
        composeTestRule
            .onNodeWithContentDescription(label)
            .assertExists()
            .assertIsDisplayed()
            .assertTextEquals("")
    }
    
    @Test
    fun `AccessibleRadioGroup should handle selection correctly`() {
        // Given
        val options = listOf("Option 1", "Option 2", "Option 3")
        var selectedOption = options[0]
        val groupLabel = "Choose an option"
        
        // When
        composeTestRule.setContent {
            AccessibleRadioGroup(
                options = options,
                selectedOption = selectedOption,
                onOptionSelected = { selectedOption = it },
                groupLabel = groupLabel
            )
        }
        
        // Then
        composeTestRule
            .onNodeWithContentDescription(groupLabel)
            .assertExists()
        
        // Check that first option is selected
        composeTestRule
            .onNodeWithContentDescription(options[0])
            .assertIsSelected()
        
        // Select second option
        composeTestRule
            .onNodeWithContentDescription(options[1])
            .performClick()
        
        // Verify selection changed
        assert(selectedOption == options[1])
    }
    
    @Test
    fun `disabled AccessibleButton should have disabled semantics`() {
        // Given
        val buttonText = "Disabled Button"
        val contentDescription = "Disabled test button"
        
        // When
        composeTestRule.setContent {
            AccessibleButton(
                onClick = { },
                enabled = false,
                contentDescription = contentDescription
            ) {
                Text(buttonText)
            }
        }
        
        // Then
        composeTestRule
            .onNodeWithContentDescription(contentDescription)
            .assertExists()
            .assertIsNotEnabled()
    }
}