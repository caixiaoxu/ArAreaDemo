package com.lsy.arareademo

import android.app.Application
import com.baidu.mapapi.SDKInitializer

/**
 * @author Xuwl
 * @date 2021/11/10
 *
 */
class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        SDKInitializer.initialize(this)
    }
}