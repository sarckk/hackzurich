package com.example.addictionapp.ui.apps

import com.example.addictionapp.data.models.Application
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.addictionapp.data.blocklist.BlocklistRepository
import com.example.addictionapp.data.models.ApplicationWithIcon
import kotlinx.coroutines.launch

class AppSelectionViewModel(
    val blocklistRepository: BlocklistRepository
) : ViewModel() {

    val blacklistedApplications: MutableList<Application> = mutableListOf()

    fun addToBlacklistedApps(name: String, packageName: String) {
        blacklistedApplications.add(Application(name, packageName))
    }

    fun removeFromBlacklistedApps(name: String, packageName: String) {
        blacklistedApplications.remove(Application(name, packageName))
    }

    fun getAppList(packageManager: PackageManager): List<ApplicationWithIcon> =
        packageManager.getInstalledApplications(PackageManager.GET_META_DATA).map { app ->
            ApplicationWithIcon (packageManager.getApplicationIcon(app),
                packageManager.getApplicationLabel(app) as String,
                app.packageName,
                { name, packageName -> addToBlacklistedApps(name, packageName) },
                { name, packageName -> removeFromBlacklistedApps(name, packageName) }
            )
        }

    fun upsertApplication(application: Application) = viewModelScope.launch {
        blocklistRepository.upsertApplication(application)
    }
}