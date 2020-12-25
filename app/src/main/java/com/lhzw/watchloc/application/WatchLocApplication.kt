package com.lhzw.watchloc.application

import android.app.Application
import android.content.Context
import android.provider.CalendarContract
import java.lang.ref.WeakReference

/**
 *
@author：created by xtqb
@description:
@date : 2019/10/24 13:59
 *
 */

class WatchLocApplication : Application(){
    private lateinit var mWeakReference : WeakReference<Context>
    override fun onCreate() {
        super.onCreate()
        instances = this@WatchLocApplication
        mWeakReference = WeakReference(applicationContext)
    }

    fun getContext() = mWeakReference.get()

    companion object {
        lateinit var instances: WatchLocApplication
        fun getInstance() = instances
    }

}