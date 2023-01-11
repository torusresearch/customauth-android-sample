package com.web3auth.custom_auth_wallet_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.web3auth.custom_auth_wallet_app.api.models.LoginVerifier
import com.web3auth.custom_auth_wallet_app.utils.*
import org.torusresearch.customauth.CustomAuth
import org.torusresearch.customauth.types.Auth0ClientOptions.Auth0ClientOptionsBuilder
import org.torusresearch.customauth.types.CustomAuthArgs
import org.torusresearch.customauth.types.LoginType
import org.torusresearch.customauth.types.SubVerifierDetails
import org.torusresearch.customauth.types.TorusLoginResponse
import java.util.concurrent.CompletableFuture


class OnBoardingActivity : AppCompatActivity() {

    private var expandFlag = false
    private lateinit var selectedNetwork: String
    private lateinit var ivFullLogin: AppCompatImageView
    private var torusSdk: CustomAuth? = null
    private lateinit var clBody: ConstraintLayout
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        supportActionBar?.hide()

        configureWeb3Auth()
        setUpListeners()
    }

    private fun setUpListeners() {
        clBody = findViewById(R.id.clBody)
        progressBar = findViewById(R.id.progressBar)
        val rlSocialLogins = findViewById<RelativeLayout>(R.id.rlSocialLogins)
        ivFullLogin = findViewById(R.id.ivFullLogin)
        ivFullLogin.setOnClickListener {
            expandFlag = if (expandFlag) {
                collapse(rlSocialLogins)
                false
            } else {
                expand(rlSocialLogins)
                true
            }
        }
        findViewById<AppCompatImageView>(R.id.ivBack).setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        findViewById<AppCompatButton>(R.id.btnContinue).setOnClickListener {
            signIn(
                LoginVerifier(
                    "Email Password",
                    LoginType.EMAIL_PASSWORD,
                    "BDIXq6ryHwTGwN11LFo4kwiMGY50zPip",
                    "torus-auth0-email-passwordless",
                    Web3AuthUtils.getDomain(selectedNetwork)
                ),
                ""
            )
        }
        findViewById<AppCompatImageView>(R.id.ivGoogle).setOnClickListener {
            signIn(
                LoginVerifier(
                    "Google",
                    LoginType.GOOGLE,
                    Web3AuthUtils.getClientId(selectedNetwork, LoginType.GOOGLE),
                    Web3AuthUtils.getVerifier(selectedNetwork, LoginType.GOOGLE)
                ),
                getString(R.string.google)
            )
        }
        findViewById<AppCompatImageView>(R.id.ivFacebook).setOnClickListener {
            signIn(
                LoginVerifier(
                    "Facebook", LoginType.FACEBOOK,
                    Web3AuthUtils.getClientId(selectedNetwork, LoginType.FACEBOOK),
                    Web3AuthUtils.getVerifier(selectedNetwork, LoginType.FACEBOOK)
                ),
                getString(R.string.facebook)
            )
        }
        findViewById<AppCompatImageView>(R.id.ivTwitter).setOnClickListener {
            signIn(
                LoginVerifier(
                    "Twitter",
                    LoginType.TWITTER,
                    Web3AuthUtils.getClientId(selectedNetwork, LoginType.TWITTER),
                    Web3AuthUtils.getVerifier(selectedNetwork, LoginType.TWITTER),
                    Web3AuthUtils.getDomain(selectedNetwork)
                ),
                getString(R.string.twitter)
            )
        }
        findViewById<AppCompatImageView>(R.id.ivDiscord).setOnClickListener {
            signIn(
                LoginVerifier(
                    "Discord", LoginType.DISCORD,
                    Web3AuthUtils.getClientId(selectedNetwork, LoginType.DISCORD),
                    Web3AuthUtils.getVerifier(selectedNetwork, LoginType.DISCORD)
                ),
                getString(R.string.discord)
            )
        }
        findViewById<AppCompatImageView>(R.id.ivLine).setOnClickListener {
            signIn(
                LoginVerifier(
                    "Line",
                    LoginType.LINE,
                    Web3AuthUtils.getClientId(selectedNetwork, LoginType.LINE),
                    Web3AuthUtils.getVerifier(selectedNetwork, LoginType.LINE),
                    Web3AuthUtils.getDomain(selectedNetwork)
                ),
                getString(R.string.line)
            )
        }
        findViewById<AppCompatImageView>(R.id.ivApple).setOnClickListener {
            signIn(
                LoginVerifier(
                    "Apple",
                    LoginType.APPLE,
                    Web3AuthUtils.getClientId(selectedNetwork, LoginType.APPLE),
                    Web3AuthUtils.getVerifier(selectedNetwork, LoginType.APPLE),
                    Web3AuthUtils.getDomain(selectedNetwork)
                ),
                getString(R.string.apple)
            )
        }
        findViewById<AppCompatImageView>(R.id.ivLinkedin).setOnClickListener {
            signIn(
                LoginVerifier(
                    "LinkedIn",
                    LoginType.LINKEDIN,
                    Web3AuthUtils.getClientId(selectedNetwork, LoginType.LINKEDIN),
                    Web3AuthUtils.getVerifier(selectedNetwork, LoginType.LINKEDIN),
                    Web3AuthUtils.getDomain(selectedNetwork)
                ),
                getString(R.string.linkedin)
            )
        }
        findViewById<AppCompatImageView>(R.id.ivGithub).setOnClickListener {
            signIn(
                LoginVerifier(
                    "Github",
                    LoginType.GITHUB,
                    Web3AuthUtils.getClientId(selectedNetwork, LoginType.GITHUB),
                    Web3AuthUtils.getVerifier(selectedNetwork, LoginType.GITHUB),
                    Web3AuthUtils.getDomain(selectedNetwork)
                ),
                getString(R.string.github)
            )
        }
    }

    private fun configureWeb3Auth() {
        selectedNetwork =
            this.applicationContext.web3AuthWalletPreferences.getString(NETWORK, "")
                .toString()
        println("Selected N/w: $selectedNetwork")

        val args = CustomAuthArgs(
            Web3AuthUtils.getRedirectUri(selectedNetwork), //"redirect-uri"
            NetworkUtils.getTorusNetwork(selectedNetwork),
            Web3AuthUtils.getBrowserRedirectUri(selectedNetwork) //browser-redirect-uri
        )

        // Initialize CustomAuth
        this.torusSdk = CustomAuth(args, this)
    }

    private fun signIn(loginVerifier: LoginVerifier, loginType: String) {
        this.applicationContext.web3AuthWalletPreferences[loginType] = loginType

        var builder: Auth0ClientOptionsBuilder? = null
        if (loginVerifier.domain != null) {
            builder = Auth0ClientOptionsBuilder(loginVerifier.domain)
            builder.setVerifierIdField(loginVerifier.verifierIdField)
            builder.setVerifierIdCaseSensitive(loginVerifier.isVerfierIdCaseSensitive)
        }
        clBody.hide()
        progressBar.show()
        val torusLoginResponseCf: CompletableFuture<TorusLoginResponse>? = if (builder == null) {
            torusSdk?.triggerLogin(
                SubVerifierDetails(
                    loginVerifier.typeOfLogin,
                    loginVerifier.verifier,
                    loginVerifier.clientId
                )
                    .setPreferCustomTabs(true)
                    .setAllowedBrowsers(ALLOWED_CUSTOM_TABS_PACKAGES)
            )
        } else {
            torusSdk?.triggerLogin(
                SubVerifierDetails(
                    loginVerifier.typeOfLogin,
                    loginVerifier.verifier,
                    loginVerifier.clientId, builder.build()
                )
                    .setPreferCustomTabs(true)
                    .setAllowedBrowsers(ALLOWED_CUSTOM_TABS_PACKAGES)
            )
        }

        torusLoginResponseCf?.whenComplete { torusLoginResponse: TorusLoginResponse, error: Throwable? ->
            if (error != null) {
                Log.d(
                    getString(R.string.onboarding_error),
                    error.message ?: getString(R.string.something_went_wrong)
                )
            } else {
                this.applicationContext.web3AuthWalletPreferences[LOGIN_RESPONSE] =
                    torusLoginResponse
                this.applicationContext.web3AuthWalletPreferences[ISONBOARDED] = true
                startActivity(Intent(this@OnBoardingActivity, MainActivity::class.java))
                finish()
            }
            clBody.show()
            progressBar.hide()
        }
    }

    private fun expand(v: View) {
        v.measure(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        val targetHeight: Int = v.measuredHeight

        v.layoutParams.height = 1
        v.visibility = View.VISIBLE

        val a: Animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                v.layoutParams.height = if (interpolatedTime == 1f)
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                else
                    (targetHeight * interpolatedTime).toInt()
                v.requestLayout()

            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }

        a.duration = (targetHeight / v.context.resources.displayMetrics.density).toInt().toLong()
        v.startAnimation(a)
        ivFullLogin.animate().rotationBy(180F).setDuration(300).start()
    }

    private fun collapse(v: View) {
        val initialHeight: Int = v.measuredHeight
        val a: Animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                if (interpolatedTime == 1f) {
                    v.visibility = View.GONE
                } else {
                    v.layoutParams.height =
                        initialHeight - (initialHeight * interpolatedTime).toInt()
                    v.requestLayout()
                }
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }
        a.duration = (initialHeight / v.context.resources.displayMetrics.density).toInt().toLong()
        v.startAnimation(a)
        ivFullLogin.animate().rotationBy(180F).setDuration(300).start()
    }
}