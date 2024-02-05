package com.busydoor.app.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.busydoor.app.R


class SplashActivity : ActivityBase() {
    private lateinit var mHandler: Handler
    private lateinit var mRunnable: Runnable
    private val screenTimeOut = 3000L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        redirectDashBord()
    }
    private fun redirectDashBord() {

        mRunnable = Runnable {
            run {
                if (objSharedPref.getBoolean(getString(R.string.isLogin))) {
                    val getStartedIntent =
                        Intent(this@SplashActivity, DashboardActivity::class.java)
                    getStartedIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    getStartedIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    getStartedIntent.putExtra("user_response",objSharedPref.getString(getString(R.string.userResponse)))
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                    startActivity(getStartedIntent)
                    finish()
                } else {
                    val getStartedIntent =
                        Intent(this@SplashActivity, LoginHomeActivity::class.java)
                    getStartedIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    getStartedIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                    startActivity(getStartedIntent)
                    finish()
                }

            }
        }
        mHandler = Handler(Looper.getMainLooper())
        mHandler.postDelayed(mRunnable, screenTimeOut)
    }
}