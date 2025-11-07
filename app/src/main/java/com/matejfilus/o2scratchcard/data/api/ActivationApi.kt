package com.matejfilus.o2scratchcard.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ActivationApi {
    // Activation as GET with query parameter "code"
    @GET("version")
    suspend fun activateCard(
        @Query("code") code: String
    ): Response<Map<String, String>>
}

