/**
 * DefaultActivationRepository.kt
 *
 * Communicates with the remote ActivationApi using Retrofit, logs every step of the process,
 * and safely wraps all network operations in try/catch blocks.
 *
 * The class:
 *  - Sends an activation request with a unique card code.
 *  - Validates and parses the API response.
 *  - Checks whether the "android" version value is greater than 277028.
 *  - Returns:
 *      Result.success(true) → when activation is valid.
 *      Result.failure(Exception) → when activation fails or the API returns an error.
 *
 * Logging is included for easier debugging of API communication.
 * All work is executed on Dispatchers.IO to keep it off the main thread.
 */

package com.matejfilus.o2scratchcard.data.repository

import android.util.Log
import com.matejfilus.o2scratchcard.data.api.ActivationApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response


/**
 * Repository interface for card activation.
 * Provides a suspend function to activate a card by its unique code.
 */
interface ActivationRepository {
    suspend fun activateCard(code: String): Result<Boolean>
}

/**
 * DefaultActivationRepository
 *
 * Concrete implementation of the [ActivationRepository] interface.
 * Acts as the **data layer** bridge between the ViewModel and the remote API.
 *
 * Responsibilities:
 * - Calls the Retrofit service [ActivationApi] to perform activation requests
 * - Parses the HTTP response and determines activation success
 * - Returns a [Result] object (success or failure) to the ViewModel
 *
 * Data flow:
 * ViewModel → ActivationRepository → ActivationApi → Server → Repository → ViewModel
 *
 * The API endpoint:
 *     GET https://api.o2.sk/version?code=<uuid>
 * returns a JSON object like:
 *     { "android": "287028" }
 *
 * The repository checks if `android.toInt() > 277028`:
 *  - true → activation successful (`Result.success(Unit)`)
 *  - false → activation failed (`Result.failure(Exception("Activation failed"))`)
 *
 * This layer hides networking and parsing logic from the ViewModel,
 * ensuring better testability and clean separation of concerns.
 */

class DefaultActivationRepository(
    private val api: ActivationApi
) : ActivationRepository {

    override suspend fun activateCard(code: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("ActivationRepo", "Activating card with code: $code")

                val response: Response<Map<String, String>> = api.activateCard(code)
                Log.d("ActivationRepo", "API raw response: $response")

                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("ActivationRepo", "Parsed body: $body")

                    val version = body?.get("android")?.toIntOrNull()
                    Log.d("ActivationRepo", "Parsed version: $version")

                    if (version != null && version > 277028) {
                        Log.d("ActivationRepo", "Activation success!")
                        Result.success(true)
                    } else {
                        Log.e("ActivationRepo", "Activation failed - invalid version or null")
                        Result.failure(Exception("Activation failed (version = $version)"))
                    }
                } else {
                    Log.e("ActivationRepo", "API call failed: ${response.code()} ${response.message()}")
                    Result.failure(Exception("API call failed: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e("ActivationRepo", "Exception during activation: ${e.message}", e)
                Result.failure(e)
            }
        }
    }
}
