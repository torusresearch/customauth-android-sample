package com.web3auth.custom_auth_wallet_app

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.web3auth.custom_auth_wallet_app.utils.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isDarkMode =
            this.applicationContext.web3AuthWalletPreferences.getBoolean(ISDARKMODE, true)
        setTheme(isDarkMode)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()
        val language =
            this.applicationContext.web3AuthWalletPreferences.getString(LANGUAGE, "English")
                .toString()
        setLang(language)
        GlobalScope.launch {
            delay(500)
            navigate()
        }
    }

    private fun navigate() {
        val loggedInFlag =
            this.applicationContext.web3AuthWalletPreferences.get(ISLOGGEDIN, false)
        val onboardedFlag =
            this.applicationContext.web3AuthWalletPreferences.get(ISONBOARDED, false)
        var intent = Intent(this@SplashActivity, LoginActivity::class.java)
        if (onboardedFlag) {
            intent = Intent(this@SplashActivity, MainActivity::class.java)
        } else if (loggedInFlag) {
            intent = Intent(this@SplashActivity, OnBoardingActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun setTheme(isDarkMode: Boolean) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun setLang(language: String) {
        when (language) {
            getString(R.string.english) -> setLocale(this, "en")
            getString(R.string.german) -> setLocale(this, "de")
            getString(R.string.spanish) -> setLocale(this, "es")
            getString(R.string.japanese) -> setLocale(this, "ja")
            getString(R.string.korean) -> setLocale(this, "ko")
            getString(R.string.mandarin) -> setLocale(this, "zh")
        }
    }

    private fun setLocale(activity: Activity, lang: String?) {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        baseContext.resources.updateConfiguration(
            config,
            baseContext.resources.displayMetrics
        )
    }
}