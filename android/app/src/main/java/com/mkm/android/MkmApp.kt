package com.mkm.android

import android.app.Application
import com.mkm.android.data.remote.RetrofitClient

class MkmApp : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitClient.init(this)
    }
}
