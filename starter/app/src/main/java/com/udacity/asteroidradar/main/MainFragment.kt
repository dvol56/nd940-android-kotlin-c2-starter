package com.udacity.asteroidradar.main

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.squareup.picasso.Picasso
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val binding = FragmentMainBinding.inflate(inflater)
        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        val adapter = MainAsteroidAdapter(AsteroidListener {
                asteroid -> viewModel.onAsteroidClicked(asteroid)
        })

        binding.asteroidRecycler.adapter = adapter

        viewModel.navigateToDetail.observe(viewLifecycleOwner, Observer { asteroid ->
            asteroid?.let {
                this.findNavController().navigate(MainFragmentDirections.actionShowDetail(asteroid))
                viewModel.onAsteroidNavigated()
            }

        })

        viewModel.asteroidData.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
                binding.statusLoadingWheel.visibility = View.GONE
            }
            if(it == null || it.isEmpty()) {
                binding.statusLoadingWheel.visibility = View.VISIBLE
            }
        })

        viewModel.dailyImage.observe(viewLifecycleOwner, Observer { pictureOfTheDay ->
            pictureOfTheDay?.let {
                Picasso.get().load(pictureOfTheDay.url).into(binding.activityMainImageOfTheDay)
                binding.activityMainImageOfTheDay.contentDescription = pictureOfTheDay.title
            }

            if(pictureOfTheDay == null) {
                binding.activityMainImageOfTheDay.setImageResource(R.drawable.placeholder_picture_of_day)
            }
        })

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        viewModel.filterAsteriods(
            when(item.itemId) {
                R.id.show_today_asteroids -> AsteroidFilter.TODAY
                R.id.show_week_asteroids -> AsteroidFilter.WEEK
                else -> AsteroidFilter.SAVED
            }
        )
        return true
    }
}
