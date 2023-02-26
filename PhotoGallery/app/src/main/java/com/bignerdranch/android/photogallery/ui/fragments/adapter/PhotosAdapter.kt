package com.bignerdranch.android.photogallery.ui.fragments.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.photogallery.R
import com.bignerdranch.android.photogallery.domain.models.Photo
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.list_item_gallery.view.*

class PhotosAdapter(val photos: MutableList<Photo> = mutableListOf()) : RecyclerView.Adapter<PhotosAdapter.PhotosViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotosViewHolder {
        return PhotosViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_gallery,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = photos.size

    override fun onBindViewHolder(holder: PhotosViewHolder, position: Int) {
        holder.bind(photos[position])
    }

    inner class PhotosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(photo: Photo) {
            Picasso.get().
            load(photo.url)
                .resize(IMAGE_SIDE_PX, IMAGE_SIDE_PX)
                .centerCrop()
                .into(itemView.imageView)
        }
    }
}

const val IMAGE_SIDE_PX = 400