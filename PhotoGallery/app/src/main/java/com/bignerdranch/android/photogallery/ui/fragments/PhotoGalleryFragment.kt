package com.bignerdranch.android.photogallery.ui.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.bignerdranch.android.photogallery.QueryPreferences
import com.bignerdranch.android.photogallery.R
import com.bignerdranch.android.photogallery.domain.managers.workmanager.PollWorker
import com.bignerdranch.android.photogallery.domain.models.Photo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

private const val TAG = "PhotoGalleryFragment"
private const val TAG_T = "Thread"
private const val POLL_WORK = "POLL_WORK"

class PhotoGalleryFragment : Fragment() {
    private lateinit var photoGalleryViewModel: PhotoGalleryViewModel
    private lateinit var photoRecyclerView: RecyclerView

    // High level definition
    private var searchJob: Job? = null
    var spanCount: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setHasOptionsMenu(true)

        photoGalleryViewModel =
            ViewModelProviders.of(this).get(PhotoGalleryViewModel::class.java)
        Log.d(TAG_T, "onCreate + thread= ${Thread.currentThread()}")

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
        photoRecyclerView.adapter = photoGalleryViewModel.photosAdapter
        photoRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

        photoGalleryViewModel.loadPhotos().observe(viewLifecycleOwner,
            Observer<List<Photo>> { list ->
                with(photoGalleryViewModel.photosAdapter) {
                    photos.clear()
                    photos.addAll(list)
                    notifyDataSetChanged()
                }
            })

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_photo_gallery, menu)

        val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
        val searchView = searchItem.actionView as SearchView
        searchView.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(queryText: String): Boolean {
                    searchJob?.cancel()
                    searchJob = lifecycleScope.launch {
                        try {
                            delay(1000)

                            val imagesList = photoGalleryViewModel.fetchImages(queryText)
                            with(photoGalleryViewModel.photosAdapter) {
                                photos.clear()
                                photos.addAll(imagesList)
                                notifyDataSetChanged()
                            }
                        } catch (e: Exception) {
                            Log.d("CheckTestException", "Exception = $e")
                        }

                    }
                    Log.d(TAG, "QueryTextSubmit: $queryText")

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





