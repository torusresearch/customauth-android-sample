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
    private lateinit var switchDarkMode: SwitchCompat
    private lateinit var tvDarkModeStatus: AppCompatTextView
    private lateinit var spLannuage: AutoCompleteTextView

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
        setData()
        setUpSpinner()
        setUpSwitch()
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

        spLannuage.setText(language)
        val languages = resources.getStringArray(R.array.languages)
        val langAdapter: ArrayAdapter<String> =
            ArrayAdapter(this, R.layout.item_dropdown, languages)
        spLannuage.setAdapter(langAdapter)
        spLannuage.setOnItemClickListener { _, _, position, _ ->
            this.applicationContext.web3AuthWalletPreferences[IS_LANGUAGE_CHANGED] = true
            this.applicationContext.web3AuthWalletPreferences[LANGUAGE] =
                languages[position]
            restartApp()
        }
    }

    private fun setData() {
        spBlockChain = findViewById(R.id.spBlockChain)
        spLannuage = findViewById(R.id.spLanguage)
        tvNetwork = findViewById(R.id.tvNetwork)
        switchDarkMode = findViewById(R.id.switchDarkMode)
        tvDarkModeStatus = findViewById(R.id.tvDarkModeStatus)
        findViewById<AppCompatImageView>(R.id.ivBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        val darkModeStatus = this.applicationContext.web3AuthWalletPreferences.get(ISDARKMODE, true)
        switchDarkMode.isChecked = darkModeStatus
        tvDarkModeStatus.text =
            if (darkModeStatus) getString(R.string.on) else getString(R.string.off)
        tvNetwork.text = blockChain
    }

    private fun setUpSwitch() {
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                tvDarkModeStatus.text = getString(R.string.on)
                this.applicationContext.web3AuthWalletPreferences[ISDARKMODE] = true
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                tvDarkModeStatus.text = getString(R.string.off)
                this.applicationContext.web3AuthWalletPreferences[ISDARKMODE] = false
            }
        }
    }

    private fun restartApp() {
        val intent = Intent(this, SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        Runtime.getRuntime().exit(0)
    }


}