package com.techno.tron.bluetoothprinter.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.techno.tron.bluetoothprinter.R

class PairDeviceAdapter(private val deviceList: ArrayList<String>, private var onClick:(deviceName:String)->Unit):RecyclerView.Adapter<PairDeviceAdapter.ViewHolder>() {
    inner class ViewHolder(val view:View):RecyclerView.ViewHolder(view){
        val deviceText = view.findViewById<TextView>(R.id.device_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val view = LayoutInflater.from(parent.context).inflate(R.layout.device_name,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return deviceList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.deviceText.text = deviceList[position]
        holder.view.setOnClickListener{
            onClick.invoke(deviceList[position])
        }
    }
}