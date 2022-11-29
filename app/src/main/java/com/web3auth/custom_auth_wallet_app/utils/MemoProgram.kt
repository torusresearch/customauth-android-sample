package com.web3auth.custom_auth_wallet_app.utils

import org.p2p.solanaj.core.AccountMeta
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.TransactionInstruction
import java.nio.charset.StandardCharsets
import java.util.*


object MemoProgram : Program() {
    val PROGRAM_ID: PublicKey = PublicKey("Memo1UhkJRfHyvLMcVucJwxXeuD728EqVDDwQDxFMNo")

    /**
     * Returns a [TransactionInstruction] object containing instructions to call the Memo program with the
     * specified memo.
     * @param account signer pubkey
     * @param memo utf-8 string to be written into Solana transaction
     * @return [TransactionInstruction] object with memo instruction
     */
    fun writeUtf8(account: PublicKey?, memo: String): TransactionInstruction {
        // Add signer to AccountMeta keys
        val keys: List<AccountMeta> = Collections.singletonList(
            account?.let {
                AccountMeta(
                    it,
                    false, false
                )
            }
        )

        // Convert memo string to UTF-8 byte array
        val memoBytes: ByteArray = memo.toByteArray(StandardCharsets.UTF_8)
        return createTransactionInstruction(
            PROGRAM_ID,
            keys,
            memoBytes
        )
    }
}