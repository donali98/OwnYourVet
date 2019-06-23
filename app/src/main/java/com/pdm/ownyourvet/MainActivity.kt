package com.pdm.ownyourvet

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.pdm.ownyourvet.Activities.LoginActivity
import com.pdm.ownyourvet.Fragments.ChatFragment
import com.pdm.ownyourvet.Utils.ActivityHelper
import com.pdm.ownyourvet.ViewModels.DiseasesViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), ChatFragment.OnFragmentInteractionListener,ActivityHelper {



    lateinit var diseasesViewModel: DiseasesViewModel

    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val mAuth: FirebaseAuth by lazy {FirebaseAuth.getInstance()}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)

        setUpBottomNavMenu(navController)
        setUpSideNavigationMenu(navController)
        setUpActionBar(navController)

        diseasesViewModel = ViewModelProviders.of(this).get(DiseasesViewModel::class.java)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.exit -> {
                mAuth.signOut()
                goToActivity<LoginActivity>()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(Navigation.findNavController(this, R.id.nav_host_fragment), drawer_layout)
    }

    private fun setUpBottomNavMenu(navController: NavController){
        /*bottom_nav?.let {
            NavigationUI.setupWithNavController(it, navController)
        }*/
    }

    private fun setUpSideNavigationMenu(navController: NavController){
        nav_view?.let {
            NavigationUI.setupWithNavController(it, navController)
        }
    }

    private fun setUpActionBar(navController: NavController){
        NavigationUI.setupActionBarWithNavController(this, navController, drawer_layout)
    }
    override fun getLinearLayoutManagerWithContext(): LinearLayoutManager = LinearLayoutManager(this)

    override fun setSpecieOnListItem(textView: TextView, id: Long) {
//        textView.text = info
        diseasesViewModel.getSpecieByRelation(id).observe(this, Observer {
            Log.d("CUSTOM","listened")
            textView.text = it?.name
        })
    }

}
