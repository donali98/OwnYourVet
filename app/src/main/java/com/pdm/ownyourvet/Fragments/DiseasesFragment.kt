package com.pdm.ownyourvet.Fragments


import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.pdm.ownyourvet.Adapters.DiseaseAdapter

import com.pdm.ownyourvet.R
import com.pdm.ownyourvet.Room.Entities.Diseases
import com.pdm.ownyourvet.ViewModels.DiseasesViewModel
import com.pdm.ownyourvet.isConnected
import kotlinx.android.synthetic.main.custom_popup.*
import kotlinx.android.synthetic.main.custom_popup.view.*
import kotlinx.android.synthetic.main.fragment_diseases.view.*

class DiseasesFragment : Fragment() {

    private lateinit var diseasesViewModel:DiseasesViewModel
    private val spinnerOptions = arrayOf("Todos", "Gatos", "Perros")
    private val spinnerOptionsId = longArrayOf(0,12,2)
    private lateinit var spinner: Spinner
    private lateinit var dialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_diseases, container, false)
        init(view)
        return view
    }

    fun init(view: View){
        diseasesViewModel = ViewModelProviders.of(this).get(DiseasesViewModel::class.java)

        var adapter = object : DiseaseAdapter(view.context){
            override fun setClickListenerToDisease(holder: DiseasesViewHolder, disease: Diseases) {
                holder.linearLayout_disease.setOnClickListener {
                    showPopUp(view, disease)
                }
            }
        }
        val recyclerView = view.recyclerViewDiseases
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(view.context)

        spinner = view.diseaseSpinner
        val arrayAdapter = ArrayAdapter(view.context, R.layout.custom_spinner, spinnerOptions)
        spinner.adapter = arrayAdapter as SpinnerAdapter?

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view2: View?, position: Int, id: Long) {
                Log.d("CODIGO", "SPECIE: "+spinnerOptions[position]+ " ID: "+spinnerOptionsId[position])
                if (spinnerOptionsId[position].toInt()==0){
                    diseasesViewModel.allDiseases.observe(this@DiseasesFragment, Observer { diseases ->
                        diseases?.let { adapter.setDiseases(it) }
                    })
                } else {
                    diseasesViewModel.updateDiseasesBySpecie(spinnerOptionsId[position])
                    diseasesViewModel.diseasesBySpecie?.observe(this@DiseasesFragment, Observer { diseases ->
                        diseases?.let { adapter.setDiseases(it) }
                    })
                }
            }
        }

        if (isConnected(view.context)){
            diseasesViewModel.retrieveMovies()
        }

        diseasesViewModel.allDiseases.observe(this, Observer { diseases ->
            diseases?.let { adapter.setDiseases(it) }
        })

        view.button_refresh_diseases.setOnClickListener {
            if (isConnected(view.context)){
                diseasesViewModel.deleteDiseases()
                diseasesViewModel.retrieveMovies()
                Toast.makeText(view.context, "Datos actualizados", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(view.context, "Sin conexión a internet", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun showPopUp(view: View, disease:Diseases){
        dialog = Dialog(view.context)
        dialog.setContentView(R.layout.custom_popup)
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.popup_name.text = disease.name
        dialog.popup_information.text = disease.information
        if (disease.specie_id.toInt()==12){
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
