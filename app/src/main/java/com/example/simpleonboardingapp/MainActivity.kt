package com.example.simpleonboardingapp

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.example.simpleonboardingapp.databinding.MainActivityBinding
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.json.jsonDeserializer
import com.iproov.androidapiclient.AssuranceType
import com.iproov.androidapiclient.ClaimType
import com.iproov.androidapiclient.kotlinfuel.ApiClientFuel
import com.iproov.sdk.api.IProov
import com.iproov.sdk.api.exception.SessionCannotBeStartedTwiceException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private val job = SupervisorJob()
    private var sessionStateJob: Job? = null
    private val uiScope = CoroutineScope(Dispatchers.Main + job)
    private lateinit var binding: MainActivityBinding

    val baseUrl = BuildConfig.BASE_URL
    val fuelUrl = BuildConfig.FUEL_URL
    val apiKey = BuildConfig.API_KEY
    val apiSecret = BuildConfig.API_SECRET

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val instructionFirst =  binding.instructionFirst
        val instructionFirstText =  instructionFirst.text.toString()
        val instructionFirstLink = "help"
        var startIndex = instructionFirstText.indexOf(instructionFirstLink)
        var endIndex = startIndex + instructionFirstLink.length

        var spannableString = SpannableString(instructionFirstText)

        val clickableFirstSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                binding.enrolGpaButton.visibility = View.GONE
                supportFragmentManager.commit  {
                    replace(R.id.fragmentContainerView, AdjustBrightnessTutorialFragment())
                    addToBackStack(null)
                }
            }
        }

        spannableString.setSpan(clickableFirstSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        instructionFirst.text = spannableString
        instructionFirst.movementMethod = LinkMovementMethod.getInstance()

        val instructionSecond =  binding.instructionSecond
        val instructionSecondText =  instructionSecond.text.toString()
        val instructionSecondLink = "scanning tips"
        startIndex = instructionSecondText.indexOf(instructionSecondLink)
        endIndex = startIndex + instructionSecondLink.length

        spannableString = SpannableString(instructionSecondText)

        val clickableSecondSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                binding.enrolGpaButton.visibility = View.GONE
                supportFragmentManager.commit  {
                    replace(R.id.fragmentContainerView, ScanningTipsFragment())
                    addToBackStack(null)
                }
            }
        }

        spannableString.setSpan(clickableSecondSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        instructionSecond.text = spannableString
        instructionSecond.movementMethod = LinkMovementMethod.getInstance()

        val instructionEnd =  binding.instructionEnd
        val instructionEndText =  instructionEnd.text.toString()
        val instructionEndLink = "here"
        spannableString = SpannableString(instructionEndText)
        startIndex = instructionEndText.indexOf(instructionEndLink)
        endIndex = startIndex + instructionEndLink.length

        spannableString.setSpan(URLSpan("https://iproov.com/blog/biometric-authentication-liveness-accessibility-inclusivity-wcag-regulations"), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        instructionEnd.text = spannableString
        instructionEnd.movementMethod = LinkMovementMethod.getInstance()

        binding.enrolGpaButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            if (username.isEmpty()) {
                Toast.makeText(this, getString(R.string.username_cannot_empty), Toast.LENGTH_SHORT).show()
            } else {
                launchIProov(ClaimType.ENROL, username, AssuranceType.GENUINE_PRESENCE)
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
                if (fragment != null) {
                    supportFragmentManager.beginTransaction().remove(fragment).commit()
                }
                binding.enrolGpaButton.visibility = View.VISIBLE
            }

        })

        IProov.session?.let { session ->
            observeSessionState(session)
        }
    }

    private fun observeSessionState(session: IProov.Session, whenReady: (() -> Unit)? = null) {
        sessionStateJob?.cancel()
        sessionStateJob = lifecycleScope.launch(Dispatchers.IO) {
            session.state.onSubscription { whenReady?.invoke() }
                .collect { state ->
                    if (sessionStateJob?.isActive == true) {
                        withContext(Dispatchers.Main) {
                            when (state) {
                                is IProov.State.Starting -> {
                                }
                                is IProov.State.Connecting ->
                                    binding.progressBar.isIndeterminate =
                                        true

                                is IProov.State.Connected ->
                                    binding.progressBar.isIndeterminate =
                                        false

                                is IProov.State.Processing ->
                                    binding.progressBar.progress =
                                        state.progress.times(100).toInt()

                                is IProov.State.Success -> onResult(
                                    getString(R.string.success),
                                    "",
                                )

                                is IProov.State.Failure -> onResult(
                                    state.failureResult.reason.feedbackCode,
                                    getString(state.failureResult.reason.description),
                                )

                                is IProov.State.Error -> onResult(
                                    getString(R.string.error),
                                    state.exception.localizedMessage,
                                )

                                is IProov.State.Canceled -> onResult(
                                    getString(R.string.canceled),
                                    null,
                                )
                            }
                        }
                    }
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // Cancel coroutine when Activity is destroyed
    }

    private fun launchIProov(claimType: ClaimType, username: String, assuranceType: AssuranceType) {
        binding.progressBar.visibility = View.VISIBLE
        binding.progressBar.isIndeterminate = true

        val apiClientFuel = ApiClientFuel(
            this,
            fuelUrl,
            apiKey,
            apiSecret,
        )

        uiScope.launch(Dispatchers.IO) {
            try {
                val token = apiClientFuel.getToken(
                    assuranceType,
                    claimType,
                    username,
                )

                if (!job.isActive) return@launch
                startScan(token)
            } catch (ex: Exception) {
                withContext(Dispatchers.Main) {
                    ex.printStackTrace()
                    if (ex is FuelError) {
                        val json = jsonDeserializer().deserialize(ex.response)
                        val description = json.obj().getString("error_description")
                        onResult(getString(R.string.error), description)
                    } else {
                        onResult(getString(R.string.error), getString(R.string.failed_to_get_token))
                    }
                }
            }
        }
    }

    @Throws(SessionCannotBeStartedTwiceException::class)
    private fun startScan(token: String) {
        IProov.createSession(applicationContext, baseUrl, token).let { session ->
            // Observe first, then start
            observeSessionState(session) {
                session.start()
            }
        }
    }

    private fun onResult(title: String?, resultMessage: String?) {
        binding.progressBar.progress = 0
        binding.progressBar.visibility = View.GONE

        AlertDialog.Builder(this@MainActivity)
            .setTitle(title)
            .setMessage(resultMessage)
            .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.cancel() }
            .show()
    }
}