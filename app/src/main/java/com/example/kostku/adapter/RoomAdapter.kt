package com.example.kostku.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kostku.R
import com.example.kostku.model.Room
import java.text.NumberFormat
import java.util.Locale

class RoomAdapter(
    private val rooms: List<Room>,
    private val onRoomClick: (Room) -> Unit
) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    class RoomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRoomNumber: TextView = view.findViewById(R.id.tvRoomNumber)
        val tvRoomType: TextView = view.findViewById(R.id.tvRoomType)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val tvFacilities: TextView = view.findViewById(R.id.tvFacilities)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_room, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = rooms[position]
        holder.tvRoomNumber.text = "Kamar ${room.room}"
        holder.tvRoomType.text = "Tipe ${room.type}"
        holder.tvPrice.text = formatPrice(room.price)
        holder.tvFacilities.text = room.fasilitas

        // Set click listener
        holder.itemView.setOnClickListener {
            onRoomClick(room)
        }
    }

    override fun getItemCount() = rooms.size

    private fun formatPrice(price: Int): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(price)
    }
} 