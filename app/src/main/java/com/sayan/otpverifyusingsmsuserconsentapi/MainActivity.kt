package com.sayan.otpverifyusingsmsuserconsentapi

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.gms.auth.api.credentials.HintRequest

class MainActivity : AppCompatActivity() {

    var userPhoneNumber: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestHint()
    }


    private val CREDENTIAL_PICKER_REQUEST = 1  // Set to an unused request code

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
                    userPhoneNumber= credential.id  //<-- will need to process phone number string
                    println("Phone number: $userPhoneNumber")
                }
        }
    }
}
