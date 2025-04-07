package com.example.nutrifill

import android.app.Application
import android.os.StrictMode

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Configure StrictMode for development
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectNetwork()          // Detect network operations on main thread
                    .detectCustomSlowCalls()  // Detect slow custom code
                    .permitDiskReads()        // Allow disk reads
                    .permitDiskWrites()       // Allow disk writes for now
                    .penaltyLog()            // Log violations
                    .build()
            )
            
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .detectActivityLeaks()
                    .detectLeakedRegistrationObjects()
                    .penaltyLog()            // Log violations
                    .build()
            )
        }
        
        // Application startup initialization complete
    }
}