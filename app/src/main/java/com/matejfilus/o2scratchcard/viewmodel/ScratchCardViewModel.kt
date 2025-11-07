package com.matejfilus.o2scratchcard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matejfilus.o2scratchcard.data.api.RetrofitInstance
import com.matejfilus.o2scratchcard.data.repository.ActivationRepository
import com.matejfilus.o2scratchcard.data.repository.DefaultActivationRepository
import com.matejfilus.o2scratchcard.domain.model.CardState
import com.matejfilus.o2scratchcard.domain.model.ScratchCard
import com.matejfilus.o2scratchcard.domain.model.CardHistoryItem
import com.matejfilus.o2scratchcard.data.api.ActivationApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel responsible for managing the scratch card lifecycle and app state.
 *
 * This class coordinates between the UI (Compose screens) and the data layer
 * through the [ActivationRepository], which performs the network call to
 * `https://api.o2.sk/version?code=<uuid>` using [ActivationApi].
 *
 * Relation to ActivationApi:
 * This ViewModel never calls the network API directly.
 * Instead, it communicates with the data layer through an abstraction:
 *      `ActivationRepository`
 *
 * The repository (DefaultActivationRepository) internally uses Retrofit’s
 * `ActivationApi` to perform the real HTTP request:
 *
 * ViewModel → ActivationRepository → ActivationApi → Server
 *
 * This separation ensures:
 *  - Clean architecture with single responsibility per layer
 *  - Easier unit testing (ViewModel can be tested without network access)
 *  - Clear boundary between UI logic (ViewModel) and data source (API)
 *
 * The ViewModel exposes three key layers of reactive state via [StateFlow]:
 *  - [card]: current scratch card (unscratched, scratched, or activated)
 *  - [history]: in-memory log of all card events (scratched, activated, or cancelled)
 *  - [errorMessage] / [isLoading]: used for UI feedback and dialogs
 *
 * Key responsibilities:
 *  - Simulate scratching (2-second delay, generates random UUID)
 *  - Handle cancellation of scratch operation
 *  - Perform activation via API and evaluate the result:
 *      - If the API response contains "android" > 277028 → success (card activated)
 *      - Otherwise → show error dialog
 *  - Maintain a local (RAM-only) history of all scratch and activation attempts
 *
 * The ViewModel is lifecycle-aware, uses [viewModelScope] for coroutines,
 * and is created via [ScratchCardViewModelFactory] to allow dependency injection.
 */

class ScratchCardViewModel(
    private val repository: ActivationRepository = DefaultActivationRepository(RetrofitInstance.api)
) : ViewModel() {

    // Current card
    private val _card = MutableStateFlow(ScratchCard())
    val card: StateFlow<ScratchCard> = _card

    // Card history (RAM only)
    private val _history = MutableStateFlow<List<CardHistoryItem>>(emptyList())
    val history: StateFlow<List<CardHistoryItem>> = _history

    // Error messages
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /**
     * Simulates card scratching (takes 2 seconds).
     * Generates a new code if the operation is completed.
     */
    fun scratchCard() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            val code = UUID.randomUUID().toString()
            val newCard = ScratchCard(code = code, state = CardState.SCRATCHED, timestamp = System.currentTimeMillis())
            _card.value = newCard
            addToHistory(newCard)
        }
    }

    /**
     * Called after scratching is complete (e.g. in ScratchScreen)
     */
    fun onScratchFinished() {
        val code = UUID.randomUUID().toString()
        val newCard = ScratchCard(code = code, state = CardState.SCRATCHED, timestamp = System.currentTimeMillis())
        _card.value = newCard
        addToHistory(newCard)
    }

    /**
     * Activates the card – contacts the API.
     * If the value of "android" > 277028 → success.
     */
    fun activateCard(onError: (String) -> Unit = { msg -> _errorMessage.value = msg }) {
        val currentCard = _card.value

        if (currentCard.code.isNullOrBlank()) {
            onError("Activation failed: missing code.")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.activateCard(currentCard.code)
                if (result.isSuccess) {
                    val activated = currentCard.copy(
                        state = CardState.ACTIVATED,
                        timestamp = System.currentTimeMillis()
                    )
                    _card.value = activated
                    _errorMessage.value = null
                    addToHistory(activated)
                } else {
                    val msg = result.exceptionOrNull()?.message ?: "Activation failed."
                    onError(msg)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clears the error message.
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Adds a record to the history from the current ScratchCard model.
     */
    fun addToHistory(card: ScratchCard) {
        val item = CardHistoryItem(
            code = card.code,
            state = card.state,
            timestamp = if (card.timestamp > 0L) card.timestamp else System.currentTimeMillis()
        )
        _history.value = listOf(item) + _history.value
    }

    /**
     * Adds a record to history by code and status (e.g. CANCELLED).
     */
    fun addToHistory(code: String?, state: CardState) {
        val newItem = CardHistoryItem(
            code = code,
            state = state,
            timestamp = System.currentTimeMillis()
        )
        _history.value = listOf(newItem) + _history.value
    }

    /**
     * Records the canceled scratching.
     */
    fun onScratchCancelled() {
        addToHistory(code = null, state = CardState.CANCELLED)
    }
}
