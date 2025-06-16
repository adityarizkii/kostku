package com.example.kostku

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kostku.adapter.RoomAdapter
import com.example.kostku.model.Room
import com.example.kostku.model.RoomType

class RoomTypeDetailActivity : AppCompatActivity() {
    companion object {
        private const val EXTRA_ROOM_TYPE = "extra_room_type"
        private const val EXTRA_ROOM_PRICE = "extra_room_price"
        private const val EXTRA_ROOM_FACILITIES = "extra_room_facilities"

        fun createIntent(context: Context, roomType: RoomType): Intent {
            return Intent(context, RoomTypeDetailActivity::class.java).apply {
                putExtra(EXTRA_ROOM_TYPE, roomType.type)
                putExtra(EXTRA_ROOM_PRICE, roomType.price)
                putExtra(EXTRA_ROOM_FACILITIES, roomType.facilities)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_type_detail)

        // Get room type details from intent
        val type = intent.getStringExtra(EXTRA_ROOM_TYPE) ?: return
        val price = intent.getIntExtra(EXTRA_ROOM_PRICE, 0)
        val facilities = intent.getStringExtra(EXTRA_ROOM_FACILITIES) ?: ""

        // Set up toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Tipe $type"

        // Set room type details
        findViewById<android.widget.TextView>(R.id.tvRoomType).text = "Tipe $type"
        findViewById<android.widget.TextView>(R.id.tvPrice).text = "Harga Mulai dari ${formatPrice(price)}"
        findViewById<android.widget.TextView>(R.id.tvFacilities).text = facilities

        // Set up RecyclerView with filtered rooms
        val rooms = getRoomsByType(type)
        findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvRooms).apply {
            layoutManager = LinearLayoutManager(this@RoomTypeDetailActivity)
            adapter = RoomAdapter(rooms) { room ->
                // Navigate to room detail with room data
                val intent = Intent(this@RoomTypeDetailActivity, RoomDetailActivity::class.java).apply {
                    putExtra("room_number", room.room)
                    putExtra("room_type", room.type)
                    putExtra("room_price", room.price)
                    putExtra("room_facilities", room.fasilitas)
                }
                startActivity(intent)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun getRoomsByType(type: String): List<Room> {
        // Filter rooms based on type
        return when (type) {
            "A" -> (1..5).map { Room(it, "A", 3000000, "Kasur, Meja & Kursi, AC, Kamar Mandi Dalam") }
            "B" -> (6..10).map { Room(it, "B", 2000000, "Kasur, Meja & Kursi, AC, Kamar Mandi Dalam") }
            "C" -> (11..15).map { Room(it, "C", 1000000, "Kasur, Meja & Kursi, AC, Kamar Mandi Dalam") }
            else -> emptyList()
        }
    }

    private fun formatPrice(price: Int): String {
        val format = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("id", "ID"))
        return format.format(price)
    }
}