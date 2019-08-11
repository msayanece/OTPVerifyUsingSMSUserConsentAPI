package com.sayan.otpverifyusingsmsuserconsentapi

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.gms.auth.api.credentials.HintRequest
import kotlinx.android.synthetic.main.activity_main.*

private const val CREDENTIAL_PICKER_REQUEST = 1  // Set to an unused request code

/**
 * Google documentation
 * https://www.youtube.com/watch?v=_-Hs6DdcZyY
 * https://developers.google.com/identity/sms-retriever/user-consent/overview
 * https://developers.google.com/identity/sms-retriever/user-consent/request
 */
class MainActivity : AppCompatActivity(){

    var userPhoneNumber: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestHint()

        verifyButton.setOnClickListener {
            //get the input value
            val phoneNumber: String = phoneEditText.text.toString()
            //Use regex ^[6-9]\d{9}$ for indian mobile number checking
            if (phoneNumber.matches(Regex("^(?:(?:\\+|0{0,2})91(\\s*[\\-]\\s*)?|[0]?)?[6789]\\d{9}\$"))){
                verifyPhoneNumber()
            }else{
                showToast("Phone number is invalid")
            }

        }
    }

    private fun verifyPhoneNumber() {
        showToast("Phone number verified successfully")
    }

    // Construct a request for phone numbers and show the picker
    private fun requestHint() {
        //create hint request
        val hintRequest = HintRequest.Builder()
            .setPhoneNumberIdentifierSupported(true)
            .build()
        //get credentials client
        val credentialsClient = Credentials.getClient(this)
        //get the hint picker intent using hint request on credentials client
        val intent = credentialsClient.getHintPickerIntent(hintRequest)
        //start hint picker
        startIntentSenderForResult(
            intent.intentSender,
            CREDENTIAL_PICKER_REQUEST,
            null, 0, 0, 0
        )
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CREDENTIAL_PICKER_REQUEST ->
                // Obtain the phone number from the result of hint picker
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val credential = data.getParcelableExtra<Credential>(Credential.EXTRA_KEY)
                    println("Phone number: ${credential.id}")
                    phoneEditText.setText(credential.id)
                    Handler().postDelayed({
                        verifyButton.callOnClick()
                    },250)
                }
        }
    }

    fun Context.showToast(message: String){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
