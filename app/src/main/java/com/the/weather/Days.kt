package com.the.weather

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.the.weather.databinding.FragmentDaysBinding


class Days : Fragment(), WetherAdapter.Listener {

    private lateinit var binding: FragmentDaysBinding
    private lateinit var adapter:WetherAdapter
    private val model: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentDaysBinding.inflate(inflater,container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        model.liveDataList.observe(viewLifecycleOwner) {

            adapter.submitList(it.subList(1,it.size))
        }
    }

    private fun init() = with(binding){
        adapter = WetherAdapter(this@Days)
        rcViewDays.layoutManager = LinearLayoutManager(activity)
        rcViewDays.adapter = adapter
    }

    companion object {
        @JvmStatic
        fun newInstance() = Days()
    }

    override fun onClick(item: WetherModel) {


        model.liveDataCurrent.value = item
    }
}