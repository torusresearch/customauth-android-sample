package com.web3auth.custom_auth_wallet_app.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.web3auth.custom_auth_wallet_app.api.ApiHelper
import com.web3auth.custom_auth_wallet_app.api.Web3AuthApi
import com.web3auth.custom_auth_wallet_app.api.models.EthGasAPIResponse
import com.web3auth.custom_auth_wallet_app.api.models.GasApiResponse
import com.web3auth.custom_auth_wallet_app.api.models.Params
import com.web3auth.custom_auth_wallet_app.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.web3j.crypto.*
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.response.EthGetBalance
import org.web3j.protocol.core.methods.response.EthGetTransactionCount
import org.web3j.protocol.core.methods.response.EthSendTransaction
import org.web3j.protocol.core.methods.response.Web3ClientVersion
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import kotlin.Pair

class EthereumViewModel : ViewModel() {

    private lateinit var web3: Web3j
    private lateinit var web3Balance: EthGetBalance
    var isWeb3Configured = MutableLiveData(false)
    var priceInUSD = MutableLiveData("")
    var publicAddress = MutableLiveData("")
    var balance = MutableLiveData(0.0)
    var ethGasAPIResponse: MutableLiveData<EthGasAPIResponse> = MutableLiveData(null)
    var transactionHash = MutableLiveData(Pair(false, ""))
    var gasAPIResponse: MutableLiveData<GasApiResponse> = MutableLiveData(null)
    var error: MutableLiveData<Boolean> = MutableLiveData(false)

    init {
        getMaxTransactionConfig()
    }

    fun configureWeb3j(blockChain: String) {
        val url = NetworkUtils.getRpcUrl(blockChain)
        web3 = Web3j.build(HttpService(url))
        try {
            val clientVersion: Web3ClientVersion = web3.web3ClientVersion().sendAsync().get()
            isWeb3Configured.value = !clientVersion.hasError()
        } catch (e: Exception) {
            error.postValue(true)
            e.printStackTrace()
        }
    }

    fun getCurrencyPriceInUSD(fsym: String, tsyms: String) {
        GlobalScope.launch {
            val web3AuthApi = ApiHelper.getTorusInstance().create(Web3AuthApi::class.java)
            val result = web3AuthApi.getCurrencyPrice(fsym, tsyms)
            if (result.isSuccessful && result.body() != null) {
                priceInUSD.postValue(result.body()?.USD)
            }
        }
    }

    fun getPublicAddress(publicKey: String) {
        GlobalScope.launch {
            publicAddress.postValue(publicKey)
        }
    }

    fun retrieveBalance(publicAddress: String) {
        GlobalScope.launch {
            try {
                web3Balance = web3.ethGetBalance(publicAddress, DefaultBlockParameterName.LATEST)
                    .sendAsync()
                    .get()
                balance.postValue(web3Balance.balance.toDouble())
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun getSignature(privateKey: String, message: String): String {
        val credentials: Credentials = Credentials.create(privateKey)
        val hashedData = Hash.sha3(message.toByteArray(StandardCharsets.UTF_8))
        val signature = Sign.signMessage(hashedData, credentials.ecKeyPair)
        val r = Numeric.toHexString(signature.r)
        val s = Numeric.toHexString(signature.s).substring(2)
        val v = Numeric.toHexString(signature.v).substring(2)
        return StringBuilder(r).append(s).append(v).toString()
    }

    private fun getMaxTransactionConfig() {
        GlobalScope.launch {
            val web3AuthApi = ApiHelper.getEthInstance().create(Web3AuthApi::class.java)
            val result = web3AuthApi.getMaxTransactionConfig()
            if (result.isSuccessful && result.body() != null) {
                ethGasAPIResponse.postValue(result.body() as EthGasAPIResponse)
            }
        }
    }

    fun getGasConfig() {
        GlobalScope.launch {
            val web3AuthApi = ApiHelper.getMockGasInstance().create(Web3AuthApi::class.java)
            val result = web3AuthApi.getGasConfig()
            if (result.isSuccessful && result.body() != null) {
                gasAPIResponse.postValue(result.body() as GasApiResponse)
            }
        }
    }

    fun sendTransaction(
        privateKey: String,
        recipientAddress: String,
        amountToBeSent: Double,
        data: String?,
        params: Params
    ) {
        GlobalScope.launch {
            try {
                val credentials: Credentials = Credentials.create(privateKey)
                val ethGetTransactionCount: EthGetTransactionCount = web3.ethGetTransactionCount(
                    credentials.address,
                    DefaultBlockParameterName.LATEST
                ).send()
                val nonce: BigInteger = ethGetTransactionCount.transactionCount
                val value: BigInteger =
                    Convert.toWei(amountToBeSent.toString(), Convert.Unit.ETHER).toBigInteger()
                val gasLimit: BigInteger = BigInteger.valueOf(21000)
                web3.ethGasPrice().send().gasPrice
                val chainId = withContext(Dispatchers.IO) {
                    web3.ethChainId().sendAsync().get()
                }

                val rawTransaction: RawTransaction = RawTransaction.createTransaction(
                    chainId.chainId.toLong(),
                    nonce,
                    gasLimit,
                    recipientAddress,
                    value,
                    data ?: "",
                    BigInteger.valueOf(params.suggestedMaxPriorityFeePerGas?.toLong() ?: 4),
                    BigInteger.valueOf(params.suggestedMaxFeePerGas?.toLong() ?: 67)
                )

                // Sign the transaction
                val signedMessage: ByteArray =
                    TransactionEncoder.signMessage(rawTransaction, credentials)
                val hexValue: String = Numeric.toHexString(signedMessage)
                val ethSendTransaction: EthSendTransaction =
                    web3.ethSendRawTransaction(hexValue).send()
                if (ethSendTransaction.error != null) {
                    transactionHash.postValue(Pair(false, ethSendTransaction.error.message))
                } else {
                    transactionHash.postValue(Pair(true, ethSendTransaction.transactionHash))
                }
            } catch (ex: Exception) {
                error.postValue(true)
                ex.printStackTrace()
            }
        }
    }
}