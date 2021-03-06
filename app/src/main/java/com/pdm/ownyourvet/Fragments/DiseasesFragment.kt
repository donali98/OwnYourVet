package com.pdm.ownyourvet.Fragments


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.UiThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.viewModelScope
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pdm.ownyourvet.Adapters.diseases.DiseaseAdapter
import com.pdm.ownyourvet.Adapters.diseases.DiseaseDataSource
import com.pdm.ownyourvet.Adapters.diseases.DiseasesBoundaryCallback

import com.pdm.ownyourvet.R
import com.pdm.ownyourvet.Room.Entities.Diseases
import com.pdm.ownyourvet.Room.RoomDB
import com.pdm.ownyourvet.Utils.ActivityHelper
import com.pdm.ownyourvet.ViewModels.DiseasesViewModel
import com.pdm.ownyourvet.isConnected
import kotlinx.android.synthetic.main.custom_popup.*
import kotlinx.android.synthetic.main.fragment_diseases.view.*

class DiseasesFragment : Fragment() {


    private lateinit var diseasesViewModel: DiseasesViewModel
    private lateinit var spinner: Spinner
    private lateinit var dialog: Dialog
    val spinnerOptions = arrayListOf<String>()
    val spinnerOptionsId = arrayListOf<Long>()
    private lateinit var  pagedList:LiveData<PagedList<Diseases>>



    private lateinit var activityHelper: ActivityHelper
    private lateinit var myAdapter: DiseaseAdapter
    val config = PagedList.Config.Builder()
            .setInitialLoadSizeHint(5)
            .setPrefetchDistance(5)
            .setPageSize(5)
            .build()


    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityHelper = context as ActivityHelper
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_diseases, container, false)

        try{
            // Inflate the layout for this fragment
            init(view)

        }catch (e:Exception){
            Log.e("CUSTOM",e.toString())
        }
        return view

    }

    private fun initializedPagedListBuilder(
            config: PagedList.Config,
            byId: Long = 0
    ): LivePagedListBuilder<Int, Diseases> {
        val db = activityHelper.getDbFromMain()
        val livePageListBuilder = if (byId.toInt() == 0)
            LivePagedListBuilder(db.diseasesDao().getAllDiseases(), config)
        else {
            LivePagedListBuilder(db.diseasesDao().getDiseasesBySpecieId(byId), config)
        }
        livePageListBuilder.setBoundaryCallback(DiseasesBoundaryCallback(db,activityHelper))
        return livePageListBuilder
    }


    fun init(view: View) {
        diseasesViewModel = ViewModelProviders.of(this).get(DiseasesViewModel::class.java)

        if(isConnected(view.context)){
            diseasesViewModel.retrieveSpecies()
        }
        else Log.e("CUSTOM","No ay inter prro :v")

        diseasesViewModel.getAllSpecies().observe(this, Observer {

            if (it.isNotEmpty()) {
                spinnerOptionsId.clear()
                spinnerOptions.clear()
                spinnerOptionsId.add(0)
                spinnerOptions.add("Ninguno")
                it.forEach { specie ->
                    spinnerOptions.add(specie.name)
                    spinnerOptionsId.add(specie.id)
                }

                spinner.adapter = ArrayAdapter(view.context, R.layout.custom_spinner, spinnerOptions) as SpinnerAdapter
            }
        })


        myAdapter = object : DiseaseAdapter(activityHelper) {
            override fun setClickListenerToDisease(holder: DiseasesViewHolder, disease: Diseases) {
                holder.linearLayout_disease.setOnClickListener {
                    showPopUp(view, disease)
                }
            }
        }
        val recyclerView = view.recyclerViewDiseases

        recyclerView.apply {
            adapter = myAdapter
            layoutManager = LinearLayoutManager(view.context) as RecyclerView.LayoutManager
            pagedList = initializedPagedListBuilder(config).build()
            pagedList.observe(this@DiseasesFragment, Observer {
                myAdapter.submitList(it)
            })

        }
        spinner = view.diseaseSpinner



        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view2: View?, position: Int, id: Long) {
                if (spinnerOptionsId[position].toInt() == 0) {
                   try{
                       recyclerView.apply {
//                           layoutManager = LinearLayoutManager(view.context) as RecyclerView.LayoutManager
                           pagedList.removeObservers (this@DiseasesFragment)
                           pagedList = initializedPagedListBuilder(config).build()
                           pagedList.observe(this@DiseasesFragment, Observer {
                               myAdapter.submitList(it)
                           })

                       }
                   }catch (e:Exception){
                       Log.e("CUSTOM",e.toString())
                   }

                } else {
                    recyclerView.apply {
//                        layoutManager = LinearLayoutManager(view.context) as RecyclerView.LayoutManager
                        pagedList.removeObservers (this@DiseasesFragment)
                        pagedList = initializedPagedListBuilder(config,spinnerOptionsId[position]).build()
                        pagedList.observe(this@DiseasesFragment, Observer {
                            myAdapter.submitList(it)
                        })

                    }
                }
            }
        }


        view.button_refresh_diseases.setOnClickListener {
            if (isConnected(view.context)) {
                diseasesViewModel.deleteSpecies().invokeOnCompletion {
                    if(isConnected(view.context)){
                        diseasesViewModel.retrieveSpecies()

                    }
                    else Toast.makeText(view.context,"Sin conexión a internet",Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(view.context, "Sin conexión a internet", Toast.LENGTH_LONG).show()
            }
        }
    }


    fun showPopUp(view: View, disease: Diseases) {
        dialog = Dialog(view.context)
        dialog.setContentView(R.layout.custom_popup)
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.popup_name.text = disease.name
        dialog.popup_information.text =  disease.information
        if (disease.specie_id.toInt() == 12) {
            dialog.popup_specie.text = getString(R.string.animales_afectado_perros)
        } else {
            dialog.popup_specie.text = getString(R.string.animales_afectados_gatos)
        }

        dialog.textView_close_popup.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

}
