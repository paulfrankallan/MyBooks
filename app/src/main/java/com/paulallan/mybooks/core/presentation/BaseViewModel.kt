package com.paulallan.mybooks.core.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base ViewModel class for implementing MVI architecture pattern.
 * 
 * @param State The type of the state managed by this ViewModel
 * @param Intent The type of the intents/actions that can be processed by this ViewModel
 * @param Effect The type of the side effects that can be emitted by this ViewModel
 * @param initialState The initial state of the ViewModel
 */
abstract class BaseViewModel<State : Any, Intent : Any, Effect : Any>(initialState: State) : ViewModel() {
    
    // State
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()
    
    // Side effects
    private val _effect = MutableSharedFlow<Effect>()
    val effect: SharedFlow<Effect> = _effect.asSharedFlow()
    
    /**
     * Process an intent/action and update the state accordingly.
     * 
     * @param intent The intent/action to process
     */
    fun processIntent(intent: Intent) {
        viewModelScope.launch {
            reduce(intent, _state.value)
        }
    }
    
    /**
     * Update the current state.
     * 
     * @param update A function that takes the current state and returns the new state
     */
    protected fun updateState(update: (State) -> State) {
        _state.update(update)
    }
    
    /**
     * Emit a side effect.
     * 
     * @param effect The side effect to emit
     */
    protected fun emitEffect(effect: Effect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }
    
    /**
     * Reduce function that processes intents/actions and updates the state.
     * This should be implemented by subclasses.
     * 
     * @param intent The intent/action to process
     * @param state The current state
     */
    protected abstract suspend fun reduce(intent: Intent, state: State)
}