package com.developer.facescan.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.developer.facescan.R
import com.developer.facescan.databinding.ItemInstructionsBinding

class InstructionAdapter(val alInstructionModel: ArrayList<String>, val context: Context):
    RecyclerView.Adapter<InstructionAdapter.ViewHolder>() {


    inner class ViewHolder(var instructionItemBinding: ItemInstructionsBinding):
        RecyclerView.ViewHolder(instructionItemBinding.root) {

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val mDeveloperListItemBinding = DataBindingUtil.inflate<ItemInstructionsBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_instructions, parent, false)
        return ViewHolder(mDeveloperListItemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentInstruction = alInstructionModel[position]
        holder.instructionItemBinding.txtInstruction.text=currentInstruction
    }



    override fun getItemCount(): Int {
        return alInstructionModel.size    }



}