package com.weijia.chatbuddy.models

import com.weijia.chatbuddy.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface GrokApi {
    @POST("chat/completions")
    suspend fun getChatResponse(@Body request: ChatRequest): ChatResponse
}

data class ChatRequest(
    val model: String = "grok-beta",
    val messages: List<Message>,
    val max_tokens: Int = 500,
    val temperature: Float = 0.7f
)

data class Message(val role: String, val content: String)

data class ChatResponse(val choices: List<Choice>)

data class Choice(val message: Message)

object GrokService {

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain: Interceptor.Chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${BuildConfig.GROK_API_KEY}")
                .build()
            chain.proceed(request)
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.x.ai/v1/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: GrokApi = retrofit.create(GrokApi::class.java)
}