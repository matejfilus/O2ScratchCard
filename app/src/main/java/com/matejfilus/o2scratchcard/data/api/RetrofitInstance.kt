package com.matejfilus.o2scratchcard.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.matejfilus.o2scratchcard.data.repository.ActivationRepository

/**
 * Retrofit API definition for activating a scratch card.
 *
 * This interface defines a single GET endpoint:
 *   https://api.o2.sk/version?code=<uuid>
 *
 * The request sends a unique scratch card code as a query parameter (`code`).
 * The response body is expected to be a simple JSON object, for example:
 *   { "android": "287028" }
 *
 * The API response is mapped as a [Map]<String, String> for simplicity.
 * The ViewModel (through the [ActivationRepository]) extracts the "android" value
 * from this response and compares it against a threshold (277028):
 *
 *   - If `android` > 277028 → the card becomes ACTIVATED
 *   - Otherwise → an error dialog is displayed
 */

object RetrofitInstance {
    val api: ActivationApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.o2.sk/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ActivationApi::class.java)
    }
}
