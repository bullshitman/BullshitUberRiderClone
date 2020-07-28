package com.bullshitman.bullshituberriderclone.utils

import android.view.View
import com.bullshitman.bullshituberriderclone.common.Common
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object UserUtils {
    fun updateUserInfo(view: View, updateData: Map<String, Any>) {
        FirebaseDatabase.getInstance().getReference(Common.RIDER_INFO_REFERENCE)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .updateChildren(updateData)
            .addOnFailureListener { exception ->
                Snackbar.make(view, exception.message!!, Snackbar.LENGTH_LONG).show()
            }
            .addOnSuccessListener {
                Snackbar.make(view, "Info updated.", Snackbar.LENGTH_LONG).show()
            }
    }
}