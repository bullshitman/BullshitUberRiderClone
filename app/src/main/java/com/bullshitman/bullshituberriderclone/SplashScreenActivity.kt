package com.bullshitman.bullshituberriderclone

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.bullshitman.bullshituberriderclone.common.Common
import com.bullshitman.bullshituberriderclone.model.RiderModel
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_splash_screen.*
import java.util.concurrent.TimeUnit

private const val LOGIN_REQUEST_CODE = 7070
private const val TAG = "SplashScreen"

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var provider: List<AuthUI.IdpConfig>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var listener: FirebaseAuth.AuthStateListener

    private lateinit var database: FirebaseDatabase
    private lateinit var riderInfoRef: DatabaseReference

    override fun onStart() {
        super.onStart()
        delaySplashScreen()
    }

    override fun onStop() {
        if (firebaseAuth != null && listener != null) {
            firebaseAuth.removeAuthStateListener(listener)
        }
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        init()
    }

    private fun init() {
        database = FirebaseDatabase.getInstance()
        riderInfoRef = database.getReference(Common.RIDER_INFO_REFERENCE)
        provider = listOf(AuthUI.IdpConfig.PhoneBuilder().build(),
                          AuthUI.IdpConfig.GoogleBuilder().build())
        firebaseAuth = FirebaseAuth.getInstance()
        listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                checkUserFromFirebase()
            } else {
                showLoginLayout()
            }
        }
    }

    private fun showLoginLayout() {
        val authMethodPickerLayout = AuthMethodPickerLayout.Builder(R.layout.layout_sigh_in)
            .setPhoneButtonId(R.id.btn_phone_sign_in)
            .setGoogleButtonId(R.id.btn_google_sign_in)
            .build()
        startActivityForResult(AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAuthMethodPickerLayout(authMethodPickerLayout)
            .setTheme(R.style.LoginTheme)
            .setAvailableProviders(provider)
            .setIsSmartLockEnabled(false)
            .build(), LOGIN_REQUEST_CODE)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOGIN_REQUEST_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if(resultCode == Activity.RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser
            } else {
                Toast.makeText(this@SplashScreenActivity, response!!.error!!.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun delaySplashScreen() {
        Completable.timer(3, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
            .subscribe({firebaseAuth.addAuthStateListener(listener)})
    }
    private fun checkUserFromFirebase() {
        riderInfoRef.child(FirebaseAuth.getInstance().currentUser!!.uid)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    Toast.makeText(this@SplashScreenActivity, p0.message, Toast.LENGTH_LONG).show()
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        val model = p0.getValue(RiderModel::class.java)
                        goToHomeActivity(model)
                    } else {
                        showRegisterLayout()
                    }
                }

            })
    }

    private fun showRegisterLayout() {
        val builder = AlertDialog.Builder(this, com.google.firebase.database.R.style.DialogTheme)
        val itemView = LayoutInflater.from(this).inflate(com.google.firebase.database.R.layout.layout_register, null)
        val firstName = itemView.findViewById<View>(com.google.firebase.database.R.id.edit_first_name) as TextInputEditText
        val lastName = itemView.findViewById<View>(com.google.firebase.database.R.id.edit_last_name) as TextInputEditText
        val phoneNumber = itemView.findViewById<View>(com.google.firebase.database.R.id.edit_phone_number) as TextInputEditText
        val continueBtn = itemView.findViewById<View>(com.google.firebase.database.R.id.btn_continue) as Button

        if (FirebaseAuth.getInstance().currentUser!!.phoneNumber != null && TextUtils.isDigitsOnly(FirebaseAuth.getInstance().currentUser!!.phoneNumber)) {
            phoneNumber.setText(FirebaseAuth.getInstance().currentUser!!.phoneNumber)
        }
        builder.setView(itemView)
        val dialog = builder.create()
        dialog.show()
        continueBtn.setOnClickListener {
            when {
                TextUtils.isDigitsOnly(firstName.text.toString()) -> {
                    Toast.makeText(this@SplashScreenActivity, "Please, enter First Name", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                TextUtils.isDigitsOnly(lastName.text.toString()) -> {
                    Toast.makeText(this@SplashScreenActivity, "Please, enter Last Name", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                TextUtils.isDigitsOnly(phoneNumber.text.toString()) -> {
                    Toast.makeText(this@SplashScreenActivity, "Please, enter Phone Number", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                else -> {
                    val model = DriverInfoModel()
                    model.firstName = firstName.text.toString()
                    model.lastName = lastName.text.toString()
                    model.phoneNumber = phoneNumber.text.toString()

                    riderInfoRef.child(FirebaseAuth.getInstance().currentUser!!.uid)
                        .setValue(model)
                        .addOnFailureListener{ e ->
                            Toast.makeText(this@SplashScreenActivity, "${e.message}", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                            progress_bar.visibility = View.GONE
                        }
                        .addOnSuccessListener {
                            Toast.makeText(this@SplashScreenActivity, "Registered!", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                            goToHomeActivity(model)
                            progress_bar.visibility = View.GONE
                        }
                }
            }
        }
    }

    private fun goToHomeActivity(model: RiderModel?) {
        Common.currentRider = model
    }
}