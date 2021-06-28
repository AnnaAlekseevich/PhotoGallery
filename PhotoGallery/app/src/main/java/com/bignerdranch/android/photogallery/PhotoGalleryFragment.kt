package com.bignerdranch.android.photogallery

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.concurrent.Executors

private const val TAG = "PhotoGalleryFragment"
private const val TAG_T = "Thread"
class PhotoGalleryFragment : Fragment() {
    private lateinit var photoGalleryViewModel: PhotoGalleryViewModel
    private lateinit var photoRecyclerView: RecyclerView


private val adapter =  PhotoAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        photoGalleryViewModel =
            ViewModelProviders.of(this).get(PhotoGalleryViewModel::class.java)
        Log.d(TAG_T, "onCreate + thread= ${Thread.currentThread()}")


    }

    private fun onPhotosLoaded(items: List<GalleryItem>){
        // DataSource
      val dataSource = MyPositionalDataSource(items)
        Log.d(TAG_T, "onPhotosLoaded + thread= ${Thread.currentThread()}")

        // PagedList
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setInitialLoadSizeHint(20)
            .setPageSize(20)
            .build()

            var pagedList: PagedList<GalleryItem?> =
            PagedList.Builder(dataSource, config)
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .setNotifyExecutor(MainThreadExecutor())
                .build()

        adapter.submitList(pagedList)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_photo_gallery, container, false)
        Log.d(TAG, "onCreateView")
        photoRecyclerView = view.findViewById(R.id.photo_recycler_view)
        photoRecyclerView.layoutManager = GridLayoutManager(context, 1)


        return view
    }

    companion object {
        fun newInstance() = PhotoGalleryFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated")
        photoGalleryViewModel.galleryItemLiveData.observe(
            viewLifecycleOwner,
            { galleryItems ->
                Log.d(TAG, "onViewCreated - galleryItems" + galleryItems)
                photoRecyclerView.adapter = adapter
                onPhotosLoaded(galleryItems)
                Log.d(TAG_T, "onViewCreated  + thread= ${Thread.currentThread()}")

            }
        )
    }



    private class PhotoAdapter() :
        PagedListAdapter<GalleryItem, PhotoHolder>(DIFF_CALLBACK) {

        override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
            Log.d(TAG, "onBindViewHolder")
            val itItem: GalleryItem? = getItem(position)

            // Note that "concert" is a placeholder if it's null.
            //holder.bindTo(itItem)
            Log.d(TAG, "PhotoAdapter" + itItem)
            itItem?.let { holder.bindTitle(it.title) }
        }

        companion object {
            private val DIFF_CALLBACK = object :
                DiffUtil.ItemCallback<GalleryItem>() {
                // Concert details may have changed if reloaded from the database,
                // but ID is fixed.
                override fun areItemsTheSame(oldItem: GalleryItem,
                                             newItem: GalleryItem) = oldItem.id == newItem.id

                override fun areContentsTheSame(oldItem: GalleryItem,
                                                newItem: GalleryItem) = oldItem == newItem
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
            Log.d(TAG, "onCreateViewHolder")
            val textView = TextView(parent.context)
            return PhotoHolder(textView)
        }
    }

        class PhotoHolder(itemTextView: TextView) : RecyclerView.ViewHolder(itemTextView){
            val bindTitle: (CharSequence) -> Unit = itemTextView::setText
        }

}





