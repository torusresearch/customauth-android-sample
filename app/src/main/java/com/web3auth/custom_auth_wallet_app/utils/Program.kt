package com.web3auth.custom_auth_wallet_app.utils

import org.p2p.solanaj.core.AccountMeta
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.TransactionInstruction

open class Program {
    /**
     * Returns a [TransactionInstruction] built from the specified values.
     * @param programId Solana program we are calling
     * @param keys AccountMeta keys
     * @param data byte array sent to Solana
     * @return [TransactionInstruction] object containing specified values
     */
    fun createTransactionInstruction(
        programId: PublicKey,
        keys: List<AccountMeta>,
        data: ByteArray
    ): TransactionInstruction {
        return TransactionInstruction(programId, keys, data)
    }
}