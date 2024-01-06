package com.busydoor.app.customMethods


import android.app.Activity
import android.content.Context
import com.google.firebase.auth.PhoneAuthProvider


/*shared preference*/
const val  CHECK_DIALOG_OPEN_CLOSE="check_dialog_open_close"

/*CODE*/
const val SUCCESS_CODE=200
const val LOGIN=1
const val ERROR_CODE = 400
const val REGISTER=2
const val HOME_DATA_GET=3
const val STAFF_LIST_DATA=4
const val STAFF_GRAPH_DATA=5
const val HOME_GRAPH_IMG_DATA =8
const val ADD_USER_TO_PREMISE =8
const val USER_ACTIVE_DEACTIVE=6
const val USER_STAFF_DATA=10
const val USER_KNOWN_DETAILS_DATA=12
const val USER_CONFIG_DATA=15
const val USER_ACCESSS=15
const val STATUS_GRAPH_DATA =18
const val USER_LIST_DATA =19
const val ATTENDANCE =23
const val QR_RESPONSE =24
const val BEACON_DETAILS =25
const val STAFF_BLUETOOTH_LOG= 26
const val STAFF_BLUETOOTH_OUT= 27
const val REQUEST_OFFSITE= 28
const val ALL_REQUEST_OFFSITE= 29


const val SerachUSER_LIST_DATA =20
const val UPDATE_USER =21
const val ADD_USER =22
var ADD_USER_RESPONSE =1
var ADD_USER_PREMISE =1
var showView = false

var minsOpenedAt=0.0
var hoursOpenedAt=0.0
var secsOpenedAt=0.0

var minsFirstCustomer=0.0
var hoursFirstCustomer=0.0
var secsFirstCustomer=0.0

var minsClosedAt=0.0
var hoursClosedAt=0.0
var secsClosedAt=0.0


var toolTipTime : String = ""
var toolTipDate : String = ""
var toolTipOpenAtTime : String = ""
var toolTipOpenAtDate : String = ""
var toolTipClosedAtTime : String = ""
var toolTipClosedAtDate : String = ""
var isUserHavingAccess : Boolean = false

const val key = "12345678901234561234567890123456"
const val ENCRYPTION_IV = "12345678901234561234567890123456"
var END_DATE = ""
var DATE_OPTION = ""
var DEVICE_TYPE: String = "Android"
var forceResendingTokenGbl: PhoneAuthProvider.ForceResendingToken? =null
lateinit var activity: Activity
lateinit var gContext:Context
var apiTriggered = true
var globalDate =""

