package com.antigravity.aura.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.aura.data.entity.ApiKeyEntity
import com.antigravity.aura.data.repository.AuraRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApiKeyViewModel @Inject constructor(
    private val repository: AuraRepository
) : ViewModel() {

    val apiKeys: StateFlow<List<ApiKeyEntity>> = repository.getAllApiKeys()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addApiKey(key: String, label: String) {
        viewModelScope.launch {
            val isFirst = apiKeys.value.isEmpty()
            repository.insertApiKey(
                ApiKeyEntity(
                    key = key,
                    label = label,
                    isActive = isFirst // Automatically activate the first key
                )
            )
        }
    }

    fun deleteApiKey(apiKey: ApiKeyEntity) {
        viewModelScope.launch {
            repository.deleteApiKey(apiKey)
        }
    }

    fun setActiveKey(keyId: Int) {
        viewModelScope.launch {
            repository.setActiveApiKey(keyId)
        }
    }

    fun resetQuota(apiKey: ApiKeyEntity) {
        viewModelScope.launch {
            repository.updateApiKey(apiKey.copy(isQuotaExceeded = false))
        }
    }
}
