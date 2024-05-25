package com.the.weather

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.the.weather.databinding.ListitemBinding

class WetherAdapter (val listener: WetherAdapter.Listener?):ListAdapter<WetherModel, WetherAdapter.Holder>(Comperator()) {

    class Holder(view: View, val listener:Listener?): RecyclerView.ViewHolder(view){
        val binding = ListitemBinding.bind(view)

        var itemTemp:WetherModel? = null
        init {
            itemView.setOnClickListener {



                itemTemp?.let { it1-> listener?.onClick(it1) }
            }
        }
        fun bind(item:WetherModel) = with(binding){
            itemTemp = item
            tvData.text = item.time
            tvCondition.text = item.condition

            tvTemp.text = item.currentTemp.ifEmpty  { "${item.maxTemp}º/ ${item.minTemp}º" }
            Picasso.get().load("https:"+item.imageUrl).into(im)
        }
    }

    class Comperator: DiffUtil.ItemCallback<WetherModel>(){
        override fun areItemsTheSame(oldItem: WetherModel, newItem: WetherModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: WetherModel, newItem: WetherModel): Boolean {
            return oldItem == newItem

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listitem, parent, false)
        return Holder(view,listener)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    interface Listener{
        fun onClick(item:WetherModel)
    }
}