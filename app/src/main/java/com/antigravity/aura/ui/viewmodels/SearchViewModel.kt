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
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import javax.inject.Inject

data class TrackSearchResult(
    val videoId: String,
    val title: String,
    val artist: String
)

@HiltViewModel
class SearchViewModel @Inject constructor() : ViewModel() {
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
                val results = withContext(Dispatchers.IO) {
                    val apiKey = com.antigravity.aura.BuildConfig.YOUTUBE_API_KEY
                    
                    if (apiKey.isEmpty() || apiKey == "YOUR_API_KEY_HERE") {
                        throw Exception("Please add YOUTUBE_API_KEY=AIza... to your local.properties file!")
                    }

                    val encodedQuery = java.net.URLEncoder.encode("$query official audio", "UTF-8")
                    val urlString = "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&maxResults=20&q=$encodedQuery&key=$apiKey"
                    
                    val responseStr = java.net.URL(urlString).readText()
                    val json = org.json.JSONObject(responseStr)
                    val items = json.optJSONArray("items") ?: org.json.JSONArray()
                    
                    val parsedResults = mutableListOf<TrackSearchResult>()
                    for (i in 0 until items.length()) {
                        val item = items.getJSONObject(i)
                        val videoId = item.optJSONObject("id")?.optString("videoId")
                        val snippet = item.optJSONObject("snippet")
                        
                        if (videoId != null && snippet != null) {
                            // Clean up HTML escaped text like &#39;
                            val title = android.text.Html.fromHtml(snippet.optString("title"), android.text.Html.FROM_HTML_MODE_LEGACY).toString()
                            val artist = snippet.optString("channelTitle")
                            
                            parsedResults.add(
                                TrackSearchResult(
                                    videoId = videoId,
                                    title = title,
                                    artist = artist
                                )
                            )
                        }
                    }
                    parsedResults
                }
                
                if (results.isEmpty()) {
                    _errorMessage.value = "YouTube API returned no results."
                    _searchResults.value = emptyList()
                } else {
                    _searchResults.value = results
                }
                
                
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Failed to fetch from YouTube: \${e.message ?: e.javaClass.simpleName}."
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
