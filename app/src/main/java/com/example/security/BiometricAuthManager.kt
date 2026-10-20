package com.example.security

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class BiometricAuthResult {
    object Success : BiometricAuthResult()
    data class Error(val errorCode: Int, val errString: CharSequence) : BiometricAuthResult()
    object Failed : BiometricAuthResult()
    object NotAvailable : BiometricAuthResult()
}

class BiometricAuthManager(private val context: Context) {

    private val credentialManager = CredentialManager.create(context)
    private val biometricManager = BiometricManager.from(context)

    fun canAuthenticateBiometrics(): Boolean {
        val authenticators = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        } else {
            BIOMETRIC_WEAK or DEVICE_CREDENTIAL
        }
        val canAuth = biometricManager.canAuthenticate(authenticators)
        return canAuth == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun getBiometricStatusDescription(): String {
        val authenticators = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        } else {
            BIOMETRIC_WEAK or DEVICE_CREDENTIAL
        }
        return when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> "Biometric hardware ready (Fingerprint / Face Unlock)"
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "Biometric hardware not detected"
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Biometric sensor currently unavailable"
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "No fingerprint or face enrolled in system"
            else -> "Biometric authentication supported via Credential Manager"
        }
    }

    /**
     * Authenticate using BiometricPrompt for user logins or payment authorization
     */
    fun authenticateWithBiometricPrompt(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        negativeButtonText: String = "Use UPI PIN",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(context)

        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setConfirmationRequired(false)

        val authenticators = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        } else {
            BIOMETRIC_WEAK
        }

        // On Android 10 and below or when device credentials are not mixed with negative button
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            promptInfoBuilder.setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
        } else {
            promptInfoBuilder.setNegativeButtonText(negativeButtonText)
        }

        val promptInfo = promptInfoBuilder.build()

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // If user cancelled or pressed negative button, inform gracefully
                    if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                        errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON
                    ) {
                        onError("Authentication cancelled")
                    } else {
                        onError(errString.toString())
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onError("Biometric authentication failed. Please try again.")
                }
            }
        )

        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            Log.e("BiometricAuthManager", "Failed to launch BiometricPrompt", e)
            onError(e.message ?: "Could not open biometric prompt")
        }
    }

    /**
     * Authenticate using CredentialManager Passkey/Public Key credential option
     */
    suspend fun authenticateWithCredentialManager(
        activity: FragmentActivity,
        requestJson: String = """{"challenge":"xpay_biometric_auth","rpId":"com.aistudio.xpay"}"""
    ): BiometricAuthResult = withContext(Dispatchers.IO) {
        try {
            val getPublicKeyOption = GetPublicKeyCredentialOption(requestJson = requestJson)
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(getPublicKeyOption)
                .build()

            val response: GetCredentialResponse = credentialManager.getCredential(
                context = activity,
                request = request
            )
            Log.d("BiometricAuthManager", "Credential received: ${response.credential.type}")
            BiometricAuthResult.Success
        } catch (e: GetCredentialException) {
            Log.w("BiometricAuthManager", "CredentialManager getCredential failed: ${e.message}")
            BiometricAuthResult.Error(1, e.message ?: "Credential authentication failed")
        } catch (e: Exception) {
            Log.e("BiometricAuthManager", "Unexpected exception in CredentialManager", e)
            BiometricAuthResult.Error(0, e.message ?: "Authentication error")
        }
    }
}
