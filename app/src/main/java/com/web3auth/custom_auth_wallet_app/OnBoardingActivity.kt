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
    private val selectedLoginVerifier: LoginVerifier? = null
    private var domain = "torus-test.auth0.com"
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
                    "sqKRBVSdwa4WLkaq419U7Bamlh5vK1H7",
                    "torus-auth0-email-password",
                    domain
                ),
                ""
            )
        }
        findViewById<AppCompatImageView>(R.id.ivGoogle).setOnClickListener {
            signIn(
                LoginVerifier(
                    "Google",
                    LoginType.GOOGLE,
                    "221898609709-obfn3p63741l5333093430j3qeiinaa8.apps.googleusercontent.com",
                    "google-lrc"
                ),
                getString(R.string.google)
            )
        }
        findViewById<AppCompatImageView>(R.id.ivFacebook).setOnClickListener {
            signIn(
                LoginVerifier("Facebook", LoginType.FACEBOOK, "617201755556395", "facebook-lrc"),
                getString(R.string.facebook)
            )
        }
        findViewById<AppCompatImageView>(R.id.ivTwitter).setOnClickListener {
            signIn(
                LoginVerifier(
                    "Twitter",
                    LoginType.TWITTER,
                    "A7H8kkcmyFRlusJQ9dZiqBLraG2yWIsO",
                    "torus-auth0-twitter-lrc",
                    domain
                ),
                getString(R.string.twitter)
            )
        }
        findViewById<AppCompatImageView>(R.id.ivDiscord).setOnClickListener {
            signIn(
                LoginVerifier("Discord", LoginType.DISCORD, "682533837464666198", "discord-lrc"),
                getString(R.string.discord)
            )
        }
        findViewById<AppCompatImageView>(R.id.ivLine).setOnClickListener {
            signIn(
                LoginVerifier(
                    "Line",
                    LoginType.APPLE,
                    "WN8bOmXKNRH1Gs8k475glfBP5gDZr9H1",
                    "torus-auth0-line-lrc",
                    domain
                ),
                getString(R.string.line)
            )
        }
        findViewById<AppCompatImageView>(R.id.ivApple).setOnClickListener {
            signIn(
                LoginVerifier(
                    "Apple",
                    LoginType.APPLE,
                    "m1Q0gvDfOyZsJCZ3cucSQEe9XMvl9d9L",
                    "torus-auth0-apple-lrc",
                    domain
                ),
                getString(R.string.apple)
            )
        }
        findViewById<AppCompatImageView>(R.id.ivLinkedin).setOnClickListener {
            signIn(
                LoginVerifier(
                    "LinkedIn",
                    LoginType.LINKEDIN,
                    "59YxSgx79Vl3Wi7tQUBqQTRTxWroTuoc",
                    "torus-auth0-linkedin-lrc",
                    domain
                ),
                getString(R.string.linkedin)
            )
        }
        findViewById<AppCompatImageView>(R.id.ivGithub).setOnClickListener {
            signIn(
                LoginVerifier(
                    "Github",
                    LoginType.GITHUB,
                    "PC2a4tfNRvXbT48t89J5am0oFM21Nxff",
                    "torus-auth0-github-lrc",
                    domain
                ),
                getString(R.string.github)
            )
        }
        findViewById<AppCompatImageView>(R.id.ivTwitch).setOnClickListener {
            signIn(
                LoginVerifier(
                    "Twitch",
                    LoginType.TWITCH,
                    "f5and8beke76mzutmics0zu4gw10dj",
                    "twitch-lrc"
                ),
                getString(R.string.twitch)
            )
        }
    }

    private fun configureWeb3Auth() {
        selectedNetwork =
            this.applicationContext.web3AuthWalletPreferences.getString(NETWORK, "")
                .toString()

        val args = CustomAuthArgs(
            "https://scripts.toruswallet.io/redirect.html",
            NetworkUtils.getTorusNetwork(selectedNetwork),
            "torusapp://org.torusresearch.customauthandroid/redirect"
        )

        // Initialize CustomAuth
        this.torusSdk = CustomAuth(args, this)
    }

    private fun signIn(loginVerifier: LoginVerifier, loginType: String) {
        this.applicationContext.web3AuthWalletPreferences[loginType] = loginType

        var builder: Auth0ClientOptionsBuilder? = null
        if (this.selectedLoginVerifier?.domain != null) {
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