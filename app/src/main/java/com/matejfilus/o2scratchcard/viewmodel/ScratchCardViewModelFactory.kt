package com.matejfilus.o2scratchcard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.matejfilus.o2scratchcard.data.repository.ActivationRepository

/**
 * Factory class responsible for creating instances of [ScratchCardViewModel].
 *
 * Android's [ViewModelProvider] requires a no-argument constructor by default,
 * but since [ScratchCardViewModel] depends on an [ActivationRepository],
 * this factory is used to properly inject that dependency when creating the ViewModel.
 *
 * This approach makes the ViewModel easier to test and keeps it independent
 * from Android framework classes or direct dependency creation (e.g. Retrofit, repositories).
 *
 * Example usage:
 * val factory = ScratchCardViewModelFactory(DefaultActivationRepository(RetrofitInstance.api))
 * val viewModel: ScratchCardViewModel = viewModel(factory = factory)
 */

class ScratchCardViewModelFactory(
    private val repository: ActivationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScratchCardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScratchCardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
