package com.freecode.mobile

import android.app.Application
import com.freecode.mobile.data.local.AppDatabase

class FreeCodeApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.create(this) }
}
