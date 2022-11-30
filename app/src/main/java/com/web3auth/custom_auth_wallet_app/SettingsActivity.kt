package com.web3auth.custom_auth_wallet_app

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SwitchCompat
import com.web3auth.custom_auth_wallet_app.utils.*

class SettingsActivity : AppCompatActivity() {

    private lateinit var spBlockChain: AutoCompleteTextView
    private lateinit var tvNetwork: AppCompatTextView
    private lateinit var blockChain: String
    private lateinit var network: String
    private lateinit var language: String
    private lateinit var theme: String
    private lateinit var spLanguage: AutoCompleteTextView
    private lateinit var spTheme: AutoCompleteTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.hide()

        blockChain = this.applicationContext.web3AuthWalletPreferences.getString(
            BLOCKCHAIN,
            "ETH Mainnet"
        ).toString()
        network =
            this.applicationContext.web3AuthWalletPreferences.getString(NETWORK, "Mainnet")
                .toString()
        language = this.applicationContext.web3AuthWalletPreferences.getString(LANGUAGE, "English")
            .toString()
        theme = this.applicationContext.web3AuthWalletPreferences.getString(THEME, "Dark")
            .toString()
        setData()
        setUpSpinner()
    }

    private fun setUpSpinner() {
        spBlockChain.setText(blockChain)
        val blockchains = resources.getStringArray(R.array.blockchains)
        val adapter: ArrayAdapter<String> =
            ArrayAdapter(this, R.layout.item_dropdown, blockchains)
        spBlockChain.setAdapter(adapter)
        spBlockChain.setOnItemClickListener { _, _, position, _ ->
            this.applicationContext.web3AuthWalletPreferences[BLOCKCHAIN] =
                blockchains[position]
            tvNetwork.text = blockchains[position]
        }

        spLanguage.setText(language)
        val languages = resources.getStringArray(R.array.languages)
        val langAdapter: ArrayAdapter<String> =
            ArrayAdapter(this, R.layout.item_dropdown, languages)
        spLanguage.setAdapter(langAdapter)
        spLanguage.setOnItemClickListener { _, _, position, _ ->
            this.applicationContext.web3AuthWalletPreferences[IS_LANGUAGE_CHANGED] = true
            this.applicationContext.web3AuthWalletPreferences[LANGUAGE] =
                languages[position]
            restartApp()
        }

        spTheme.setText(theme)
        val themes = resources.getStringArray(R.array.themes)
        val themeAdapter: ArrayAdapter<String> =
            ArrayAdapter(this, R.layout.item_dropdown, themes)
        spTheme.setAdapter(themeAdapter)
        spTheme.setOnItemClickListener { _, _, position, _ ->
            this.applicationContext.web3AuthWalletPreferences[THEME] =
                themes[position]
            when (themes[position]) {
                "Light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                "Dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                "System" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    private fun setData() {
        spBlockChain = findViewById(R.id.spBlockChain)
        spLanguage = findViewById(R.id.spLanguage)
        spTheme = findViewById(R.id.spTheme)
        tvNetwork = findViewById(R.id.tvNetwork)
        findViewById<AppCompatImageView>(R.id.ivBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun restartApp() {
        val intent = Intent(this, SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        Runtime.getRuntime().exit(0)
    }
}