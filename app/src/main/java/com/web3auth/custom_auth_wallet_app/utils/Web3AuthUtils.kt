package com.web3auth.custom_auth_wallet_app.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.LocaleList
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import com.web3auth.custom_auth_wallet_app.R
import org.torusresearch.customauth.types.LoginType
import java.math.BigDecimal
import java.util.*
import kotlin.math.pow

object Web3AuthUtils {

    fun getBlockChainName(blockChain: String): String {
        return when (blockChain) {
            "ETH Mainnet" -> "EthAddress"
            "ETH Goerli" -> "EthAddress"
            "Solana Testnet" -> "SolAddress"
            "Solana Mainnet" -> "SolAddress"
            "Solana Devnet" -> "SolAddress"
            "Polygon Mainnet" -> "MATICAddress"
            "Binance Mainnet" -> "BNBAddress"
            else -> "EthAddress"
        }
    }

    fun getCurrency(blockChain: String): String {
        return when (blockChain) {
            "ETH Mainnet" -> "ETH"
            "Solana Testnet" -> "SOL"
            "Solana Mainnet" -> "SOL"
            "Solana Devnet" -> "SOL"
            "Polygon Mainnet" -> "MATIC"
            "Binance Mainnet" -> "BNB"
            "ETH Goerli" -> "ETH"
            else -> "ETH"
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    return true
                }
            }
        }
        return false
    }

    fun getAccountViewText(context: Context, blockChain: String): String {
        return when (blockChain) {
            "ETH Mainnet" -> context.getString(R.string.eth_view_account)
            "ETH Goerli" -> context.getString(R.string.eth_view_account)
            "Binance Mainnet" -> context.getString(R.string.bnb_view_transaction_status)
            "Solana Mainnet" -> context.getString(R.string.sol_view_account)
            "Solana Testnet" -> context.getString(R.string.sol_view_account)
            "Solana Devnet" -> context.getString(R.string.sol_view_account)
            "Polygon Mainnet" -> context.getString(R.string.poly_view_account)
            else -> context.getString(R.string.eth_view_account)
        }
    }

    fun getTransactionStatusText(context: Context, blockChain: String): String {
        return when (blockChain) {
            "ETH Mainnet" -> context.getString(R.string.eth_view_transaction_status)
            "ETH Goerli" -> context.getString(R.string.eth_view_transaction_status)
            "Binance Mainnet" -> context.getString(R.string.bnb_view_transaction_status)
            "Solana Mainnet" -> context.getString(R.string.sol_view_transaction_status)
            "Solana Testnet" -> context.getString(R.string.sol_view_transaction_status)
            "Solana Devnet" -> context.getString(R.string.sol_view_transaction_status)
            "Polygon Mainnet" -> context.getString(R.string.poly_view_transaction_status)
            else -> context.getString(R.string.eth_view_transaction_status)
        }
    }

    fun getViewTransactionUrl(context: Context, blockChain: String): String {
        return when (blockChain) {
            "ETH Mainnet" -> context.getString(R.string.eth_transaction_status_url)
            "Polygon Mainnet" -> context.getString(R.string.polygon_transaction_status_url)
            "Binance Mainnet" -> "https://bscscan.com/"
            "ETH Goerli" -> context.getString(R.string.eth_transaction_status_url)
            "Solana Mainnet" -> context.getString(R.string.sol_transaction_status_url)
            "Solana Testnet" -> context.getString(R.string.sol_transaction_status_url_testnet)
            "Solana Devnet" -> context.getString(R.string.sol_transaction_status_url_devnet)
            else -> context.getString(R.string.eth_transaction_status_url)
        }
    }

    private fun getEtherInWei() = 10.0.pow(18)

    private fun getEtherInGwei() = 10.0.pow(10)

    fun toWeiEther(ethBalance: Double): Double {
        val decimalWei = ethBalance
        return decimalWei / getEtherInWei()
    }

    private fun toGwieEther(balance: BigDecimal): Double {
        val decimalWei = balance.toDouble()
        return decimalWei / getEtherInGwei()
    }

    fun isValidEthAddress(address: String): Boolean {
        val ethAddressRegex = Regex(pattern = "^0x[a-fA-F0-9]{40}$")
        return ethAddressRegex.matches(input = address)
    }

    fun convertMinsToSec(mins: Double): Double = mins * 60L

    fun getMaxTransactionFee(amount: Double): Double {
        return toGwieEther(BigDecimal.valueOf(amount).multiply(BigDecimal.valueOf(21000)))
    }

    fun getAmountInLamports(amount: String) =
        amount.toBigDecimal().multiply(BigDecimal.valueOf(1000000000)).toLong()

    fun containsEmoji(message: String): Boolean {
        val emojiRegex = Regex(
            pattern = "(?:[\uD83C\uDF00-\uD83D\uDDFF]|[\uD83E\uDD00-\uD83E\uDDFF]|" +
                    "[\uD83D\uDE00-\uD83D\uDE4F]|[\uD83D\uDE80-\uD83D\uDEFF]|" +
                    "[\u2600-\u26FF]\uFE0F?|[\u2700-\u27BF]\uFE0F?|\u24C2\uFE0F?|" +
                    "[\uD83C\uDDE6-\uD83C\uDDFF]{1,2}|" +
                    "[\uD83C\uDD70\uD83C\uDD71\uD83C\uDD7E\uD83C\uDD7F\uD83C\uDD8E\uD83C\uDD91-\uD83C\uDD9A]\uFE0F?|" +
                    "[\u0023\u002A\u0030-\u0039]\uFE0F?\u20E3|[\u2194-\u2199\u21A9-\u21AA]\uFE0F?|[\u2B05-\u2B07\u2B1B\u2B1C\u2B50\u2B55]\uFE0F?|" +
                    "[\u2934\u2935]\uFE0F?|[\u3030\u303D]\uFE0F?|[\u3297\u3299]\uFE0F?|" +
                    "[\uD83C\uDE01\uD83C\uDE02\uD83C\uDE1A\uD83C\uDE2F\uD83C\uDE32-\uD83C\uDE3A\uD83C\uDE50\uD83C\uDE51]\uFE0F?|" +
                    "[\u203C\u2049]\uFE0F?|[\u25AA\u25AB\u25B6\u25C0\u25FB-\u25FE]\uFE0F?|" +
                    "[\u00A9\u00AE]\uFE0F?|[\u2122\u2139]\uFE0F?|\uD83C\uDC04\uFE0F?|\uD83C\uDCCF\uFE0F?|" +
                    "[\u231A\u231B\u2328\u23CF\u23E9-\u23F3\u23F8-\u23FA]\uFE0F?)+"
        )
        return emojiRegex.containsMatchIn(input = message)
    }

    fun getPriceinUSD(ethAmount: Double, usdPrice: Double): Double = ethAmount * (usdPrice)

    fun getPriceInEth(amount: Double, usdPrice: Double) = amount / usdPrice

    fun getPriceInSol(amount: Double, usdPrice: Double) = amount / usdPrice

    fun openCustomTabs(context: Context, url: String) {
        val defaultBrowser = context.getDefaultBrowser()
        val customTabsBrowsers = context.getCustomTabsBrowsers()
        val otherParams = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(context.getColor(R.color.background_color))
            .setNavigationBarColor(context.getColor(R.color.background_color))
            .build()

        if (customTabsBrowsers.contains(defaultBrowser)) {
            val customTabs = CustomTabsIntent.Builder()
                .setDefaultColorSchemeParams(otherParams)
                .build()
            customTabs.intent.setPackage(defaultBrowser)
            customTabs.launchUrl(context, Uri.parse(url))
        } else if (customTabsBrowsers.isNotEmpty()) {
            val customTabs = CustomTabsIntent.Builder()
                .setDefaultColorSchemeParams(otherParams)
                .build()
            customTabs.intent.setPackage(customTabsBrowsers[0])
            customTabs.launchUrl(context, Uri.parse(url))
        }
    }

    fun getSocialLoginIcon(context: Context, loginType: String): Int {
        return when (loginType) {
            context.getString(R.string.google) -> R.drawable.iv_google
            context.getString(R.string.facebook) -> R.drawable.ic_facebook
            context.getString(R.string.twitter) -> R.drawable.ic_twitter
            context.getString(R.string.discord) -> R.drawable.ic_discord
            context.getString(R.string.line) -> R.drawable.ic_line
            context.getString(R.string.apple) -> R.drawable.ic_apple
            context.getString(R.string.linkedin) -> R.drawable.ic_linkedin
            context.getString(R.string.wechat) -> R.drawable.ic_wechat
            context.getString(R.string.github) -> R.drawable.ic_github
            context.getString(R.string.reddit) -> R.drawable.ic_reddit
            context.getString(R.string.twitch) -> R.drawable.ic_twitch_inactive
            else -> R.drawable.iv_google
        }
    }

    private fun getMainnetClientId(loginType: LoginType): String {
        return when (loginType) {
            LoginType.GOOGLE -> "876733105116-i0hj3s53qiio5k95prpfmj0hp0gmgtor.apps.googleusercontent.com"
            LoginType.FACEBOOK -> "2554219104599979"
            LoginType.TWITTER -> "OPUyrj5G82ZDL1FU1J5Ve3OvQzAsQxy9"
            LoginType.DISCORD -> "630308572013527060"
            LoginType.LINE -> "a4jD59wm3e5SpXyfH06HIz63iZRjWxan"
            LoginType.APPLE -> "FURCtS8ni75fvwE0nftxSV39u7JaX7X6"
            LoginType.LINKEDIN -> "hgmrH20a7SE1Cpuha1Ke6RlHTdnNwp8a"
            LoginType.GITHUB -> "bbDQ4eCvCrjY2BGR6OES8qjbMgQDTVHz"
            LoginType.TWITCH -> "tfppratfiloo53g1x133ofa4rc29px"
            else -> "876733105116-i0hj3s53qiio5k95prpfmj0hp0gmgtor.apps.googleusercontent.com"
        }
    }

    private fun getTestnetClientId(loginType: LoginType): String {
        return when (loginType) {
            LoginType.GOOGLE -> "221898609709-obfn3p63741l5333093430j3qeiinaa8.apps.googleusercontent.com"
            LoginType.FACEBOOK -> "617201755556395"
            LoginType.TWITTER -> "f5and8beke76mzutmics0zu4gw10dj"
            LoginType.DISCORD -> "682533837464666198"
            LoginType.LINE -> "WN8bOmXKNRH1Gs8k475glfBP5gDZr9H1"
            LoginType.APPLE -> "m1Q0gvDfOyZsJCZ3cucSQEe9XMvl9d9L"
            LoginType.LINKEDIN -> "59YxSgx79Vl3Wi7tQUBqQTRTxWroTuoc"
            LoginType.GITHUB -> "PC2a4tfNRvXbT48t89J5am0oFM21Nxff"
            LoginType.TWITCH -> "f5and8beke76mzutmics0zu4gw10dj"
            else -> "221898609709-obfn3p63741l5333093430j3qeiinaa8.apps.googleusercontent.com"
        }
    }

    private fun getMainnetVerfier(loginType: LoginType): String {
        return when (loginType) {
            LoginType.GOOGLE -> "google"
            LoginType.FACEBOOK -> "facebook"
            LoginType.TWITTER -> "twitter"
            LoginType.DISCORD -> "discord"
            LoginType.LINE -> "torus-auth0-line"
            LoginType.APPLE -> "torus-auth0-apple"
            LoginType.LINKEDIN -> "torus-auth0-linkedin"
            LoginType.GITHUB -> "torus-auth0-github"
            LoginType.TWITCH -> "twitch"
            else -> "google"
        }
    }

    private fun getTestnetVerifier(loginType: LoginType): String {
        return when (loginType) {
            LoginType.GOOGLE -> "google-lrc"
            LoginType.FACEBOOK -> "facebook-lrc"
            LoginType.TWITTER -> "torus-auth0-twitter-lrc"
            LoginType.DISCORD -> "discord-lrc"
            LoginType.LINE -> "torus-auth0-line-lrc"
            LoginType.APPLE -> "torus-auth0-apple-lrc"
            LoginType.LINKEDIN -> "torus-auth0-linkedin-lrc"
            LoginType.GITHUB -> "torus-auth0-github-lrc"
            LoginType.TWITCH -> "twitch-lrc"
            else -> "google-lrc"
        }
    }

    fun getClientId(selectedNetwork: String, loginType: LoginType): String {
        return if (selectedNetwork == "Testnet")
            getTestnetClientId(loginType)
        else
            getMainnetClientId(loginType)
    }

    fun getVerifier(selectedNetwork: String, loginType: LoginType): String {
        return if (selectedNetwork == "Testnet")
            getTestnetVerifier(loginType)
        else
            getMainnetVerfier(loginType)
    }

    fun getDomain(network: String): String {
        return when (network) {
            "Mainnet" -> "https://torus.au.auth0.com"
            "Testnet" -> "https://torus-test.auth0.com"
            else -> "https://torus.au.auth0.com"
        }
    }

    fun getRedirectUri(network: String): String {
        return when (network) {
            "Mainnet" -> "https://scripts.toruswallet.io/redirect-prod.html" //https://scripts.toruswallet.io/redirect-prod.html
            "Testnet" -> "https://scripts.toruswallet.io/redirect.html"
            else -> "https://scripts.toruswallet.io/redirect-prod.html"
        }
    }

    fun getBrowserRedirectUri(network: String): String {
        return when (network) {
            "Mainnet" -> "torus://org.torusresearch.customauth/redirect"
            "Testnet" -> "torus://org.torusresearch.customauth/redirect"
            else -> "torus://org.torusresearch.customauth/redirect"
        }
    }

    fun getSystemLocale(): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        LocaleList.getDefault().get(0).language
    } else {
        Locale.getDefault().language
    }
}