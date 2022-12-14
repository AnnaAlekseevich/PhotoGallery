package com.bignerdranch.android.photogallery.ui.fragments

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.bignerdranch.android.photogallery.*
import com.bignerdranch.android.photogallery.MainThreadExecutor
import com.bignerdranch.android.photogallery.R
import com.bignerdranch.android.photogallery.domain.managers.workmanager.PollWorker
import com.bignerdranch.android.photogallery.domain.models.GalleryItem
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

private const val TAG = "PhotoGalleryFragment"
private const val TAG_T = "Thread"
private const val POLL_WORK = "POLL_WORK"
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
        setHasOptionsMenu(true)

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

//        val constraints = Constraints.Builder()
//            .setRequiredNetworkType(NetworkType.UNMETERED)
//            .build()
//        val workRequest = OneTimeWorkRequest
//            .Builder(PollWorker::class.java)
//            .setConstraints(constraints)
//            .build()
//        WorkManager.getInstance()
//            .enqueue(workRequest)

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

            val pagedList: PagedList<GalleryItem> =
            PagedList.Builder(dataSource, config)
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .setNotifyExecutor(MainThreadExecutor())
                .build()

        adapter?.submitList(pagedList)
    }

    private fun View.afterMeasured(f: View.() -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
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
            viewLifecycleOwner
        ) {
            Log.d(TAG, "onViewCreated - galleryItems" + it)
            photoRecyclerView.adapter = adapter
            onPhotosLoaded(it)
            Log.d(TAG_T, "onViewCreated  + thread= ${Thread.currentThread()}")

        }
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
                                             newItem: GalleryItem
                ) = oldItem.id == newItem.id

                override fun areContentsTheSame(oldItem: GalleryItem,
                                                newItem: GalleryItem
                ) = oldItem == newItem
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_photo_gallery, menu)

        val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
        val searchView = searchItem.actionView as SearchView
        searchView.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(queryText: String): Boolean {
                    Log.d(TAG, "QueryTextSubmit: $queryText")
                    photoGalleryViewModel.fetchPhotos(queryText)
                    return true
                }
                override fun onQueryTextChange(queryText: String): Boolean {
                    Log.d(TAG, "QueryTextChange: $queryText")
                    return false
                }
            })
            setOnSearchClickListener {
                searchView.setQuery(photoGalleryViewModel.searchTerm, false)
            }
        }
        val toggleItem = menu.findItem(R.id.menu_item_toggle_polling)
        val isPolling = QueryPreferences.isPolling(requireContext())
        val toggleItemTitle = if (isPolling) {
            R.string.stop_polling
        } else {
            R.string.start_polling
        }
        toggleItem.setTitle(toggleItemTitle)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_clear -> {
                photoGalleryViewModel.fetchPhotos("")
                true
            }
            R.id.menu_item_toggle_polling -> {
                val isPolling = QueryPreferences.isPolling(requireContext())
                if (isPolling) {
                    WorkManager.getInstance().cancelUniqueWork(POLL_WORK)
                    QueryPreferences.setPolling(requireContext(), false)
                } else {
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.UNMETERED)
                        .build()
                    val periodicRequest = PeriodicWorkRequest
                        .Builder(PollWorker::class.java, 15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build()
                    WorkManager.getInstance().enqueueUniquePeriodicWork(POLL_WORK,
                        ExistingPeriodicWorkPolicy.KEEP,
                        periodicRequest)
                    QueryPreferences.setPolling(requireContext(), true)
                }
                activity?.invalidateOptionsMenu()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}





