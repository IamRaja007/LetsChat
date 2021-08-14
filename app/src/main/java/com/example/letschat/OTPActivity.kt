package com.example.letschat

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import java.util.concurrent.*
import kotlinx.android.synthetic.main.activity_otp.*


const val PHONE_NUMBER="phoneNumber"

class OTPActivity:AppCompatActivity(), View.OnClickListener {

    private lateinit var callbacks:PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var progressDialogBox:AlertDialog
    private var phoneNumber:String? = null
    private var mStoredVerificationId:String?=null
    private var mResendToken:PhoneAuthProvider.ForceResendingToken?=null

    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)
        initializeTheViews()
        startVerificationProcess()   //have to call this after initializing the views because in that function we have initialized the callback
    }

    private fun showCountdownTimer(millisInFuture:Long) {
        btnResendCode.isEnabled=false
        object : CountDownTimer(millisInFuture,1000){  //countdown interval is 1sec i.e 1000 millis
            override fun onTick(millisUntilFinished: Long) {   //after every countdown interval, here 1 sec, it gets called
                tvtimer.isVisible=true
                tvtimer.text=getString(R.string.time_remaining,millisUntilFinished/1000)
            }

            override fun onFinish() {   //after the countdown is over
                btnResendCode.isEnabled=true
                tvtimer.text=getString(R.string.time_remaining,0)
            }

        }.start()
    }

    private fun startVerificationProcess(){

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber!!,
            60L,
            TimeUnit.SECONDS,
            this,
            callbacks
        )
        showCountdownTimer(60000)
        progressDialogBox=ProgressDialogBox.showDialog(this,"Sending a verification code",false)
        progressDialogBox.show()

    }

    private fun initializeTheViews() {
        phoneNumber=intent.getStringExtra(PHONE_NUMBER)
        tvVerifyNUMBER.text=getString(R.string.verify_number_otp,phoneNumber)
        setSpannableString()

        btnSendCode.setOnClickListener(this)
        btnResendCode.setOnClickListener(this)

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                /* This callback will be invoked in two situations:

                 1 - Instant verification. In some cases the phone number can be instantly
                    verified without needing to send or enter a verification code.
                 2 - Auto-retrieval. On some devices Google Play services can automatically
                     detect the incoming verification SMS and perform verification without
                     user action.
                */

                if(::progressDialogBox.isInitialized){
                    progressDialogBox.dismiss()
                }
                val smsCode=credential.smsCode
                if(!smsCode.isNullOrBlank()){
                    etOTP.setText(smsCode)
                }
                signInWithPhoneAuthCredential(credential)

            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.

                if(::progressDialogBox.isInitialized){
                    progressDialogBox.dismiss()
                }

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    Log.e("Exception:", "FirebaseAuthInvalidCredentialsException", e)
                    Log.e("=========:", "FirebaseAuthInvalidCredentialsException " + e.message)
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    Log.e("Exception:", "FirebaseTooManyRequestsException", e)
                }

                // Show a message and update the UI
               ProgressDialogBox.dialogWithPosNegBtn(this@OTPActivity,"Your phone number might be wrong or there may be a connection error. Please try again",false)
                   .setPositiveButton("OK"){_,_ ->
                    val intent=Intent(this@OTPActivity,LoginActivity::class.java)
                       startActivity(intent)
                   }
                   .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                   }
                   .create()
                   .show()

            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                /* The SMS verification code has been sent to the provided phone number, we
                 now need to ask the user to enter the code and then construct a credential
                 by combining the code with a verification ID.
                 */

                progressDialogBox.dismiss()
                tvtimer.isVisible=false
                Log.d("TAG", "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                mStoredVerificationId = verificationId
                mResendToken = token
            }
        }
    }



    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {

        val mAuth=FirebaseAuth.getInstance()
        mAuth.signInWithCredential(credential)
             .addOnCompleteListener {
                if (it.isSuccessful){

                    if(::progressDialogBox.isInitialized){
                        progressDialogBox.dismiss()
                    }
                    Toast.makeText(this, "Registered Succesfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this,SignUpProfileActivity::class.java))
                    finish()
                    //for first time login

                }else{
                    if(::progressDialogBox.isInitialized){
                        progressDialogBox.dismiss()
                    }

                    ProgressDialogBox.dialogWithPosNegBtn(this@OTPActivity,"Your phone number verification is failed. Please try again",false)
                        .setPositiveButton("OK"){_,_ ->
                            val intent=Intent(this@OTPActivity,LoginActivity::class.java)
                            startActivity(intent)
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()
                        .show()

                }
            }

    }

    private fun setSpannableString() {     //String which we can click
        val span=SpannableString(getString(R.string.waiting_for_otp,phoneNumber))
        val clickableSpan= object : ClickableSpan(){
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText=false     //by default every clickable link has an underline, so we need to remove it manually
                ds.color = getColor(R.color.Reddish)   //link text color
            }

            override fun onClick(widget: View) {
                /* on clicking this string what will happen we have to define here
                   in this case we want to send the user back to the previous activity
                 */
                val intent=Intent(this@OTPActivity,LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        //now we will set the span. meaning which text we want to make clickable and all
        span.setSpan(clickableSpan,span.length-13,span.length,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvwaitingforOTP.movementMethod= LinkMovementMethod.getInstance()
        tvwaitingforOTP.text=span
    }

    override fun onClick(v: View) {
        when(v){
            btnResendCode ->{
                if (mResendToken != null){
                    showCountdownTimer(60000)
                    progressDialogBox=ProgressDialogBox.showDialog(this,"Sending verification code..",false)
                    progressDialogBox.show()

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        phoneNumber!!,
                        60L,
                        TimeUnit.SECONDS,
                        this,
                        callbacks,
                        mResendToken
                    )
                }
                else{
                    Toast.makeText(this, "Sorry, You Can't request new code now, Please wait ...", Toast.LENGTH_SHORT).show()
                }

            }

            btnSendCode ->{
                val mCode=etOTP.text.toString()
                if(mCode.isNotEmpty() && !mStoredVerificationId.isNullOrEmpty()){
                    progressDialogBox=ProgressDialogBox.showDialog(this,"Please Wait..",false)
                    progressDialogBox.show()

                    val credential=PhoneAuthProvider.getCredential(mStoredVerificationId!!,mCode)
                    signInWithPhoneAuthCredential(credential)
                }
            }
        }
    }
}

//Along with this, I had to add SHA256 fingerprint of this app in firebase console