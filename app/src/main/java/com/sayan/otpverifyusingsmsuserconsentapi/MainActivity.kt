package com.sayan.otpverifyusingsmsuserconsentapi

import android.app.Activity
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import kotlinx.android.synthetic.main.activity_main.*

private const val CREDENTIAL_PICKER_REQUEST = 1  // Set to an unused request code
private const val SMS_CONSENT_REQUEST = 2  // Set to an unused request code

/**
 * Google documentation
 * https://www.youtube.com/watch?v=_-Hs6DdcZyY
 * https://developers.google.com/identity/sms-retriever/user-consent/overview
 * https://developers.google.com/identity/sms-retriever/user-consent/request
 */
class MainActivity : AppCompatActivity(){


    private val smsVerificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
                val extras = intent.extras
                val smsRetrieverStatus = extras?.get(SmsRetriever.EXTRA_STATUS) as Status

                when (smsRetrieverStatus.statusCode) {
                    CommonStatusCodes.SUCCESS -> {
                        // Get consent intent
                        val consentIntent = extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
                        try {
                            // Start activity to show consent dialog to user, activity must be started in
                            // 5 minutes, otherwise you'll receive another TIMEOUT intent
                            startActivityForResult(consentIntent, SMS_CONSENT_REQUEST)
                        } catch (e: ActivityNotFoundException) {
                            // Handle the exception ...
                            e.printStackTrace()
                        }
                    }
                    CommonStatusCodes.TIMEOUT -> {
                        // Time out occurred, handle the error.
                        println("SMS user consent timeout.")
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestHint()

        verifyButton.setOnClickListener {
            //get the input value
            val phoneNumber: String = phoneEditText.text.toString()
            //Use regex ^[6-9]\d{9}$ for indian mobile number checking
            if (phoneNumber.matches(Regex("^(?:(?:\\+|0{0,2})91(\\s*[\\-]\\s*)?|[0]?)?[6789]\\d{9}\$"))){
                verifyPhoneNumber(phoneNumber)
            }else{
                showToast("Phone number is invalid")
            }
        }

        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        registerReceiver(smsVerificationReceiver, intentFilter)
    }

    private fun verifyPhoneNumber(phoneNumber: String) {
        startListeningToOTPSMS()
        requestServerToSendOTP()
    }

    /**
     * Send OTP through your Server API here
     */
    private fun requestServerToSendOTP() {
        // you may check the work here by manually sending SMS from your other mobile-device.
        // make sure to add your number as smsGatewaySenderPhoneNumber in startListeningToOTPSMS() method
    }

    /**
     * Start listening for SMS User Consent broadcasts from smsGatewaySenderPhoneNumber.
     * The Task<Void> will be successful if SmsRetriever was able to start SMS User Consent,
     * and will error if there was an error starting.
     */
    private fun startListeningToOTPSMS() {
        val task = SmsRetriever.getClient(this).startSmsUserConsent(null /*or smsGatewaySenderPhoneNumber*/)
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
                    //fetch the phone number selected and set the phone number to the edit text
                    val credential = data.getParcelableExtra<Credential>(Credential.EXTRA_KEY)
                    println("Phone number: ${credential.id}")
                    phoneEditText.setText(credential.id)
                    //call verifyButton onClick method after a delay
                    Handler().postDelayed({
                        verifyButton.callOnClick()
                    },250)
                }
            SMS_CONSENT_REQUEST ->
                // Obtain the phone number from the result
                if (resultCode == Activity.RESULT_OK && data != null) {
                    // Get SMS message content
                    val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                    parseOTPAndCompleteVerification(message) // define this function
                } else {
                    println("Consent denied. User can type OTP manually.")
                }
        }
    }

    /**
     * Extract one-time code from the message and
     * complete verification `message` contains the entire text of the SMS message,
     * so you will need to parse the string.
     */
    private fun parseOTPAndCompleteVerification(message: String?){
        // send one time code to the server after parsing the OTP from the message String
        showToast("Phone number verified successfully!")
    }

    fun Context.showToast(message: String){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
