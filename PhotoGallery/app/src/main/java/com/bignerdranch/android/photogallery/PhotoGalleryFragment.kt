package com.bignerdranch.android.photogallery

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.photogallery.services.BindService
import com.bignerdranch.android.photogallery.services.ForegroundService
import com.bignerdranch.android.photogallery.services.SimpleService
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
        setHasOptionsMenu(true)

        photoGalleryViewModel = ViewModelProviders.of(this).get(PhotoGalleryViewModel::class.java)
        Log.d(TAG_T, "onCreate + thread= ${Thread.currentThread()}")
        adapter = PhotoAdapter(requireContext())

        val responseHandler = Handler()
        thumbnailDownloader = ThumbnailDownloader(responseHandler) { photoHolder, bitmap ->
            val drawable = BitmapDrawable(resources, bitmap)
            photoHolder.bindDrawable(drawable)
        }
        lifecycle.addObserver(thumbnailDownloader.fragmentLifecycleObserver)

    }

    private fun onPhotosLoaded(items: List<GalleryItem>) {
        // DataSource
        val dataSource = MyPositionalDataSource(items)
        Log.d(TAG_T, "onPhotosLoaded + thread= ${Thread.currentThread()}")

        // PagedList
        val config =
            PagedList.Config.Builder().setEnablePlaceholders(false).setInitialLoadSizeHint(20)
                .setPageSize(20).build()

        val pagedList: PagedList<GalleryItem?> = PagedList.Builder(dataSource, config)
            .setFetchExecutor(Executors.newSingleThreadExecutor())
            .setNotifyExecutor(MainThreadExecutor()).build()

        adapter?.submitList(pagedList)
    }

    private fun View.afterMeasured(f: View.() -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (measuredHeight > 0 && measuredWidth > 0) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    f()
                }
            }
        })
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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

        view.findViewById<View>(R.id.start_service).setOnClickListener {
            activity?.startService(Intent(context, SimpleService::class.java))
        }

        view.findViewById<View>(R.id.start_foreground_service).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context?.applicationContext?.startForegroundService(
                    Intent(
                        context, ForegroundService::class.java
                    )
                )
            }
        }

        view.findViewById<View>(R.id.bind_service).setOnClickListener {
            bindService()
        }

        view.findViewById<View>(R.id.unbind_service).setOnClickListener {
            unbindService()
        }

        view.findViewById<View>(R.id.bind_service_test_1).setOnClickListener {
            test1()
        }

        view.findViewById<View>(R.id.bind_service_test_2).setOnClickListener {
            test2()
        }


        photoRecyclerView.afterMeasured {
            val weightColum: Int =
                activity?.resources?.getDimension(R.dimen.column_average_width)!!.toInt()

            spanCount = photoRecyclerView.width / weightColum
            Log.d(TAG, "onCreateView - spanCount = " + spanCount)
            Log.d(TAG, "onCreateView - weightColum = " + weightColum)
            Log.d(TAG, "onCreateView - photoRecyclerView.width = " + photoRecyclerView.width)
            photoRecyclerView.layoutManager =
                GridLayoutManager(context, photoRecyclerView.width / weightColum)
        }
        return view

    }

    private lateinit var mService: BindService
    private var mBound: Boolean = false

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as BindService.SuperBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }


    private fun bindService() {
        Intent(activity, BindService::class.java).also { intent ->
            activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }


    private fun test1() {
        if (mBound) {
            showMessageFromServices(mService.makeTest1Text())
        } else {
            showBindServiceError()
        }
    }

    private fun showMessageFromServices(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun test2() {
        if (mBound) {
            showMessageFromServices(mService.makeTest2Text())
        } else {
            showBindServiceError()
        }
    }

    private fun showBindServiceError() {
        Toast.makeText(context, "Service is not bound", Toast.LENGTH_SHORT).show()
    }

    private fun unbindService() {
        activity?.unbindService(connection)
        mBound = false
    }

    override fun onStop() {
        super.onStop()
        unbindService()
    }

    companion object {
        fun newInstance() = PhotoGalleryFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated")
        photoGalleryViewModel.galleryItemLiveData.observe(
            viewLifecycleOwner
        ) { galleryItems ->
            Log.d(TAG, "onViewCreated - galleryItems" + galleryItems)
            photoRecyclerView.adapter = adapter
            onPhotosLoaded(galleryItems)
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
                context, R.drawable.bill_up_close
            ) ?: ColorDrawable()
            holder.bindDrawable(placeholder)
            if (itItem != null) {
                thumbnailDownloader.queueThumbnail(holder, itItem.url)
            }
            holder.itemView.post(Runnable {
                fun run() {
                    val cellWidth: Int =
                        holder.itemView.getWidth();// this will give you cell width dynamically
                    Log.d(TAG, "onBindViewHolder - cellWidth = " + cellWidth)
                    val cellHeight: Int =
                        holder.itemView.getHeight();// this will give you cell height dynamically
                }
            })

        }

        companion object {
            private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<GalleryItem>() {
                // Concert details may have changed if reloaded from the database,
                // but ID is fixed.
                override fun areItemsTheSame(
                    oldItem: GalleryItem, newItem: GalleryItem
                ) = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: GalleryItem, newItem: GalleryItem
                ) = oldItem == newItem
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
            Log.d(TAG, "onCreateViewHolder")
            val imageView = LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_gallery, parent, false
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
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_clear -> {
                photoGalleryViewModel.fetchPhotos("")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}





