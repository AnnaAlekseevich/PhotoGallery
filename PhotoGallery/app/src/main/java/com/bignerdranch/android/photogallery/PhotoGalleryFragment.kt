package com.bignerdranch.android.photogallery

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.concurrent.Executors

private const val TAG = "PhotoGalleryFragment"
private const val TAG_T = "Thread"
private lateinit var thumbnailDownloader: ThumbnailDownloader<PhotoGalleryFragment.PhotoHolder>
class PhotoGalleryFragment : Fragment() {
    private lateinit var photoGalleryViewModel: PhotoGalleryViewModel
    private lateinit var photoRecyclerView: RecyclerView
    var spanCount: Int? = null
    val cellWidth: Int? = null



    private var adapter: PhotoAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        photoGalleryViewModel =
            ViewModelProviders.of(this).get(PhotoGalleryViewModel::class.java)
        Log.d(TAG_T, "onCreate + thread= ${Thread.currentThread()}")
        adapter = PhotoAdapter(requireContext())

        val responseHandler = Handler()
        thumbnailDownloader =
            ThumbnailDownloader(responseHandler){ photoHolder, bitmap ->
                val drawable = BitmapDrawable(resources, bitmap)
                photoHolder.bindDrawable(drawable)
            }
        lifecycle.addObserver(thumbnailDownloader.fragmentLifecycleObserver)

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

        adapter?.submitList(pagedList)
    }

    fun View.afterMeasured( f: View.() -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (measuredHeight > 0 && measuredWidth > 0) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    f()
                }
            }
        })
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        activity?.let {
            getViewLifecycleOwnerLiveData().observe(it) { viewLifecycleOwner ->
                viewLifecycleOwner.lifecycle.addObserver(
                    thumbnailDownloader.viewLifecycleObserver
                )
            }
        }

        val view = inflater.inflate(R.layout.fragment_photo_gallery, container, false)
        Log.d(TAG, "onCreateView")
        photoRecyclerView = view.findViewById(R.id.photo_recycler_view)

        photoRecyclerView.afterMeasured {
            val weightColum: Int = activity?.resources?.getDimension(R.dimen.column_average_width)!!
                .toInt()

                    spanCount = photoRecyclerView.width/weightColum
                    Log.d(TAG, "onCreateView - spanCount = " + spanCount)
                    Log.d(TAG, "onCreateView - weightColum = " + weightColum)
                    Log.d(TAG, "onCreateView - photoRecyclerView.width = " + photoRecyclerView.width)
            photoRecyclerView.layoutManager = GridLayoutManager(context, photoRecyclerView.width/weightColum)
        }
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


    private class PhotoAdapter(val context: Context) :
        PagedListAdapter<GalleryItem, PhotoHolder>(DIFF_CALLBACK) {

        override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
            Log.d(TAG, "onBindViewHolder")
            val itItem: GalleryItem? = getItem(position)

            // Note that "concert" is a placeholder if it's null.
            //holder.bindTo(itItem)
            Log.d(TAG, "PhotoAdapter" + itItem)
            val placeholder: Drawable = ContextCompat.getDrawable(
                context,
                R.drawable.bill_up_close
            ) ?: ColorDrawable()
            holder.bindDrawable(placeholder)
            if (itItem != null) {
                thumbnailDownloader.queueThumbnail(holder, itItem.url)
            }
            holder.itemView.post(Runnable {
                fun run()
                {
                    val cellWidth: Int = holder.itemView.getWidth();// this will give you cell width dynamically
                    Log.d(TAG, "onBindViewHolder - cellWidth = " + cellWidth)
                    val cellHeight: Int = holder.itemView.getHeight();// this will give you cell height dynamically
                }
            })

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
            val imageView = LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_gallery,
                parent,
                false
            ) as ImageView
            return PhotoHolder(imageView)
        }
    }

    class PhotoHolder(itemImageView: ImageView) : RecyclerView.ViewHolder(itemImageView) {
        val bindDrawable: (Drawable) -> Unit = itemImageView::setImageDrawable
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewLifecycleOwner.lifecycle.removeObserver(
            thumbnailDownloader.viewLifecycleObserver
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(
            thumbnailDownloader.fragmentLifecycleObserver
        )
    }

}





