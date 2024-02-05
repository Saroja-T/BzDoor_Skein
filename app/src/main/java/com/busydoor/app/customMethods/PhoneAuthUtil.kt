package com.busydoor.app.customMethods

import android.app.Activity
import android.content.Context
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthOptions
import java.util.concurrent.TimeUnit


object PhoneAuthUtil {

    private var mAuth: FirebaseAuth? = null
    private var verificationId: String? = null
    private var forceResendingTokenGbl: PhoneAuthProvider.ForceResendingToken? = null
    private var verifyCode: String? = null

    fun sendVerificationCode(
        context: Context,
        activity: Activity,
        number: String,
        onSuccess: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        mAuth = FirebaseAuth.getInstance()
        val options = mAuth?.let {
            PhoneAuthOptions.newBuilder(it)
                .setPhoneNumber(number)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(createCallbacks(context, onSuccess, onFailed))
                .build()
        }
        if (options != null) {
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    private fun createCallbacks(
        context: Context,
        onSuccess: () -> Unit,
        onFailed: (String) -> Unit
    ): PhoneAuthProvider.OnVerificationStateChangedCallbacks {
        return object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onCodeSent(
                s: String,
                forceResendingToken: PhoneAuthProvider.ForceResendingToken
            ) {
                super.onCodeSent(s, forceResendingToken)
                forceResendingTokenGbl = forceResendingToken
                verificationId = s
                onSuccess.invoke()
            }

            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                val code = phoneAuthCredential.smsCode
                if (code != null) {
                    verifyCode = code
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                onFailed.invoke(e.message ?: "Verification failed.")
            }
        }
    }

    fun getVerificationId(): String? {
        return verificationId
    }

    fun getForceResendingToken(): PhoneAuthProvider.ForceResendingToken? {
        return forceResendingTokenGbl
    }

    fun getVerifyCode(): String? {
        return verifyCode
    }
}

