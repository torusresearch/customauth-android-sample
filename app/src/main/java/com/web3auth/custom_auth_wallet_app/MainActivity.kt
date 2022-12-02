package com.web3auth.custom_auth_wallet_app

import android.app.Dialog
import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.web3auth.custom_auth_wallet_app.utils.*
import com.web3auth.custom_auth_wallet_app.viewmodel.EthereumViewModel
import com.web3auth.custom_auth_wallet_app.viewmodel.SolanaViewModel
import org.torusresearch.customauth.types.TorusLoginResponse

class MainActivity : AppCompatActivity() {

    private var torusLoginResponse: TorusLoginResponse? = null
    private lateinit var blockChain: String
    private lateinit var publicAddress: String
    private lateinit var selectedNetwork: String
    private lateinit var tvExchangeRate: AppCompatTextView
    private lateinit var tvViewTransactionStatus: AppCompatTextView
    private lateinit var spCurrency: Spinner
    private var priceInUSD: String = ""
    private var balance: Long = 0L
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var etMessage: AppCompatEditText
    private lateinit var btnSign: AppCompatButton
    private lateinit var tvBalance: AppCompatTextView
    private lateinit var tvNetwork: AppCompatTextView
    private lateinit var tvPriceInUSD: AppCompatTextView
    private lateinit var progressDialog: ProgressDialog
    private lateinit var ethereumViewModel: EthereumViewModel
    private lateinit var solanaViewModel: SolanaViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        init()
    }

    private fun showProgressDialog() {
        progressDialog = ProgressDialog(this@MainActivity)
        progressDialog.setMessage(getString(R.string.loading_balance))
        progressDialog.setCancelable(false)
        progressDialog.show()
    }

    private fun init() {
        selectedNetwork =
            this.applicationContext.web3AuthWalletPreferences.getString(
                NETWORK,
                getString(R.string.mainnet)
            )
                .toString()
        blockChain = this.applicationContext.web3AuthWalletPreferences.getString(
            BLOCKCHAIN,
            getString(R.string.ethereum)
        ).toString()
        torusLoginResponse =
            this.applicationContext.web3AuthWalletPreferences.getObject(LOGIN_RESPONSE)

        showProgressDialog()

        if (blockChain.contains(getString(R.string.solana))) {
            solanaViewModel = ViewModelProvider(this)[SolanaViewModel::class.java]
            solanaViewModel.setNetwork(
                NetworkUtils.getSolanaNetwork(blockChain),
                torusLoginResponse?.privateKey.toString()
            )
            solanaViewModel.getCurrencyPriceInUSD(
                Web3AuthUtils.getCurrency(blockChain),
                getString(R.string.usd)
            )
            solanaViewModel.getPublicAddress()
        } else {
            ethereumViewModel = ViewModelProvider(this)[EthereumViewModel::class.java]
            ethereumViewModel.configureWeb3j(blockChain)
        }

        if (!Web3AuthUtils.isNetworkAvailable(this@MainActivity)) {
            progressDialog.dismiss()
            longToast(getString(R.string.connect_to_internet))
            return
        }
        setData()
        configureWeb3Auth()
        observeListeners()
        setUpListeners()
    }

    private fun configureWeb3Auth() {
        if (!blockChain.contains(getString(R.string.solana))) {
            ethereumViewModel.getPublicAddress(torusLoginResponse?.publicAddress.toString())
        }

        findViewById<AppCompatTextView>(R.id.tvName).text =
            getString(R.string.welcome).plus(" ").plus(
                torusLoginResponse?.userInfo?.name?.split(" ")?.get(0)
            ).plus("!")

        val tvEmail = findViewById<AppCompatTextView>(R.id.tvEmail)
        tvEmail.text = torusLoginResponse?.userInfo?.email
        val loginType =
            this.applicationContext.web3AuthWalletPreferences.getString(LOGINTYPE, "")
        tvEmail.addLeftDrawable(Web3AuthUtils.getSocialLoginIcon(this, loginType.toString()))
        setData()
    }

    private fun observeListeners() {
        if (!blockChain.contains(getString(R.string.solana))) {
            ethereumViewModel.isWeb3Configured.observe(this) {
                if (it == false) {
                    toast(getString(R.string.web3j_connection_error))
                }
            }

            ethereumViewModel.priceInUSD.observe(this) {
                if (!it.isNullOrEmpty()) {
                    priceInUSD = it
                    this.applicationContext.web3AuthWalletPreferences[PRICE_IN_USD] =
                        priceInUSD
                    tvExchangeRate.text =
                        "1 ".plus(Web3AuthUtils.getCurrency(blockChain)).plus(" = ")
                            .plus(priceInUSD).plus(" " + getString(R.string.usd))
                    if (this::publicAddress.isInitialized && publicAddress.isNotEmpty()) {
                        ethereumViewModel.retrieveBalance(publicAddress)
                        showPriceInUSD()
                    }
                }
            }

            ethereumViewModel.error.observe(this) {
                if (it) {
                    toast(getString(R.string.something_went_wrong))
                }
            }

            ethereumViewModel.publicAddress.observe(this) {
                publicAddress = it
                findViewById<AppCompatTextView>(R.id.tvAddress).text =
                    publicAddress.take(3).plus("...").plus(publicAddress.takeLast(4))
                this.applicationContext.web3AuthWalletPreferences[PUBLICKEY] = publicAddress
                if (publicAddress.isNotEmpty()) {
                    ethereumViewModel.retrieveBalance(publicAddress)
                }
            }

            ethereumViewModel.balance.observe(this) { it ->
                if (it > 0.0 && priceInUSD.isNotEmpty()) {
                    tvBalance.text = Web3AuthUtils.toWeiEther(it).roundOff()
                    showPriceInUSD()
                } else {
                    tvBalance.text = "0"
                }
                progressDialog.dismiss()
            }
        } else {
            solanaViewModel.priceInUSD.observe(this) {
                if (it != null && it.isNotEmpty()) {
                    priceInUSD = it
                    this.applicationContext.web3AuthWalletPreferences[PRICE_IN_USD] =
                        priceInUSD
                    tvExchangeRate.text =
                        "1 ".plus(Web3AuthUtils.getCurrency(blockChain)).plus(" = ")
                            .plus(priceInUSD).plus(" " + getString(R.string.usd))
                    showPriceInUSD()
                }
                progressDialog.dismiss()
            }
            solanaViewModel.publicAddress.observe(this) {
                if (it.isNotEmpty()) {
                    publicAddress = it
                    findViewById<AppCompatTextView>(R.id.tvAddress).text =
                        publicAddress.take(3).plus("...").plus(publicAddress.takeLast(4))
                    this.applicationContext.web3AuthWalletPreferences[PUBLICKEY] =
                        publicAddress
                    solanaViewModel.getBalance(publicAddress)
                }
            }
            solanaViewModel.privateKey.observe(this) {}
            solanaViewModel.balance.observe(this) {
                if (it > 0) {
                    balance = it
                    tvBalance.text = String.format("%.4f", it.roundOffLong())
                    showPriceInUSD()
                } else {
                    tvBalance.text = "0"
                }
            }
            solanaViewModel.signature.observe(this) {
                if (it?.isNotEmpty() == true) {
                    showSignatureResult(it)
                }
            }

            solanaViewModel.error.observe(this) {
                if (it) {
                    toast(getString(R.string.something_went_wrong))
                }
            }
        }
    }

    private fun setUpListeners() {
        tvExchangeRate = findViewById(R.id.tvExchangeRate)
        etMessage = findViewById(R.id.etMessage)
        btnSign = findViewById(R.id.btnSign)
        tvBalance = findViewById(R.id.tvBalance)
        tvPriceInUSD = findViewById(R.id.tvPriceInUSD)
        swipeRefreshLayout = findViewById(R.id.swipeRefresh)
        findViewById<AppCompatButton>(R.id.btnTransfer).setOnClickListener {
            if (tvBalance.text.toString().toDouble().compareTo(0.0) == 0) {
                toast(getString(R.string.insufficient_balance))
                return@setOnClickListener
            }
            val intent = Intent(this@MainActivity, TransferAssetsActivity::class.java)
            if (etMessage.text.toString().trim().isNotEmpty()) {
                intent.putExtra(DATA, etMessage.text.toString())
            }
            startActivity(intent)
        }
        val ivLogout = findViewById<AppCompatImageView>(R.id.ivLogout)
        ivLogout.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                showPopupOption(ivLogout)
            }
        }
        findViewById<AppCompatTextView>(R.id.tvLogout).setOnClickListener { logout() }
        findViewById<AppCompatImageView>(R.id.ivQRCode).setOnClickListener {
            showQRDialog(publicAddress)
        }

        btnSign.setOnClickListener {
            val msg = etMessage.text.toString()
            if (msg.isEmpty() || Web3AuthUtils.containsEmoji(etMessage.text.toString())) {
                toast(getString(R.string.invalid_message))
                return@setOnClickListener
            }

            if (blockChain.contains(getString(R.string.solana))) {
                solanaViewModel.signTransaction(NetworkUtils.getSolanaNetwork(blockChain), msg)
            } else {
                val signatureHash = ethereumViewModel.getSignature(
                    torusLoginResponse?.privateKey.toString(),
                    msg
                )
                showSignatureResult(signatureHash)
            }
        }
        swipeRefreshLayout.setOnRefreshListener {
            init()
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun setData() {
        tvNetwork = findViewById(R.id.tvNetwork)
        tvNetwork.text = blockChain.plus(" ")
        spCurrency = findViewById(R.id.spCurrency)
        setUpSpinner()

        tvViewTransactionStatus = findViewById(R.id.tvViewTransactionStatus)
        tvViewTransactionStatus.text = Web3AuthUtils.getAccountViewText(this, blockChain)
        tvViewTransactionStatus.setOnClickListener {
            var accountUrl = Web3AuthUtils.getViewTransactionUrl(this, blockChain)
                .plus(getString(R.string.user_account_address)).plus(publicAddress)
            if (blockChain == getString(R.string.sol_testnet)) {
                accountUrl = accountUrl.plus("?cluster=testnet")
            } else if (blockChain == getString(R.string.sol_devnet)) {
                accountUrl = accountUrl.plus("?cluster=devnet")
            }
            Web3AuthUtils.openCustomTabs(
                this@MainActivity,
                accountUrl
            )
        }
        tvNetwork.setOnClickListener {
            navigateToSettings()
        }

        if (!blockChain.contains(getString(R.string.solana))) {
            ethereumViewModel.getCurrencyPriceInUSD(
                Web3AuthUtils.getCurrency(blockChain),
                getString(R.string.usd)
            )
        }
    }

    private fun setUpSpinner() {
        var currencies: MutableList<String> = mutableListOf()
        currencies.add(Web3AuthUtils.getCurrency(blockChain))
        currencies.add(getString(R.string.usd))
        val currencyAdapter: ArrayAdapter<String> =
            ArrayAdapter(this, R.layout.item_dropdown, currencies)
        spCurrency.adapter = currencyAdapter
        spCurrency.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                if (tvBalance.text.toString().toDouble().compareTo(0.0) == 0) return
                if (currencies[position] == getString(R.string.usd)) {
                    getCurrencyInUSD()
                } else {
                    getCurrencyInSelectedBlockChain()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun showQRDialog(publicAddress: String) {
        val dialog = Dialog(this@MainActivity)
        dialog.setContentView(R.layout.dialog_qr_code)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        dialog.window?.attributes?.windowAnimations = R.style.animation
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val ivQR = dialog.findViewById<AppCompatImageView>(R.id.ivQRCode)
        val tvAddress = dialog.findViewById<AppCompatTextView>(R.id.tvAddress)
        val ivClose = dialog.findViewById<AppCompatImageView>(R.id.ivClose)

        tvAddress.text = publicAddress
        tvAddress.setOnClickListener { copyToClipboard(tvAddress.text.toString()) }

        val writer = MultiFormatWriter()
        val hintMap = mapOf(EncodeHintType.MARGIN to 0)
        val bitMatrix = writer.encode(publicAddress, BarcodeFormat.QR_CODE, 200, 200, hintMap)
        val barcodeEncoder = BarcodeEncoder()
        val bitmap = barcodeEncoder.createBitmap(bitMatrix)
        ivQR.setImageBitmap(bitmap)

        ivClose.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun getCurrencyInUSD() {
        if (blockChain.contains(getString(R.string.solana))) {
            tvBalance.text = String.format(
                "%.4f",
                Web3AuthUtils.getPriceinUSD(
                    tvBalance.text.toString().toDouble(),
                    priceInUSD.toDouble()
                )
            )
        } else {
            tvBalance.text = String.format(
                "%.4f",
                Web3AuthUtils.getPriceinUSD(
                    tvBalance.text.toString().toDouble(),
                    priceInUSD.toDouble()
                )
            )
        }
    }

    private fun showSignatureResult(signatureHash: String) {
        if (signatureHash == getString(R.string.error)) {
            showSignTransactionDialog(false)
        } else {
            showSignTransactionDialog(true, signatureHash)
        }
    }

    private fun showSignTransactionDialog(isSuccess: Boolean, ethHash: String? = null) {
        val dialog = Dialog(this@MainActivity)
        dialog.setContentView(R.layout.popup_sign_transaction)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.animation
        val ivState = dialog.findViewById<AppCompatImageView>(R.id.ivState)
        val transactionState = dialog.findViewById<AppCompatTextView>(R.id.tvTransactionState)
        val transactionHash = dialog.findViewById<AppCompatTextView>(R.id.tvTransactionHash)
        val tvCopy = dialog.findViewById<AppCompatTextView>(R.id.tvCopy)
        val ivClose = dialog.findViewById<AppCompatImageView>(R.id.ivClose)

        if (isSuccess) {
            transactionHash.text = ethHash
            transactionState.text = getString(R.string.sign_success)
            ivState.setImageDrawable(getDrawable(R.drawable.ic_iv_transaction_success))
            tvCopy.setOnClickListener { copyToClipboard(transactionHash.text.toString()) }
        } else {
            transactionState.text = getString(R.string.sign_failed)
            ivState.setImageDrawable(getDrawable(R.drawable.ic_transaction_failed))
            transactionHash.hide()
            tvCopy.hide()
        }

        ivClose.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showPriceInUSD() {
        if (tvBalance.text.isNotEmpty() && priceInUSD.isNotEmpty()) {
            if (blockChain.contains(getString(R.string.solana))) {
                tvPriceInUSD.text = "= ".plus(
                    String.format(
                        "%.4f",
                        Web3AuthUtils.getPriceinUSD(
                            tvBalance.text.toString().toDouble(),
                            priceInUSD.toDouble()
                        )
                    )
                ).plus(" ").plus(getString(R.string.usd))
            } else {
                tvPriceInUSD.text = "= ".plus(
                    String.format(
                        "%.4f",
                        Web3AuthUtils.getPriceinUSD(
                            tvBalance.text.toString().toDouble(),
                            priceInUSD.toDouble()
                        )
                    )
                ).plus(" ").plus(getString(R.string.usd))
            }
        }
    }

    private fun getCurrencyInSelectedBlockChain() {
        if (blockChain.contains(getString(R.string.solana))) {
            solanaViewModel.getBalance(publicAddress)
        } else {
            ethereumViewModel.retrieveBalance(publicAddress)
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", text)
        clipboardManager.setPrimaryClip(clipData)
        toast("Text Copied")
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showPopupOption(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.main, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.settings -> {
                    navigateToSettings()
                }
                R.id.logout -> {
                    logout()
                }
            }
            true
        }
        popupMenu.setForceShowIcon(true)
        popupMenu.show()
    }

    private fun navigateToSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    override fun onRestart() {
        super.onRestart()
        init()
    }

    private fun logout() {
        this.applicationContext.web3AuthWalletPreferences[ISLOGGEDIN] = false
        this.applicationContext.web3AuthWalletPreferences[ISONBOARDED] = false
        this.applicationContext.web3AuthWalletPreferences[LOGOUT] = false
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}