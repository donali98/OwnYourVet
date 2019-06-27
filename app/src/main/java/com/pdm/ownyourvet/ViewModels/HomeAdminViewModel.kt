package com.pdm.ownyourvet.ViewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.pdm.ownyourvet.Network.Models.users.SingleUserResponse
import com.pdm.ownyourvet.Network.UserService
import com.pdm.ownyourvet.Repositories.UserRepo
import com.pdm.ownyourvet.Room.Entities.User
import com.pdm.ownyourvet.Room.RoomDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeAdminViewModel(private val app: Application) : AndroidViewModel(app) {
    private val userRepo: UserRepo
    val userLiveData:MutableLiveData<User> = MutableLiveData()

    init {
        val userDao = RoomDB.getInstance(app).userDao()
        userRepo = UserRepo(userDao)
    }

    fun getUserByType(type: String) = userRepo.getUserByType(type)

    fun retreiveUserById(id:String) = viewModelScope.launch(Dispatchers.IO){
        val resp = UserService.getUserService().getUserById(id).await()
        if(resp.isSuccessful) with(resp){
            Log.d("REQUESTS","User retrieved")
            withContext(Dispatchers.Main){
                userLiveData.postValue(resp.body()!!.data)
            }
        }else Log.e("REQUESTS","User not retreived")
    }
    fun retrieveClients() = viewModelScope.launch(Dispatchers.IO) {
        val resp = UserService.getUserService().getClients().await()
        if (resp.isSuccessful) with(resp) {
            Log.d("REQUESTS","Clients retrieved")
            userRepo.insertUsers(this.body()!!.data)
        }
    }

    fun sendUser(user: User) = viewModelScope.launch(Dispatchers.IO) {
        val api = UserService.getUserService()
        val usrResp = api.getUserById(user.id).await()
        if (!usrResp.isSuccessful) with(usrResp) {

            if (this.code() == 404) {
                val resp = api.saveUser(user.id, user.email, user.userType, user.names, user.direction).await()
                if (resp.isSuccessful) {
                    Log.d("REQUESTS", "user ${user.email} saved into api")
                    userRepo.insertUser(user)
                } else with(resp) {
                    if (this.code() == 404)
                        Log.e("REQUESTS", this.message())
                }
            }
        }

    }
}