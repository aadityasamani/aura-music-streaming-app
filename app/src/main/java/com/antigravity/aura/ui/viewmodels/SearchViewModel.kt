package com.antigravity.aura.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import javax.inject.Inject

data class TrackSearchResult(
    val videoId: String,
    val title: String,
    val artist: String
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val youtubeSearcher: com.antigravity.aura.youtube.YouTubeSearcher
) : ViewModel() {
    private val _searchResults = MutableStateFlow<List<TrackSearchResult>>(emptyList())
    val searchResults: StateFlow<List<TrackSearchResult>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun search(query: String) {
        if (query.isBlank()) return
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val results = youtubeSearcher.search(query)
                
                if (results.isEmpty()) {
                    _errorMessage.value = "No results found on YouTube."
                    _searchResults.value = emptyList()
                } else {
                    _searchResults.value = results
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Failed to fetch from YouTube: ${e.message ?: e.javaClass.simpleName}."
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
