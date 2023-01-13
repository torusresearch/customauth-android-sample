package com.web3auth.custom_auth_wallet_app.api

import com.web3auth.custom_auth_wallet_app.api.models.EthGasAPIResponse
import com.web3auth.custom_auth_wallet_app.api.models.GasApiResponse
import com.web3auth.custom_auth_wallet_app.api.models.PriceResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.math.BigInteger

interface Web3AuthApi {
    @GET("/currency")
    suspend fun getCurrencyPrice(
        @Query("fsym") fsym: String,
        @Query("tsyms") tsyms: String
    ): Response<PriceResponse>

    @GET("api/ethgasAPI.json")
    suspend fun getMaxTransactionConfig(): Response<EthGasAPIResponse>

    @GET("/networks/{chain_id}/suggestedGasFees")
    suspend fun getGasConfig(@Path("chain_id") chainId: BigInteger): Response<GasApiResponse>
}