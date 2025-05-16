package com.example.mychatbotapp

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mychatbotapp.api.RetrofitInstance
import com.example.mychatbotapp.model.Content
import com.example.mychatbotapp.model.GeminiRequest
import com.example.mychatbotapp.model.GeminiResponse
import com.example.mychatbotapp.model.Part
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GoogleGenerativeAIException
import com.google.ai.client.generativeai.type.SerializationException
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class ChatViewModel : ViewModel() {
    val messageList by lazy {
        mutableStateListOf<MessageModel>()
    }
    private val generativeModel = GenerativeModel(
        // modelName = "models/gemini-1.0-pro-vision-latest",
        // this was not working so i run this url on postman then got different modals and tested,https://generativelanguage.googleapis.com/v1beta/models?key="your api key from google gemini api key"
        // modelName = "models/gemini-1.5-flash",
        modelName = "models/gemini-2.0-flash-001",
        apiKey = Constants.apiKey,

        )

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun sendMessage(question: String) {
        viewModelScope.launch {
            try {
                val chat = generativeModel.startChat(
                    history = messageList.map {
                        content(it.role) { text(it.message) }
                    }.toList()
                )
                messageList.add(MessageModel(question, "user"))
                messageList.add(MessageModel("Typing...", "model"))
                val response = chat.sendMessage(question)
                messageList.removeLast()
                messageList.add(MessageModel(response.text.toString(), "model"))
            } catch (e: IOException) {
                messageList.removeLast()
                messageList.add(MessageModel("No Internet Connection", "model"))
                Log.e("Network Error", "IOException: ${e.message}", e)
            } catch (e: SerializationException) {
                messageList.removeLast()
                messageList.add(
                    MessageModel(
                        "Gemini API Serialization Error: The response structure was unexpected. Try rephrasing the question.",
                        "model"
                    )
                )
                Log.e("Serialization Error", e.message ?: "Unknown serialization issue", e)

            } catch (e: GoogleGenerativeAIException) {
                messageList.removeLast()
                messageList.add(MessageModel("Gemini API Error: ${e.message}", "model"))
                Log.e("Gemini API Error", e.message ?: "Unknown Gemini API error", e)
            } catch (e: Exception) {
                messageList.removeLast()
                messageList.add(MessageModel("Unexpected Error: ${e.localizedMessage}", "model"))
                Log.e("Unexpected Error", e.localizedMessage ?: "Unknown error occurred", e)
            }
        }
    }


    // The below code using Retrofit
    /*  private val apiKey = Constants.apiKey  // store your API key here

      fun sendMessage(userInput: String) {
          val request = GeminiRequest(
              contents = listOf(
                  Content(parts = listOf(Part(text = userInput)))
              )
          )

          viewModelScope.launch(Dispatchers.IO) {
              val call = RetrofitInstance.api.sendMessage(apiKey, request)
              call.enqueue(object : Callback<GeminiResponse> {
                  override fun onResponse(call: Call<GeminiResponse>, response: Response<GeminiResponse>) {
                      if (response.isSuccessful) {
                          val text = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                          Log.i("Gemini Success", text ?: "No response")
                      } else {
                          Log.e("Gemini Error", response.errorBody()?.string() ?: "Unknown error")
                      }
                  }

                  override fun onFailure(call: Call<GeminiResponse>, t: Throwable) {
                      Log.e("Gemini Failure", t.message ?: "Network error")
                  }
              })
          }
      }*/
}