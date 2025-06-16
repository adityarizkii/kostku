package com.example.kostku.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kostku.R
import com.example.kostku.model.RoomType
import java.text.NumberFormat
import java.util.Locale

class RoomTypeAdapter(
    private val roomTypes: List<RoomType>,
    private val onRoomTypeClick: (RoomType) -> Unit
) : RecyclerView.Adapter<RoomTypeAdapter.RoomTypeViewHolder>() {

    class RoomTypeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivRoomType: ImageView = view.findViewById(R.id.ivRoomType)
        val tvRoomType: TextView = view.findViewById(R.id.tvRoomType)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val tvRoomCount: TextView = view.findViewById(R.id.tvRoomCount)
        val tvFacilities: TextView = view.findViewById(R.id.tvFacilities)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomTypeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_room_type, parent, false)
        return RoomTypeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomTypeViewHolder, position: Int) {
        val roomType = roomTypes[position]
        
        // Set room type image
        val imageResId = when (roomType.type.lowercase()) {
            "a" -> R.drawable.tipe_a
            "b" -> R.drawable.tipe_b
            "c" -> R.drawable.tipe_c
            else -> R.drawable.tipe_a // Default image
        }
        holder.ivRoomType.setImageResource(imageResId)
        
        holder.tvRoomType.text = "Tipe ${roomType.type}"
        holder.tvPrice.text = "Harga Mulai dari ${formatPrice(roomType.price)}"
        holder.tvRoomCount.text = "${roomType.roomCount} kamar tersedia"
        holder.tvFacilities.text = roomType.facilities

        holder.itemView.setOnClickListener {
            onRoomTypeClick(roomType)
        }
    }

    override fun getItemCount() = roomTypes.size

    private fun formatPrice(price: Int): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(price)
    }
}