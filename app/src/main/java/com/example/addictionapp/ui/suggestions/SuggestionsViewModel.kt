package com.example.addictionapp.ui.suggestions

import androidx.lifecycle.*
import com.example.addictionapp.data.models.Suggestion
import com.example.addictionapp.data.suggestions.SuggestionRepository
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class SuggestionsViewModel(private val suggestionRepository: SuggestionRepository): ViewModel(){
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    val suggestions : LiveData<List<Suggestion>> =
        suggestionRepository.getAllSuggestions().onStart {
            _loading.value = true
        }.onEach{
            _loading.value = false
        }.asLiveData()

    fun addSuggestion(suggestion: String) = viewModelScope.launch{
        // 0 or null indicates that the primary key will be auto-generated by Room
        _loading.value = true
       val newSuggestion = Suggestion(0,suggestion)
       suggestionRepository.upsertSuggestion(newSuggestion)
        _loading.value = false
    }
}