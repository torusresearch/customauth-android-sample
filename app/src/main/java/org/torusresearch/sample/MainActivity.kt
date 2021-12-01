package org.torusresearch.sample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import org.torusresearch.customauth.CustomAuth
import org.torusresearch.customauth.types.*

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener, View.OnClickListener {
    class Verifier(
        val typeOfLogin: LoginType,
        val name: String,
        val verifierId: String,
        val clientId: String,
        val domain: String? = null,
        val verifierIdField: String? = null,
        val isVerifierIdCaseSensitive: Boolean = true
    )

    companion object {
        const val RC_GOOGLE_SIGN_IN = 1
    }

    private var verifiers = arrayOf<Verifier>()
    private var selectedVerifier: Verifier? = null

    lateinit var customAuth: CustomAuth
    lateinit var googleSignIn: GoogleSignInClient

    lateinit var spinnerLoginType: Spinner
    lateinit var buttonLogin: Button
    lateinit var buttonAgregateLogin: Button
    lateinit var buttonGoogleLogin: SignInButton
    lateinit var textResult: TextView

    private fun setVerifiers() {
        verifiers = arrayOf(
            Verifier(
                LoginType.GOOGLE,
                getString(R.string.google),
                getString(R.string.torus_google_verifier_id),
                getString(R.string.torus_google_client_id)
            ),
            Verifier(
                LoginType.FACEBOOK,
                getString(R.string.facebook),
                getString(R.string.torus_facebook_verifier_id),
                getString(R.string.torus_facebook_client_id)
            ),
            Verifier(
                LoginType.TWITCH,
                getString(R.string.twitch),
                getString(R.string.torus_twitch_verifier_id),
                getString(R.string.torus_twitch_client_id)
            ),
            Verifier(
                LoginType.DISCORD,
                getString(R.string.discord),
                getString(R.string.torus_discord_verifier_id),
                getString(R.string.torus_discord_client_id)
            ),
            Verifier(
                LoginType.APPLE,
                getString(R.string.apple),
                getString(R.string.torus_apple_verifier_id),
                getString(R.string.torus_apple_client_id),
                getString(R.string.torus_proxy_verifier_domain)
            ),
            Verifier(
                LoginType.GITHUB,
                getString(R.string.github),
                getString(R.string.torus_github_verifier_id),
                getString(R.string.torus_github_client_id),
                getString(R.string.torus_proxy_verifier_domain)
            ),
            Verifier(
                LoginType.LINKEDIN,
                getString(R.string.linkedin),
                getString(R.string.torus_linkedin_verifier_id),
                getString(R.string.torus_linkedin_client_id),
                getString(R.string.torus_proxy_verifier_domain)
            ),
            Verifier(
                LoginType.TWITTER,
                getString(R.string.twitter),
                getString(R.string.torus_twitter_verifier_id),
                getString(R.string.torus_twitter_client_id),
                getString(R.string.torus_proxy_verifier_domain)
            ),
            Verifier(
                LoginType.LINE,
                getString(R.string.line),
                getString(R.string.torus_line_verifier_id),
                getString(R.string.torus_line_client_id),
                getString(R.string.torus_proxy_verifier_domain)
            ),
            Verifier(
                LoginType.EMAIL_PASSWORD,
                getString(R.string.email_password),
                getString(R.string.torus_email_password_verifier_id),
                getString(R.string.torus_email_password_client_id),
                getString(R.string.torus_proxy_verifier_domain)
            ),
            Verifier(
                LoginType.JWT,
                getString(R.string.passwordless),
                getString(R.string.torus_passwordless_verifier_id),
                getString(R.string.torus_passwordless_client_id),
                getString(R.string.torus_proxy_verifier_domain), "name", false
            ),
            Verifier(
                LoginType.JWT,
                getString(R.string.sms_passwordless),
                getString(R.string.torus_sms_passwordless_verifier_id),
                getString(R.string.torus_sms_passwordless_client_id),
                getString(R.string.torus_proxy_verifier_domain), "name", false
            ),
        )
    }

    private fun setIsLoggingIn(isLoggingIn: Boolean) {
        spinnerLoginType.isEnabled = !isLoggingIn
        buttonLogin.isEnabled = !isLoggingIn
        buttonAgregateLogin.isEnabled = !isLoggingIn
        buttonGoogleLogin.isEnabled = !isLoggingIn
        if (isLoggingIn) textResult.text = "Logging in..."
    }

    private fun getVerifierDetails(verifier: Verifier): SubVerifierDetails {
        return if (verifier.domain != null) SubVerifierDetails(
            verifier.typeOfLogin,
            verifier.verifierId,
            verifier.clientId,
            Auth0ClientOptions.Auth0ClientOptionsBuilder(verifier.domain)
                .also {
                    it.setVerifierIdField(verifier.verifierIdField)
                    it.setVerifierIdCaseSensitive(verifier.isVerifierIdCaseSensitive)
                }.build()
        ) else SubVerifierDetails(
            verifier.typeOfLogin,
            verifier.verifierId,
            verifier.clientId
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setVerifiers()

        customAuth = CustomAuth(
            CustomAuthArgs(
                getString(R.string.torus_browser_redirect_uri),
                TorusNetwork.TESTNET,
                getString(R.string.torus_app_redirect_uri)
            ), this
        )

        googleSignIn = GoogleSignIn.getClient(
            this, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestId()
                .requestEmail()
                .requestProfile()
                .requestIdToken(getString(R.string.torus_native_google_client_id))
                .build()
        )

        spinnerLoginType = findViewById(R.id.spinner_login_type)
        spinnerLoginType.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            verifiers.map { it.name }
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerLoginType.onItemSelectedListener = this

        buttonLogin = findViewById(R.id.button_login)
        buttonLogin.setOnClickListener(this)

        buttonAgregateLogin = findViewById(R.id.button_aggregate_login)
        buttonAgregateLogin.setOnClickListener(this)

        buttonGoogleLogin = findViewById(R.id.button_native_google_login)
        buttonGoogleLogin.setOnClickListener(this)

        textResult = findViewById(R.id.text_result)
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (parent == spinnerLoginType) {
            selectedVerifier = verifiers[position]
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        if (parent == spinnerLoginType) {
            selectedVerifier = null
        }
    }

    override fun onClick(view: View?) {
        if (view == buttonLogin) {
            val verifier = selectedVerifier ?: return
            setIsLoggingIn(true)
            customAuth.triggerLogin(getVerifierDetails(verifier))
                .whenComplete { res, err ->
                    runOnUiThread {
                        if (err != null) {
                            textResult.text = "Error: ${err.message}"
                        } else {
                            textResult.text =
                                "Address:\n${res.publicAddress}\n\nPrivate key:\n${res.privateKey}"
                        }
                        setIsLoggingIn(false)
                    }
                }
        } else if (view == buttonAgregateLogin) {
            setIsLoggingIn(true)
            customAuth.triggerAggregateLogin(
                AggregateLoginParams(
                    AggregateVerifierType.SINGLE_VERIFIER_ID,
                    getString(R.string.torus_aggregate_verifier_id),
                    arrayOf(
                        SubVerifierDetails(
                            LoginType.GOOGLE,
                            getString(R.string.torus_aggregate_google_verifier_id),
                            getString(R.string.torus_aggregate_google_client_id)
                        )
                    )
                )
            ).whenComplete { res, err ->
                runOnUiThread {
                    if (err != null) {
                        textResult.text = "Error: ${err.message}"
                    } else {
                        textResult.text =
                            "Address:\n${res.publicAddress}\n\nPrivate key:\n${res.privateKey}"
                    }
                    setIsLoggingIn(false)
                }
            }
        } else if (view == buttonGoogleLogin) {
            setIsLoggingIn(true)
            googleSignIn.revokeAccess()
                .continueWith { googleSignIn.signOut() }
                .continueWith {
                    startActivityForResult(
                        googleSignIn.signInIntent,
                        RC_GOOGLE_SIGN_IN
                    )
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            RC_GOOGLE_SIGN_IN -> {
                val task: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val googleAccount = task.getResult(ApiException::class.java)
                    customAuth.getTorusKey(
                        getString(R.string.torus_native_google_verifier_id),
                        googleAccount.email,
                        hashMapOf("verifier_id" to googleAccount.email),
                        googleAccount.idToken
                    ).whenComplete { res, err ->
                        runOnUiThread {
                            if (err != null) {
                                textResult.text = "Error: ${err.message}"
                            } else {
                                textResult.text =
                                    "Address:\n${res.publicAddress}\n\nPrivate key:\n${res.privateKey}"
                            }
                            setIsLoggingIn(false)
                        }
                    }
                } catch (err: ApiException) {
                    Log.e(javaClass.name, err.toString())
                    textResult.text = "Error: ${err.message}"
                }
            }
        }
    }
}