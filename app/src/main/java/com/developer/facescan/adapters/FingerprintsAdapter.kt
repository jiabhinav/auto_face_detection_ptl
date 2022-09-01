package com.developer.facescan.adapters

import android.content.Context
import android.graphics.Bitmap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.developer.facescan.R
import com.developer.facescan.interfaces.OnImageClick


class FingerprintsAdapter(
    val context: Context,
    private var mFingerPrints: ArrayList<Bitmap>,
    var onImageClick: OnImageClick)
    : RecyclerView.Adapter<FingerprintsAdapter.ViewHolder>() {
    
    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fingerprint, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bitmap = mFingerPrints[position]

        // sets the image to the imageview from our itemHolder class
//        Picasso.with(context).load(itemsViewModel.file).into(holder.imageView)
        holder.imageView.setImageBitmap(bitmap)
        holder.imageView.setOnClickListener {
            onImageClick.onClickImage(position)
        }
    }

    override fun getItemCount(): Int {
        return mFingerPrints.size
    }


    // Holds the views for adding it to image
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        var imageView: ImageView = itemView.findViewById(R.id.img_fingerprint)
        }

    }


