package com.web3auth.custom_auth_wallet_app.api.models

data class GasApiResponse(
    val low: Params? = null,
    val medium: Params? = null,
    val high: Params? = null
)

data class Params(
    val minWaitTimeEstimate: Double? = null,
    val maxWaitTimeEstimate: Double? = null,
    val suggestedMaxPriorityFeePerGas: String? = null,
    val suggestedMaxFeePerGas: String? = null
)