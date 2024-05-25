package com.the.weather

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.the.weather.databinding.FragmentHoursBinding
import org.json.JSONArray
import org.json.JSONObject


class Hours : Fragment() {

    private lateinit var binding: FragmentHoursBinding
    private lateinit var adapter: WetherAdapter


    private val model: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentHoursBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        model.liveDataCurrent.observe(viewLifecycleOwner) {

            adapter.submitList(getHoursList(it))
        }
    }

    private fun initRecyclerView() = with(binding) {

        rcView.layoutManager = LinearLayoutManager(activity)

        adapter = WetherAdapter(null)
        rcView.adapter = adapter

    }


    private fun getHoursList(wItem: WetherModel): List<WetherModel> {
        val horsArray =
            JSONArray(wItem.hours)
        val list = ArrayList<WetherModel>()


        for (i in 0 until horsArray.length()) {
            val item = WetherModel(
                wItem.city,

                (horsArray[i] as JSONObject).getString("time"),
                (horsArray[i] as JSONObject).getJSONObject("condition").getString("text"),
                (horsArray[i] as JSONObject).getString("temp_c"),

                "",
                "",
                (horsArray[i] as JSONObject).getJSONObject("condition").getString("icon"),
                ""

            )
            list.add(item)
        }
        return list

    }

    companion object {
        @JvmStatic
        fun newInstance() = Hours()
    }

}