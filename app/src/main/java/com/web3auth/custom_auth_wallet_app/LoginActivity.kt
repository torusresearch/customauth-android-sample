package com.web3auth.custom_auth_wallet_app

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.web3auth.custom_auth_wallet_app.utils.BLOCKCHAIN
import com.web3auth.custom_auth_wallet_app.utils.ISLOGGEDIN
import com.web3auth.custom_auth_wallet_app.utils.NETWORK
import com.web3auth.custom_auth_wallet_app.utils.web3AuthWalletPreferences
import com.web3auth.custom_auth_wallet_app.utils.*

class LoginActivity : AppCompatActivity() {

    private lateinit var networkSpinner: AutoCompleteTextView
    private lateinit var blockChainSpinner: AutoCompleteTextView
    private lateinit var networks: Array<String>
    private lateinit var blockchains: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_login)
        setUpSpinner()
        findViewById<AppCompatButton>(R.id.btnLogin).setOnClickListener {
            startActivity(Intent(this@LoginActivity, OnBoardingActivity::class.java))
            this.applicationContext.web3AuthWalletPreferences[ISLOGGEDIN] = true
            finish()
        }
    }

    private fun setUpSpinner() {
        networks = resources.getStringArray(R.array.networks)
        blockchains = resources.getStringArray(R.array.blockchains)

        networkSpinner = findViewById(R.id.spNetwork)
        val networkAdapter: ArrayAdapter<String> =
            ArrayAdapter(this, R.layout.item_dropdown, networks)
        networkSpinner.setAdapter(networkAdapter)
        networkSpinner.setOnItemClickListener { _, _, position, _ ->
            this.applicationContext.web3AuthWalletPreferences[NETWORK] = networks[position]
        }

        blockChainSpinner = findViewById(R.id.spBlockChain)
        val adapter: ArrayAdapter<String> =
            ArrayAdapter(this, R.layout.item_dropdown, blockchains)
        blockChainSpinner.setAdapter(adapter)
        blockChainSpinner.setOnItemClickListener { _, _, position, _ ->
            this.applicationContext.web3AuthWalletPreferences[BLOCKCHAIN] =
                blockchains[position]
        }
    }
}