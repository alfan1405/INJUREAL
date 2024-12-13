package com.capstone.injureal

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.capstone.injureal.riwayat.PredictionHistory

class HistoryAdapter(
    private val predictionList: MutableList<PredictionHistory>
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private var deleteClickListener: OnDeleteClickListener? = null

    interface OnDeleteClickListener {
        fun onDeleteClick(position: Int)
    }

    fun setOnDeleteClickListener(listener: OnDeleteClickListener) {
        deleteClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val prediction = predictionList[position]

        Glide.with(holder.itemView.context)
            .load(prediction.imageUrl)
            .placeholder(R.drawable.ic_place_holder)
            .into(holder.historyImage)

        holder.woundName.text = prediction.woundName
        holder.description.text = prediction.description

        holder.deleteButton.setOnClickListener {
            deleteClickListener?.onDeleteClick(position)
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, HistoryDetail::class.java)

            intent.putExtra(HistoryDetail.EXTRA_HISTORY_ITEM, prediction)

            holder.itemView.context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return predictionList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val historyImage: ImageView = itemView.findViewById(R.id.history_image)
        val woundName: TextView = itemView.findViewById(R.id.wound_name)
        val description: TextView = itemView.findViewById(R.id.description)
        val deleteButton: ImageButton = itemView.findViewById(R.id.delete_button)
    }
}