package com.bullshitman.bullshituberriderclone.common

import com.bullshitman.bullshituberriderclone.model.RiderModel
import java.lang.StringBuilder

object Common {
    var currentRider: RiderModel? = null
    val RIDER_INFO_REFERENCE = "Rider"

    fun buildWelcomeMessage() = StringBuilder("Welcome, ${currentRider?.firstName} ${currentRider?.lastName}")
}