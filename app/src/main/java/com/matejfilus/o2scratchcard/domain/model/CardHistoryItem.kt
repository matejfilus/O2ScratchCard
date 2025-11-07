package com.matejfilus.o2scratchcard.domain.model

data class CardHistoryItem(
    val code: String?,
    val state: CardState,
    val timestamp: Long
)
