package com.matejfilus.o2scratchcard.viewmodel

import com.matejfilus.o2scratchcard.data.repository.ActivationRepository
import com.matejfilus.o2scratchcard.domain.model.CardState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * ScratchCardViewModelTest Summary
 *
 * This test suite verifies the main behavior of ScratchCardViewModel:
 * - The card starts in the correct initial state.
 * - Scratching the card generates a code and updates its state.
 * - Activating the card correctly handles success and failure responses.
 *
 * Each test runs in a controlled coroutine environment using runTest and StandardTestDispatcher,
 * allowing simulation of time delays (advanceTimeBy) and coroutine completion (advanceUntilIdle).
 */

@OptIn(ExperimentalCoroutinesApi::class)
class ScratchCardViewModelTest {

    private lateinit var viewModel: ScratchCardViewModel
    private lateinit var repository: FakeActivationRepository

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeActivationRepository()
        viewModel = ScratchCardViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Verifies that a newly created card starts in the UNSCRATCHED state.
    // This ensures the ViewModel initializes its data correctly.
    @Test
    fun `initial state is UNSCRATCHED`() = runTest {
        assertEquals(CardState.UNSCRATCHED, viewModel.card.value.state)
    }

    // Simulates scratching the card.
    // The scratchCard() method includes a delay(2000), so we advance virtual time to complete it.
    // After the delay, the state should change to SCRATCHED and a unique code should be generated.
    @Test
    fun `scratchCard generates code and updates state to SCRATCHED`() = runTest {
        viewModel.scratchCard()
        advanceTimeBy(2000)
        advanceUntilIdle()

        assertEquals(CardState.SCRATCHED, viewModel.card.value.state)
        assertNotNull(viewModel.card.value.code)
    }

    // Simulates a successful activation.
    // When the fake repository returns a positive result (androidValue > 277028),
    // the ViewModel should update the card state to ACTIVATED
    // and no error message should be present.
    @Test
    fun `activateCard sets state to ACTIVATED when api value is greater than 277028`() = runTest {
        viewModel.scratchCard()
        advanceTimeBy(2000)
        advanceUntilIdle()

        repository.androidValue = 287028
        viewModel.activateCard()
        advanceUntilIdle()

        assertEquals(CardState.ACTIVATED, viewModel.card.value.state)
        assertNull(viewModel.errorMessage.value)
    }

    // Simulates a failed activation.
    // If the repository returns a negative result (androidValue <= 277028),
    // the ViewModel should show the "Activation failed" error message
    // and keep the card state as SCRATCHED (not activated).
    @Test
    fun `activateCard shows error when api value is too low`() = runTest {
        viewModel.scratchCard()
        advanceTimeBy(2000)

        repository.androidValue = 250000 // Simulate bad API response
        viewModel.activateCard()
        advanceUntilIdle()

        assertEquals("Activation failed", viewModel.errorMessage.value)
        assertEquals(CardState.SCRATCHED, viewModel.card.value.state)
    }
}

// Fake repository for testing
/**
 * FakeActivationRepository
 *
 * A fake implementation of ActivationRepository used for testing.
 * Instead of calling a real API, it returns success if androidValue > 277028,
 * and failure otherwise. This allows deterministic tests without network access.
 */
class FakeActivationRepository : ActivationRepository {
    var androidValue: Int = 0

    override suspend fun activateCard(code: String): Result<Boolean> {
        return if (androidValue > 277028) {
            Result.success(true)
        } else {
            Result.failure(Exception("Activation failed"))
        }
    }
}

