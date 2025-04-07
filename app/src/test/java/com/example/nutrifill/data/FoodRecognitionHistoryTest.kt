package com.example.nutrifill.data

import android.content.Context
import android.content.SharedPreferences
import org.junit.Before
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import com.google.gson.Gson

@RunWith(MockitoJUnitRunner::class)
class FoodRecognitionHistoryTest {
    private lateinit var foodHistoryManager: FoodHistoryManager
    
    @Mock
    private lateinit var mockContext: Context
    
    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences
    
    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor
    
    @Before
    fun setup() {
        // Setup mock SharedPreferences
        `when`(mockContext.getSharedPreferences("food_history", Context.MODE_PRIVATE)).thenReturn(mockSharedPreferences)
        `when`(mockSharedPreferences.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        
        foodHistoryManager = FoodHistoryManager(mockContext)
    }
    
    @Test
    fun saveAndRetrieveHistory() {
        // Setup mock to return empty list initially
        `when`(mockSharedPreferences.getString(eq("history"), anyString())).thenReturn("[]")
        
        // Create a test history item
        val history = FoodRecognitionHistory(
            foodName = "Apple",
            confidence = 0.95f,
            model = "MobileNetV2"
        )
        
        // Save the history item
        foodHistoryManager.saveHistory(history)
        
        // Verify that edit() was called
        verify(mockSharedPreferences).edit()
        
        // Verify that putString was called with the correct key
        verify(mockEditor).putString(eq("history"), anyString())
        
        // Verify that apply() was called
        verify(mockEditor).apply()
    }
    
    @Test
    fun getAllHistoryReturnsCorrectData() {
        // Create a sample JSON string representing history items
        val historyJson = "[{\"foodName\":\"Apple\",\"confidence\":0.95,\"model\":\"MobileNetV2\",\"timestamp\":1234567890}]"
        
        // Setup mock to return our sample JSON
        `when`(mockSharedPreferences.getString(eq("history"), anyString())).thenReturn(historyJson)
        
        // Get all history
        val allHistory = foodHistoryManager.getAllHistory()
        
        // Verify the result
        assertEquals(1, allHistory.size)
        assertEquals("Apple", allHistory[0].foodName)
        assertEquals(0.95f, allHistory[0].confidence)
        assertEquals("MobileNetV2", allHistory[0].model)
        assertEquals(1234567890L, allHistory[0].timestamp)
    }
    
    @Test
    fun clearHistoryRemovesAllData() {
        // Clear the history
        foodHistoryManager.clearHistory()
        
        // Verify that edit() was called
        verify(mockSharedPreferences).edit()
        
        // Verify that remove was called with the correct key
        verify(mockEditor).remove("history")
        
        // Verify that apply() was called
        verify(mockEditor).apply()
    }
}