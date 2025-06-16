package com.example.kostku

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kostku.data.UserPreferences
import com.example.kostku.data.UserPreferencesManager
import com.example.kostku.databinding.ActivityRoomDetailBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.midtrans.sdk.corekit.core.MidtransSDK
import com.midtrans.sdk.corekit.core.TransactionRequest
import com.midtrans.sdk.corekit.models.snap.TransactionResult
import com.midtrans.sdk.uikit.SdkUIFlowBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import kotlin.random.Random

class RoomDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRoomDetailBinding
    private lateinit var userPreferencesManager: UserPreferencesManager
    private val clientId = "SB-Mid-client-Kz7YBCRafj0Qab0U"
    private val baseUrl = "https://midtrans-endpoint.vercel.app/"
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize UserPreferencesManager
        userPreferencesManager = UserPreferencesManager(this)

        // Initialize Midtrans SDK
        SdkUIFlowBuilder.init()
            .setClientKey(clientId)
            .setContext(this)
            .setTransactionFinishedCallback { result ->
                // Log transaction result
                android.util.Log.d("MidtransDebug", "Transaction Result: ${result.status}")
                android.util.Log.d("MidtransDebug", "Result Data: $result")

                when (result.status) {
                    TransactionResult.STATUS_SUCCESS -> {
                        Toast.makeText(this, "Payment Success", Toast.LENGTH_LONG).show()
                    }
                    TransactionResult.STATUS_PENDING -> {
                        Toast.makeText(this, "Payment Pending", Toast.LENGTH_LONG).show()
                    }
                    TransactionResult.STATUS_FAILED -> {
                        Toast.makeText(this, "Payment Failed", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setMerchantBaseUrl(baseUrl)
            .enableLog(true)
            .buildSDK()

        // Get room data from intent
        val roomNumber = intent.getIntExtra("room_number", 0)
        val roomType = intent.getStringExtra("room_type") ?: ""
        val roomPrice = intent.getIntExtra("room_price", 0)
        val roomFacilities = intent.getStringExtra("room_facilities") ?: ""

        setupUI(roomNumber, roomType, roomPrice, roomFacilities)
        setupListeners(roomNumber, roomPrice)
    }

    private fun setupUI(roomNumber: Int, roomType: String, roomPrice: Int, roomFacilities: String) {
        binding.apply {
            toolbar.setNavigationOnClickListener { finish() }
            toolbar.title = "Room $roomNumber"

            tvRoomNumber.text = "Room $roomNumber"
            tvRoomType.text = roomType
            tvPrice.text = formatPrice(roomPrice)
            tvFacilities.text = roomFacilities
        }
    }

    private fun setupListeners(roomNumber: Int, roomPrice: Int) {
        binding.btnOrder.setOnClickListener {
            startPayment(roomNumber, roomPrice)
        }
    }

    private fun startPayment(roomNumber: Int, roomPrice: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userPreferences = userPreferencesManager.userPreferencesFlow.first()
                val snapToken = getSnapToken(roomNumber, roomPrice, userPreferences)
                withContext(Dispatchers.Main) {
                    if (snapToken != null) {
                        MidtransSDK.getInstance().startPaymentUiFlow(this@RoomDetailActivity, snapToken)
                    } else {
                        Toast.makeText(this@RoomDetailActivity, "Failed to get payment token", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RoomDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun getSnapToken(roomNumber: Int, roomPrice: Int, userPreferences: UserPreferences): String? {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val mediaType = "application/json".toMediaType()
                
                // Generate random order ID between 1-1000
                val orderId = "order-${Random.nextInt(1, 1001)}"
                
                val requestBody = JSONObject().apply {
                    put("order_id", orderId)
                    put("amount", roomPrice)
                    put("customer_details", JSONObject().apply {
                        put("first_name", userPreferences.name)
                        put("email", userPreferences.email)
                    })
                }.toString()

                // Log request details
                android.util.Log.d("MidtransDebug", "Request URL: $baseUrl/snap-token")
                android.util.Log.d("MidtransDebug", "Request Body: $requestBody")

                val request = Request.Builder()
                    .url("$baseUrl/snap-token")
                    .post(requestBody.toRequestBody(mediaType))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                
                // Log response details
                android.util.Log.d("MidtransDebug", "Response Code: ${response.code}")
                android.util.Log.d("MidtransDebug", "Response Body: $responseBody")

                if (response.isSuccessful) {
                    val jsonResponse = JSONObject(responseBody)
                    val token = jsonResponse.getString("token")
                    android.util.Log.d("MidtransDebug", "Snap Token: $token")

                    // Save transaction to Firebase
                    try {
                        val transactionData = hashMapOf(
                            "status" to "pending",
                            "token" to token,
                            "order_id" to orderId,
                            "amount" to roomPrice,
                            "room" to roomNumber,
                            "customer_name" to userPreferences.name,
                            "customer_email" to userPreferences.email,
                            "created_at" to com.google.firebase.Timestamp.now()
                        )

                        db.collection("transaction")
                            .document(orderId)
                            .set(transactionData)
                            .await()

                        android.util.Log.d("MidtransDebug", "Transaction saved to Firebase with ID: $orderId")
                    } catch (e: Exception) {
                        android.util.Log.e("MidtransDebug", "Error saving to Firebase: ${e.message}", e)
                    }

                    token
                } else {
                    android.util.Log.e("MidtransDebug", "Error Response: $responseBody")
                    null
                }
            } catch (e: IOException) {
                android.util.Log.e("MidtransDebug", "Network Error: ${e.message}", e)
                null
            } catch (e: Exception) {
                android.util.Log.e("MidtransDebug", "General Error: ${e.message}", e)
                null
            }
        }
    }

    private fun formatPrice(price: Int): String {
        val format = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("id", "ID"))
        return format.format(price)
    }
}