package com.developer.objectproof.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.developer.objectproof.R

class InstructionAdapterNormal(val alInstructionModel: List<String>, val context: Context): RecyclerView.Adapter<InstructionAdapterNormal.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: TextView= itemView.findViewById(R.id.txt_instruction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_instructions, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val instructionModel=alInstructionModel[position]
        holder.textView.text=instructionModel
    }

    override fun getItemCount(): Int {
        return alInstructionModel.size
    }
}