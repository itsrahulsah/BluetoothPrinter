package com.techno.tron.bluetoothprinter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.techno.tron.bluetoothprinter.adapters.PairDeviceAdapter

class PairDeviceFragment(private val deviceList: ArrayList<String>, private var onClick:(deviceName:String)->Unit): DialogFragment() {
    private lateinit var listView:RecyclerView
    private lateinit var adapter :PairDeviceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL,R.style.DialogFragment)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView = view.findViewById(R.id.listView)
        adapter = PairDeviceAdapter(deviceList) {
            onClick.invoke(it)
            dismiss()
        }

        Log.e("TAG:",deviceList.size.toString())
        listView.layoutManager = LinearLayoutManager(requireContext())
        listView.adapter = adapter
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragmant_pair_device,container,false)
    }
}