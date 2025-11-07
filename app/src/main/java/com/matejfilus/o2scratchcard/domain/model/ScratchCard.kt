package com.matejfilus.o2scratchcard.domain.model

data class ScratchCard(
    val code: String? = null,
    val state: CardState = CardState.UNSCRATCHED,
    val timestamp: Long = System.currentTimeMillis()
)
