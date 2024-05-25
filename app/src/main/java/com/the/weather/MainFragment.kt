package com.the.weather

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso
import com.the.weather.databinding.FragmentMainFragmnetBinding
import org.json.JSONObject

const val API_KEY = ""

class MainFragmnet : Fragment() {

    private lateinit var fLocationclient: FusedLocationProviderClient
    private lateinit var binding: FragmentMainFragmnetBinding
    private lateinit var pLauncher: ActivityResultLauncher<String>
    private val model: MainViewModel by activityViewModels()

    private var tlist = listOf(
        "Hours",
        "Days"
    )
    private var flist = listOf(
        Hours.newInstance(),
        Days.newInstance()
    )


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentMainFragmnetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermissinon()
        updateCurrentCard()
        init()

    }


    fun perpissionListener() {
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {

        }
    }


    private fun checkPermissinon() {

        if (!isPermissionGranded(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            perpissionListener()
            pLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }


    override fun onResume() {
        super.onResume()
        checkLocation()

    }


    private fun init() = with(binding) {

        fLocationclient = LocationServices.getFusedLocationProviderClient(requireContext())
        val adapter = VpAdapter(activity as FragmentActivity, flist)

        vp.adapter = adapter

        TabLayoutMediator(tablayout, vp) {
                tab, pos ->
            tab.text = tlist[pos]
        }.attach()


        ibSync.setOnClickListener {
            tablayout.selectTab(tablayout.getTabAt(0))
            checkLocation()


        }

        ibSearch.setOnClickListener {

            DialogManager.searchByNameDialog(requireContext(), object : DialogManager.Listener {

                override fun onClick(name: String?) {
                    name?.let { it1 -> requestWetherData(it1) }
                }

            })
        }

    }



    private fun checkLocation() {
        if (isLocationEnabled()) {
            getLocation()
        } else {
            DialogManager.locationSetingsDialog(requireContext(), object : DialogManager.Listener {
                override fun onClick(name: String?) {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }

            })
        }
    }


    private fun isLocationEnabled(): Boolean {
        val lm = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }



    private fun getLocation() {

        val ct = CancellationTokenSource()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fLocationclient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, ct.token)
            .addOnCompleteListener {
                requestWetherData("${it.result.latitude},${it.result.longitude}")
            }

    }

    private fun updateCurrentCard() = with(binding) {


        model.liveDataCurrent.observe(viewLifecycleOwner) { item ->

            val maxMinTemp =
                "${item.maxTemp}º/${item.minTemp}º"
            tvData.text = item.time
            tvCity.text = item.city
            tvCurrentTemp.text =
                item.currentTemp.ifEmpty { maxMinTemp }

            tvCondition.text = item.condition

            if (tvCurrentTemp.text.equals(maxMinTemp)) {
                celsius.visibility = View.GONE
            }
            else {
                celsius.visibility = View.VISIBLE

            }

            tvMaxMin.text =
                if (item.currentTemp.isEmpty()) "" else maxMinTemp
            Picasso.get().load("https:" + item.imageUrl).into(imWeather)

        }
    }


    private fun requestWetherData(city: String) {
        val url = "https://api.weatherapi.com/v1/forecast.json" +
                "?key=$API_KEY&q=$city&days=3&api=no&alerts=no"
        val queue = Volley.newRequestQueue(context)

        val stringRequest = StringRequest(Request.Method.GET,
            url,

            {
                    response ->
                parseWetherData(response)
            },
            { error ->
                Log.d("MyLog", " error:$error")
            }
        )
        queue.add(stringRequest)
    }


    private fun parseWetherData(result: String) {
        val mainObject =
            JSONObject(result)
        val list = parseDays(mainObject)

        parseCurrentData(mainObject,
            list[0])

    }

    private fun parseDays(mainObject: JSONObject): List<WetherModel> {
        val list =
            ArrayList<WetherModel>()
        val daysArray = mainObject.getJSONObject("forecast").getJSONArray("forecastday")
        val name = mainObject.getJSONObject("location").getString("name")
        for (i in 0 until daysArray.length()) {
            val day = daysArray[i] as JSONObject
            val item = WetherModel(
                name,
                day.getString("date"),
                day.getJSONObject("day").getJSONObject("condition").getString("text"),
                "",
                day.getJSONObject("day").getString("maxtemp_c").toFloat().toInt()
                    .toString(),
                day.getJSONObject("day").getString("mintemp_c").toFloat().toInt().toString(),
                day.getJSONObject("day").getJSONObject("condition").getString("icon"),
                day.getJSONArray("hour")
                    .toString()
            )
            list.add(item)
        }

        model.liveDataList.value = list
        return list
    }


    private fun parseCurrentData(mainObject: JSONObject, wetherItem: WetherModel) {
        val item = WetherModel(
            mainObject.getJSONObject("location")
                .getString("name"),
            mainObject.getJSONObject("current").getString("last_updated"),
            mainObject.getJSONObject("current").getJSONObject("condition").getString("text"),
            mainObject.getJSONObject("current").getString("temp_c"),
            wetherItem.maxTemp,
            wetherItem.minTemp,
            mainObject.getJSONObject("current").getJSONObject("condition").getString("icon"),
            wetherItem.hours

        )
        model.liveDataCurrent.value = item

    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragmnet()
    }
}
