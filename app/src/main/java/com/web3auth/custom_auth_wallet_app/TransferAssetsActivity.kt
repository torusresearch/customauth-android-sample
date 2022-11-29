package com.web3auth.custom_auth_wallet_app

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.web3auth.custom_auth_wallet_app.api.models.EthGasAPIResponse
import com.web3auth.custom_auth_wallet_app.api.models.GasApiResponse
import com.web3auth.custom_auth_wallet_app.api.models.Params
import com.web3auth.custom_auth_wallet_app.utils.*
import com.web3auth.custom_auth_wallet_app.viewmodel.EthereumViewModel
import com.web3auth.custom_auth_wallet_app.viewmodel.SolanaViewModel
import org.p2p.solanaj.core.PublicKey
import org.torusresearch.customauth.types.TorusLoginResponse

class TransferAssetsActivity : AppCompatActivity() {

    private lateinit var etRecipientAddress: AppCompatEditText
    private lateinit var etAmountToSend: AppCompatEditText
    private lateinit var etMaxTransFee: AppCompatEditText
    private lateinit var publicAddress: String
    private lateinit var ethGasAPIResponse: EthGasAPIResponse
    private lateinit var ethereumViewModel: EthereumViewModel
    private lateinit var blockChain: String
    private lateinit var network: String
    private lateinit var solanaViewModel: SolanaViewModel
    private lateinit var gasApiResponse: GasApiResponse
    private lateinit var tvEth: AppCompatTextView
    private lateinit var tvUSD: AppCompatTextView
    private lateinit var tvTotalAmount: AppCompatTextView
    private lateinit var tvCostInETH: AppCompatTextView
    private lateinit var flTransaction: FrameLayout
    private lateinit var tvEdit: AppCompatTextView
    private lateinit var priceInUSD: String
    private lateinit var transDialog: Dialog
    private lateinit var selectedGasParams: Params
    private var totalCostinETH: Double = 0.0
    private var totalAmountInSol: Double = 0.0
    private var gasFee: Double = 0.0
    private var processTime: Double = 0.0
    private var isUSDSelected: Boolean = false
    private var torusLoginResponse: TorusLoginResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer_asset)
        supportActionBar?.hide()
        if (!Web3AuthUtils.isNetworkAvailable(this@TransferAssetsActivity)) {
            longToast(getString(R.string.connect_to_internet))
            return
        }
        blockChain = this.applicationContext.web3AuthWalletPreferences.getString(
            BLOCKCHAIN,
            "ETH Mainnet"
        ).toString()
        network =
            this.applicationContext.web3AuthWalletPreferences.getString(NETWORK, "Mainnet")
                .toString()
        publicAddress =
            this.applicationContext.web3AuthWalletPreferences.getString(PUBLICKEY, "")
                .toString()
        priceInUSD =
            this.applicationContext.web3AuthWalletPreferences.getString(PRICE_IN_USD, "")
                .toString()
        torusLoginResponse =
            this.applicationContext.web3AuthWalletPreferences.getObject(LOGIN_RESPONSE)
        if (blockChain.contains(getString(R.string.solana))) {
            solanaViewModel = ViewModelProvider(this)[SolanaViewModel::class.java]
            solanaViewModel.setNetwork(
                NetworkUtils.getSolanaNetwork(blockChain),
                torusLoginResponse?.privateKey.toString()
            )
        } else {
            ethereumViewModel = ViewModelProvider(this)[EthereumViewModel::class.java]
            ethereumViewModel.configureWeb3j(blockChain)
            ethereumViewModel.getGasConfig()
        }
        setUpListeners()
    }

    private fun setUpListeners() {
        etRecipientAddress = findViewById(R.id.etRecipientAddress)
        etAmountToSend = findViewById(R.id.etAmountToSend)
        etMaxTransFee = findViewById(R.id.etMaxTransFee)
        tvEth = findViewById(R.id.tvEth)
        tvUSD = findViewById(R.id.tvUSD)
        tvTotalAmount = findViewById(R.id.tvTotalAmount)
        tvCostInETH = findViewById(R.id.tvCostInETH)
        tvEdit = findViewById(R.id.tvEditTransFee)
        flTransaction = findViewById(R.id.flTransaction)
        val etBlockChain = findViewById<AppCompatEditText>(R.id.etBlockChain)
        val etBlockChainAdd = findViewById<AppCompatEditText>(R.id.etBlockChainAdd)

        etBlockChainAdd.setText(blockChain.let { Web3AuthUtils.getBlockChainName(it) })
        etBlockChain.setText(blockChain)
        tvEth.text = Web3AuthUtils.getCurrency(blockChain)

        if (blockChain.contains(getString(R.string.solana))) {
            findViewById<AppCompatTextView>(R.id.tvTransactionFee).hide()
            tvEdit.hide()
            flTransaction.hide()
        }

        findViewById<AppCompatImageView>(R.id.ivBack).setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        findViewById<AppCompatImageView>(R.id.ivScan).setOnClickListener { scanQRCode() }

        resetTotalCost()

        tvEth.setOnClickListener {
            it.background = getDrawable(R.drawable.bg_layout_tv_blue)
            tvUSD.background = getDrawable(R.drawable.bg_layout_tv_grey)
            isUSDSelected = false
            etAmountToSend.text?.clear()
            resetTotalCost()
        }

        tvUSD.setOnClickListener {
            it.background = getDrawable(R.drawable.bg_layout_tv_blue)
            tvEth.background = getDrawable(R.drawable.bg_layout_tv_grey)
            isUSDSelected = true
            etAmountToSend.text?.clear()
            resetTotalCost()
        }


        etAmountToSend.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.isNotEmpty() == true) {
                    if (blockChain.contains(getString(R.string.solana))) {
                        totalAmountInSol = if (isUSDSelected) {
                            Web3AuthUtils.getPriceInSol(
                                s.toString().toDouble(),
                                priceInUSD.toDouble()
                            )
                        } else {
                            s.toString().toDouble()
                        }
                        setTotalAmountForSol()
                    } else {
                        totalCostinETH = if (isUSDSelected) {
                            Web3AuthUtils.getPriceInEth(
                                s.toString().toDouble(),
                                priceInUSD.toDouble()
                            )
                                .plus(Web3AuthUtils.getMaxTransactionFee(ethGasAPIResponse.fastest))
                        } else {
                            s.toString().toDouble()
                                .plus(Web3AuthUtils.getMaxTransactionFee(ethGasAPIResponse.fastest))
                        }
                        setTotalAmount()
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        findViewById<AppCompatButton>(R.id.btnTransfer).setOnClickListener {
            if (blockChain.contains(getString(R.string.solana))) {
                if (isValidSolanaDetails()) {
                    showConfirmTransactionDialog(
                        publicAddress,
                        etRecipientAddress.text.toString(),
                        etAmountToSend.text.toString(),
                        gasFee = 0.0000005, processTime = 0.30,
                        tvTotalAmount.text.toString(),
                        tvCostInETH.text.toString()
                    )
                }
            } else {
                if (isValidDetails()) {
                    showConfirmTransactionDialog(
                        publicAddress,
                        etRecipientAddress.text.toString(),
                        etAmountToSend.text.toString(),
                        gasFee, processTime,
                        tvTotalAmount.text.toString(),
                        tvCostInETH.text.toString()
                    )
                }
            }
        }

        tvEdit.setOnClickListener {
            if (ethGasAPIResponse != null) {
                showMaxTransactionSelectDialog()
            }
        }

        if (blockChain.contains(getString(R.string.solana))) {
            solanaViewModel.sendTransactionResult.observe(this) {
                observeTransactionResult(it)
            }
        } else {
            ethereumViewModel.ethGasAPIResponse.observe(this) {
                if (it != null) {
                    ethGasAPIResponse = it
                    gasFee = it.fastest
                    processTime = it.fastestWait
                    tvEdit.show()
                } else {
                    tvEdit.hide()
                }
            }

            ethereumViewModel.transactionHash.observe(this) {
                observeTransactionResult(it)
            }

            ethereumViewModel.gasAPIResponse.observe(this) {
                if (it != null) {
                    gasApiResponse = it
                    it.high?.let { it1 -> setGasParams(it1) }
                }
            }
        }
    }

    private fun scanQRCode() {
        val options = ScanOptions()
        options.setOrientationLocked(true)
        barcodeLauncher.launch(options)
    }

    private fun observeTransactionResult(result: Pair<Boolean, String>) {
        if (result.second.isEmpty()) return
        if (::transDialog.isInitialized) transDialog.dismiss()
        if (result.first) {
            showTransactionDialog(TransactionStatus.SUCCESSFUL, result.second)
        } else {
            showTransactionDialog(TransactionStatus.FAILED, result.second)
        }
    }

    private val barcodeLauncher = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents == null) {
            val originalIntent = result.originalIntent
            if (originalIntent == null) {
                Toast.makeText(
                    this@TransferAssetsActivity,
                    getString(R.string.cancelled),
                    Toast.LENGTH_LONG
                ).show()
            } else if (originalIntent.hasExtra(Intents.Scan.MISSING_CAMERA_PERMISSION)) {
                Toast.makeText(
                    this@TransferAssetsActivity,
                    getString(R.string.denied_camera_permission),
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            etRecipientAddress.setText(result.contents)
        }
    }

    private fun resetTotalCost() {
        tvTotalAmount.text = "0".plus(" " + Web3AuthUtils.getCurrency(blockChain))
        tvCostInETH.text = "= ".plus("0").plus(" ").plus(getString(R.string.usd))
    }

    private fun isValidDetails(): Boolean {
        return if (etRecipientAddress.text?.isNullOrEmpty() == true) {
            toast(getString(R.string.enter_address))
            false
        } else if (!Web3AuthUtils.isValidEthAddress(etRecipientAddress.text.toString().trim())) {
            toast(getString(R.string.invalid_address))
            false
        } else if (etAmountToSend.text?.isNullOrEmpty() == true) {
            toast(getString(R.string.enter_amt_to_transfer))
            false
        } else {
            true
        }
    }

    private fun isValidSolanaDetails(): Boolean {
        return if (etRecipientAddress.text?.isNullOrEmpty() == true) {
            toast(getString(R.string.enter_address))
            false
        } else if (etRecipientAddress.text.toString().trim().length < PublicKey.PUBLIC_KEY_LENGTH) {
            toast(getString(R.string.invalid_address))
            false
        } else if (etAmountToSend.text?.isNullOrEmpty() == true) {
            toast(getString(R.string.enter_amt_to_transfer))
            false
        } else {
            true
        }
    }

    private fun showMaxTransactionSelectDialog() {
        val dialog = Dialog(this@TransferAssetsActivity)
        dialog.setContentView(R.layout.dialog_max_transaction_fee)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.animation
        val clFast = dialog.findViewById<ConstraintLayout>(R.id.clFast)
        val clAvg = dialog.findViewById<ConstraintLayout>(R.id.clAvg)
        val clSlow = dialog.findViewById<ConstraintLayout>(R.id.clSlow)
        val rbFast = dialog.findViewById<RadioButton>(R.id.rbFast)
        val rbAvg = dialog.findViewById<RadioButton>(R.id.rbAvg)
        val rbSlow = dialog.findViewById<RadioButton>(R.id.rbSlow)
        val tvFastProcessTime = dialog.findViewById<AppCompatTextView>(R.id.tvFastProcessTime)
        val tvAvgProcessTime = dialog.findViewById<AppCompatTextView>(R.id.tvAvgProcessTime)
        val tvSlowProcessTime = dialog.findViewById<AppCompatTextView>(R.id.tvSlowProcessTime)
        val tvFastEth = dialog.findViewById<AppCompatTextView>(R.id.tvFastEth)
        val tvSlowEth = dialog.findViewById<AppCompatTextView>(R.id.tvSlowEth)
        val tvAvgEth = dialog.findViewById<AppCompatTextView>(R.id.tvAvgEth)
        val tvCancel = dialog.findViewById<AppCompatTextView>(R.id.tvCancel)
        val btnSave = dialog.findViewById<AppCompatButton>(R.id.btnSave)

        try {
            tvFastProcessTime.text = getString(R.string.process_in).plus(" ")
                .plus(Web3AuthUtils.convertMinsToSec(ethGasAPIResponse.fastestWait)).plus(" ")
                .plus(getString(R.string.seconds))
            tvAvgProcessTime.text = getString(R.string.process_in).plus(" ")
                .plus(Web3AuthUtils.convertMinsToSec(ethGasAPIResponse.fastWait)).plus(" ")
                .plus(getString(R.string.seconds))
            tvSlowProcessTime.text = getString(R.string.process_in).plus(" ")
                .plus(Web3AuthUtils.convertMinsToSec(ethGasAPIResponse.avgWait)).plus(" ")
                .plus(getString(R.string.seconds))
            tvFastEth.text = getString(R.string.upto).plus(" ")
                .plus(Web3AuthUtils.getMaxTransactionFee(ethGasAPIResponse.fastest))
                .plus(" ").plus(Web3AuthUtils.getCurrency(blockChain))
            tvAvgEth.text = getString(R.string.upto).plus(" ")
                .plus(Web3AuthUtils.getMaxTransactionFee(ethGasAPIResponse.fast))
                .plus(" ").plus(Web3AuthUtils.getCurrency(blockChain))
            tvSlowEth.text = getString(R.string.upto).plus(" ")
                .plus(Web3AuthUtils.getMaxTransactionFee(ethGasAPIResponse.average))
                .plus(" ").plus(Web3AuthUtils.getCurrency(blockChain))
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        clFast.setOnClickListener {
            rbAvg.isChecked = false
            rbSlow.isChecked = false
            gasFee = ethGasAPIResponse.fastest
            processTime = ethGasAPIResponse.fastestWait
            gasApiResponse.high?.let { it1 -> setGasParams(it1) }
            setMaxTransFee(ethGasAPIResponse.fastest)
            dialog.dismiss()
        }

        clAvg.setOnClickListener {
            rbFast.isChecked = false
            rbSlow.isChecked = false
            gasFee = ethGasAPIResponse.fast
            processTime = ethGasAPIResponse.fastWait
            gasApiResponse.medium?.let { it1 -> setGasParams(it1) }
            setMaxTransFee(ethGasAPIResponse.fast)
            dialog.dismiss()
        }

        clSlow.setOnClickListener {
            rbAvg.isChecked = false
            rbFast.isChecked = false
            gasFee = ethGasAPIResponse.average
            processTime = ethGasAPIResponse.avgWait
            gasApiResponse.low?.let { it1 -> setGasParams(it1) }
            setMaxTransFee(ethGasAPIResponse.average)
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            dialog.dismiss()
        }
        tvCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun setGasParams(params: Params) {
        selectedGasParams = params
    }

    private fun showConfirmTransactionDialog(
        senderAdd: String, receiptAdd: String, amountToBeSent: String,
        gasFee: Double, processTime: Double, totalAmount: String, totalAmountInUSD: String
    ) {
        transDialog = Dialog(this@TransferAssetsActivity)
        transDialog.setContentView(R.layout.dialog_confirm_transaction)
        transDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        transDialog.setCancelable(false)
        transDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        transDialog.window?.attributes?.windowAnimations = R.style.animation
        val clTransaction = transDialog.findViewById<ConstraintLayout>(R.id.clTransaction)
        val clProgressBar = transDialog.findViewById<ConstraintLayout>(R.id.clProgressBar)
        val tvNetwork = transDialog.findViewById<AppCompatTextView>(R.id.tvNetwork)
        val tvSenderAdd = transDialog.findViewById<AppCompatTextView>(R.id.tvSenderAdd)
        val tvReceiptAdd = transDialog.findViewById<AppCompatTextView>(R.id.tvReceiptAdd)
        val tvAmountInETH = transDialog.findViewById<AppCompatTextView>(R.id.tvAmountinETH)
        val tvAmountInUsd = transDialog.findViewById<AppCompatTextView>(R.id.tvAmountInUsd)
        val tvTransactionValue =
            transDialog.findViewById<AppCompatTextView>(R.id.tvTransactionValue)
        val tvTotalAmountInETH =
            transDialog.findViewById<AppCompatTextView>(R.id.tvTotalAmountInETH)
        val tvTotalCostInUSD = transDialog.findViewById<AppCompatTextView>(R.id.tvTotalCostInUSD)
        val tvProcessTime = transDialog.findViewById<AppCompatTextView>(R.id.tvProcessTime)
        val tvCancel = transDialog.findViewById<AppCompatTextView>(R.id.tvCancel)
        val btnConfirm = transDialog.findViewById<AppCompatButton>(R.id.btnConfirm)

        tvSenderAdd.text = senderAdd
        tvReceiptAdd.text = receiptAdd
        tvAmountInETH.text = amountToBeSent.plus(" " + Web3AuthUtils.getCurrency(blockChain))
        tvAmountInUsd.text = "~".plus(
            String.format(
                "%.6f",
                Web3AuthUtils.getPriceinUSD(amountToBeSent.toDouble(), priceInUSD.toDouble())
            )
        )
            .plus(" ").plus(getString(R.string.usd))
        tvNetwork.text = blockChain

        if (blockChain.contains(getString(R.string.solana)))
            tvTransactionValue.text = "0.000005".plus(" " + Web3AuthUtils.getCurrency(blockChain))
        else
            tvTransactionValue.text = Web3AuthUtils.getMaxTransactionFee(gasFee).toString()
                .plus(" " + Web3AuthUtils.getCurrency(blockChain))

        tvProcessTime.text = "in".plus(" ").plus(" ")
            .plus(Web3AuthUtils.convertMinsToSec(processTime)).plus(" ")
            .plus(getString(R.string.seconds))

        tvTotalAmountInETH.text = totalAmount
        tvTotalCostInUSD.text = totalAmountInUSD.replace("=", "~")

        btnConfirm.setOnClickListener {
            clTransaction.hide()
            clProgressBar.show()
            if (blockChain.contains(getString(R.string.solana))) {
                solanaViewModel.signAndSendTransaction(
                    publicAddress, receiptAdd,
                    amount = Web3AuthUtils.getAmountInLamports(totalAmount.split(" ")[0]),
                    intent.getStringExtra(DATA)
                )
            } else {
                ethereumViewModel.sendTransaction(
                    torusLoginResponse?.privateKey.toString(),
                    receiptAdd,
                    totalAmount.split(" ")[0].toDouble(),
                    "",
                    selectedGasParams
                )
            }
        }
        tvCancel.setOnClickListener {
            transDialog.dismiss()
        }
        transDialog.show()
    }

    private fun setMaxTransFee(fee: Double) {
        etMaxTransFee.setText(
            getString(R.string.upto).plus(" ").plus(Web3AuthUtils.getMaxTransactionFee(fee))
        )
        val amount = etAmountToSend.text.toString()
        if (amount.isNotEmpty()) {
            totalCostinETH = if (isUSDSelected) {
                Web3AuthUtils.getPriceInEth(
                    amount.toDouble(),
                    priceInUSD.toDouble()
                ).plus(Web3AuthUtils.getMaxTransactionFee(fee))
            } else {
                amount.toString().toDouble()
                    .plus(Web3AuthUtils.getMaxTransactionFee(fee))
            }
            setTotalAmount()
        }
    }

    private fun setTotalAmount() {
        tvTotalAmount.text =
            String.format("%.6f", totalCostinETH).plus(" " + Web3AuthUtils.getCurrency(blockChain))
        tvCostInETH.text = "= ".plus(
            String.format(
                "%.6f",
                Web3AuthUtils.getPriceinUSD(totalCostinETH, priceInUSD.toDouble())
            )
        )
            .plus(" ").plus(getString(R.string.usd))
    }

    private fun setTotalAmountForSol() {
        tvTotalAmount.text =
            String.format("%.4f", totalAmountInSol)
                .plus(" " + Web3AuthUtils.getCurrency(blockChain))
        tvCostInETH.text = "= ".plus(
            String.format(
                "%.4f",
                Web3AuthUtils.getPriceinUSD(totalAmountInSol, priceInUSD.toDouble())
            )
        )
            .plus(" ").plus(getString(R.string.usd))
    }

    private fun showTransactionDialog(
        transactionStatus: TransactionStatus,
        transactionHash: String?
    ) {
        val dialog = Dialog(this@TransferAssetsActivity)
        dialog.setContentView(R.layout.popup_transaction)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.animation
        val ivState = dialog.findViewById<AppCompatImageView>(R.id.ivState)
        val transactionState = dialog.findViewById<AppCompatTextView>(R.id.tvTransactionState)
        val tvStatus = dialog.findViewById<AppCompatTextView>(R.id.tvStatus)
        val icCLose = dialog.findViewById<AppCompatImageView>(R.id.icCLose)

        when (transactionStatus) {
            TransactionStatus.PLACED -> {
                transactionState.text = getString(R.string.transaction_placed)
                ivState.setImageDrawable(getDrawable(R.drawable.ic_transaction_placed))
                tvStatus.hide()
            }
            TransactionStatus.SUCCESSFUL -> {
                transactionState.text = getString(R.string.transaction_success)
                ivState.setImageDrawable(getDrawable(R.drawable.ic_iv_transaction_success))
                tvStatus.text = Web3AuthUtils.getTransactionStatusText(this, blockChain)
                var transUrl = Web3AuthUtils.getViewTransactionUrl(this, blockChain)
                    .plus(getString(R.string.transaction)).plus(transactionHash)
                if (blockChain == getString(R.string.sol_testnet)) {
                    transUrl = transUrl.plus("?cluster=testnet")
                } else if (blockChain == getString(R.string.sol_devnet)) {
                    transUrl = transUrl.plus("?cluster=devnet")
                }
                tvStatus.setOnClickListener {
                    Web3AuthUtils.openCustomTabs(
                        this@TransferAssetsActivity,
                        transUrl
                    )
                }
            }
            TransactionStatus.FAILED -> {
                transactionState.text = getString(R.string.transaction_failed)
                ivState.setImageDrawable(getDrawable(R.drawable.ic_transaction_failed))
                tvStatus.text = getString(R.string.try_again)
                tvStatus.setOnClickListener {
                    dialog.dismiss()
                    onBackPressedDispatcher.onBackPressed()
                }
            }
            else -> {
                transactionState.text = getString(R.string.transaction_pending)
                ivState.setImageDrawable(getDrawable(R.drawable.ic_transaction_pending))
            }
        }

        icCLose.setOnClickListener {
            dialog.dismiss()
            onBackPressedDispatcher.onBackPressed()
        }
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModelStore.clear()
    }
}