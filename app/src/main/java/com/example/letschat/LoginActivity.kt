package com.example.letschat

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.gms.auth.api.credentials.CredentialsApi.ACTIVITY_RESULT_NO_HINTS_AVAILABLE
import com.google.android.gms.auth.api.credentials.CredentialsOptions
import com.google.android.gms.auth.api.credentials.HintRequest
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    lateinit var countryCode: String
    lateinit var phoneNumberWithCountryCode: String

    companion object{
        var CREDENTIAL_PICKER_REQUEST=1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        phoneSelection()
        etNumber.addTextChangedListener {

            btnNext.isEnabled =
                !(it.isNullOrEmpty() || it.length < 10)  // if(( it.isNullOrEmpty() || it.length < 10 )){ btnNext.isEnabled=false }  else{ btnNext.isEnabled=true }
        }

        btnNext.setOnClickListener {
            formatTheNumber()
        }
    }

    private fun formatTheNumber() {
        countryCode = countryCodePicker.selectedCountryCodeWithPlus   //because firebase needs this plus sign also with the country code
        phoneNumberWithCountryCode = countryCode + etNumber.text.toString()
        setConfirmationDialogPrompt()
    }

    private fun setConfirmationDialogPrompt() {
        val alertDialog=AlertDialog.Builder(this)
            .setMessage("We will be verifying the phone number: \n \n$phoneNumberWithCountryCode \n \nIs this OK, or would you like to edit the number")
            .setPositiveButton("OK"){ _,_ ->   //using Lambda function; underscore means we are not providing any arguements in this function
                showTheOTPActivity()
            }
            .setNegativeButton("Edit"){ dialogInterface,_ ->
                dialogInterface.dismiss()
            }
            .setCancelable(false)
            .create()


        alertDialog.show()
    }

    private fun phoneSelection(){
        // To retrieve the Phone Number hints, Lets first configure the hint selector dialog by creating a HintRequest object.
      val hintRequest=HintRequest.Builder()
          .setPhoneNumberIdentifierSupported(true)
          .build()

        val credentialsoptions=CredentialsOptions.Builder()
            .forceEnableSaveDialog()
            .build()

        // Then, pass the HintRequest object to  credentialsClient.getHintPickerIntent() to get an intent to prompt the user to choose a phone number.
        val credentialsClient=Credentials.getClient(this,credentialsoptions)
        val intent=credentialsClient.getHintPickerIntent(hintRequest)

        try{
            startIntentSenderForResult(intent.intentSender, CREDENTIAL_PICKER_REQUEST,
                null,
                0,
                0,
                0,
            )
        }
        catch (e: IntentSender.SendIntentException){
            e.printStackTrace()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == CREDENTIAL_PICKER_REQUEST && resultCode == RESULT_OK){

            //get data from the dialog which is of 'Credential' type
            val credential:Credential?=data?.getParcelableExtra<Credential>(Credential.EXTRA_KEY)

            //set the received data to the edit text
            etNumber.setText(credential?.id?.substring(3))

        }else if (requestCode == CREDENTIAL_PICKER_REQUEST && resultCode == ACTIVITY_RESULT_NO_HINTS_AVAILABLE){
            Toast.makeText(this,"No phone numbers found",Toast.LENGTH_SHORT).show()
        }
    }

    private fun showTheOTPActivity() {
        val intent=Intent(this,OTPActivity::class.java)
        intent.putExtra(PHONE_NUMBER,phoneNumberWithCountryCode)
        startActivity(intent)
        finish()
    }
}