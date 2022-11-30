package com.web3auth.custom_auth_wallet_app

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
        val theme =
            this.applicationContext.web3AuthWalletPreferences.getString(THEME, "Dark")
        val isLangChanged = this.applicationContext.web3AuthWalletPreferences.get(
            IS_LANGUAGE_CHANGED, false
        )
        setTheme(theme)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()
        val locale = Web3AuthUtils.getSystemLocale()
        setLocale(locale)
        if (isLangChanged) {
            val language =
                this.applicationContext.web3AuthWalletPreferences.getString(LANGUAGE, "English")
                    .toString()
            setLang(language)
        }
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

    private fun setTheme(theme: String?) {
        when (theme) {
            "Light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "Dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "System" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun setLang(language: String) {
        when (language) {
            getString(R.string.english) -> setLocale("en")
            getString(R.string.german) -> setLocale("de")
            getString(R.string.spanish) -> setLocale("es")
            getString(R.string.japanese) -> setLocale("ja")
            getString(R.string.korean) -> setLocale("ko")
            getString(R.string.mandarin) -> setLocale("zh")
        }
    }

    private fun setLocale(lang: String?) {
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