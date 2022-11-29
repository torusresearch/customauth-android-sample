package com.web3auth.custom_auth_wallet_app.utils

import org.p2p.solanaj.rpc.Cluster
import org.torusresearch.fetchnodedetails.types.TorusNetwork

object NetworkUtils {

    fun getTorusNetwork(network: String): TorusNetwork {
        return when (network) {
            "Mainnet" -> TorusNetwork.MAINNET
            "Testnet" -> TorusNetwork.TESTNET
            "Cyan" -> TorusNetwork.CYAN
            else -> TorusNetwork.MAINNET
        }
    }


    fun getSolanaNetwork(network: String): Cluster {
        return when (network) {
            "Solana Mainnet" -> Cluster.MAINNET
            "Solana Testnet" -> Cluster.TESTNET
            "Solana Devnet" -> Cluster.DEVNET
            else -> Cluster.MAINNET
        }
    }

    fun getRpcUrl(blockChain: String): String {
        return when (blockChain) {
            "ETH Mainnet" -> "https://mainnet.infura.io/v3/7f287687b3d049e2bea7b64869ee30a3"
            "Solana Testnet" -> "SOL"
            "Solana Mainnet" -> "SOL"
            "Solana Devnet" -> "SOL"
            "Polygon Mainnet" -> "https://rpc-mumbai.maticvigil.com/"
            "Binance Mainnet" -> "https://rpc.ankr.com/bsc"
            "ETH Goerli" -> "https://goerli.infura.io/v3/7f287687b3d049e2bea7b64869ee30a3"
            else -> "https://mainnet.infura.io/v3/7f287687b3d049e2bea7b64869ee30a3"
        }
    }
}