package com.busydoor.app.fcm

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.busydoor.app.R
import com.busydoor.app.activity.BottomNavigationBarActivity
import com.busydoor.app.activity.SplashActivity
import com.busydoor.app.customMethods.ACTIVITY_PREMISE_ID
import com.busydoor.app.customMethods.PrefUtils
import com.busydoor.app.customMethods.RetriveRequestOffsiteDate
import com.busydoor.app.customMethods.isNotify
import com.busydoor.app.customMethods.objSharedPref
import com.busydoor.app.service.BDApplication
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class MyFirebaseMessagingService : FirebaseMessagingService() {
    private var notificationManager: NotificationManagerCompat? = null
    private val CHANNEL_1_ID = "channel1"
    private val CHANNEL_2_ID = "channel2"
    private val CHANNEL_3_ID = "channel3"
    private val CHANNEL_4_ID = "channel4"
    private val Default_ID = "default"
    /** create var for the instance in bd-application... **/
    private lateinit var bdApplication: BDApplication
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("MyFirebase", "onNewToken: $token")
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun logout() {
        Log.e("hello check","autologout session")
        objSharedPref.putBoolean(getString(R.string.isLogin), false)
        if ((objSharedPref.getBoolean("isServiceRun"))) {
            // stop the service after user log out..
            stopBackgroundService()
        }
        // set empty to beaconMacAddress data for stop monitoring that regions...
        objSharedPref.putString("beaconMacAddress", "")
        // set empty to user data..
        objSharedPref.putString(getString(R.string.userResponse), "")
    }

    /** stopBackgroundService fun of ble_logic... **/
    @RequiresApi(Build.VERSION_CODES.R)
    fun stopBackgroundService(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bdApplication.stopForegroundService()
        }
    }
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        //logout()
        when (remoteMessage.notification!!.channelId) {
            "channel1" -> {
                sendOnChannel1(remoteMessage, remoteMessage.notification!!.imageUrl)
            }
            "channel2" -> {
                sendOnChannel2(remoteMessage, remoteMessage.notification!!.imageUrl)
            }
            "channel3" -> {
                sendOnChannel3(remoteMessage, remoteMessage.notification!!.imageUrl)
            }
            "channel4" -> {
                sendOnChannel4(remoteMessage,remoteMessage.notification!!.imageUrl)
            }
            "default" -> {
                sendOndefault(remoteMessage,remoteMessage.notification!!.imageUrl)
            }
            else -> {
                sendRequestOffsite(remoteMessage, remoteMessage.notification!!.imageUrl)
            }
        }
    }



    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        notificationManager = NotificationManagerCompat.from(this)
        objSharedPref = PrefUtils(this)
        /** assign the instance in bd-application... **/
        bdApplication = application as BDApplication

    }




    private fun createNotificationChannels() {
        Log.e("createnotify", "")
        val sound = Uri.parse("android.resource://" + packageName + "/" + R.raw.alarm)
        val sound1 = Uri.parse("android.resource://" + packageName + "/" + R.raw.error)
        val sound2 = Uri.parse("android.resource://" + packageName + "/" + R.raw.notification)
        val sound3 = Uri.parse("android.resource://" + packageName + "/" + R.raw.staff)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.e("Build.VERSION_CODES.O", "")

            //Channel one code here...//
            val channel1 = NotificationChannel(
                CHANNEL_1_ID,
                "Channel 1",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel1.description = "This is Channel 1"
            channel1.setSound(sound, audioAttributes)

            //Channel two code here...//
            val channel2 = NotificationChannel(
                CHANNEL_2_ID,
                "Channel 2",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel2.description = "This is Channel 2"
            channel2.setSound(sound1, audioAttributes)

            //Channel three code here...//
            val channel3 = NotificationChannel(
                CHANNEL_3_ID,
                "Channel 3",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel3.description = "This is Channel 3"
            channel3.setSound(sound2, audioAttributes)

            //Channel four code here...//
            val channel4 = NotificationChannel(
                CHANNEL_4_ID,
                "Channel 4",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel4.description = "This is Channel 4"
            channel4.setSound(sound3, audioAttributes)

            val default = NotificationChannel(
                Default_ID,
                "default",
                NotificationManager.IMPORTANCE_HIGH
            )
            default.description = "This is Channel default"
            //Channel creating code here...//
            val manager = getSystemService(
                NotificationManager::class.java
            )

            manager.createNotificationChannel(channel1)
            manager.createNotificationChannel(channel2)
            manager.createNotificationChannel(channel3)
            manager.createNotificationChannel(channel4)
            manager.createNotificationChannel(default)


        }

    }

    private fun sendOnChannel1(remoteMessage: RemoteMessage, imageUrl: Uri?) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.e("sendOnChannel1", ""+remoteMessage.notification!!.imageUrl)
            Log.e("sendOnChannel1", ""+remoteMessage.notification!!.title)

            val intent = Intent(this, SplashActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            // Create an Intent for the activity you want to start
            val resultIntent = Intent(this, SplashActivity::class.java)
            // Create the TaskStackBuilder
            val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
                // Add the intent, which inflates the back stack
                addNextIntentWithParentStack(resultIntent)
                // Get the PendingIntent containing the entire back stack
                getPendingIntent(0,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            }


            val title: String = remoteMessage.notification!!.title.toString()
            val message: String = remoteMessage.notification!!.body.toString()
            val notification1: Notification = NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setLargeIcon(getBitmapFromURL(imageUrl))
                .setSmallIcon(R.drawable.app_icon_notification)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(resultPendingIntent)
                .setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(getBitmapFromURL(imageUrl))
                )
                .build()
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notificationManager!!.notify(1, notification1)
        }
    }


    private fun sendOnChannel2(remoteMessage: RemoteMessage, images: Uri?) {
        val intent = Intent(this, SplashActivity::class.java)
        val title: String = remoteMessage.notification!!.title.toString()
        val message: String = remoteMessage.notification!!.body.toString()
        val notification2: Notification = NotificationCompat.Builder(this, CHANNEL_2_ID)
            .setSmallIcon(R.drawable.app_icon_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setLargeIcon(getBitmapFromURL(images))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setStyle(NotificationCompat.BigPictureStyle().bigPicture(getBitmapFromURL(images)))
            .build()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager!!.notify(2, notification2)
    }


    private fun sendOnChannel3(remoteMessage: RemoteMessage, imageUrl: Uri?) {
        Log.e("sendOnChannel3", ""+remoteMessage.notification!!.icon)
        val intent = Intent(this,SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val title: String = remoteMessage.notification!!.title.toString()
        val message: String = remoteMessage.notification!!.body.toString()


        val notification3: Notification = NotificationCompat.Builder(this, CHANNEL_3_ID)
            .setSmallIcon(R.drawable.app_icon_notification)
            .setContentTitle(title)
            .setAutoCancel(true)
            .setContentText(message)
            .setLargeIcon(getBitmapFromURL(imageUrl))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setStyle(NotificationCompat.BigPictureStyle().bigPicture(getBitmapFromURL(imageUrl)))
            .build()



        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager!!.notify(3, notification3)

    }


    private fun sendOnChannel4(remoteMessage: RemoteMessage, imageUrl: Uri?) {
        Log.e("sendOnChannel4", ""+remoteMessage.notification!!.icon )

        val intent = Intent(this,SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val title: String = remoteMessage.notification!!.title.toString()
        val message: String = remoteMessage.notification!!.body.toString()
        val notification4: Notification = NotificationCompat.Builder(this, CHANNEL_4_ID)
            .setSmallIcon(R.drawable.app_icon_notification)
            .setAutoCancel(true)
            .setContentTitle(title)
            .setContentText(message)
            .setLargeIcon(getBitmapFromURL(imageUrl))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setStyle(NotificationCompat.BigPictureStyle().bigPicture(getBitmapFromURL(imageUrl)))
            .build()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager!!.notify(4, notification4)
    }
    private fun sendOndefault(remoteMessage: RemoteMessage, imageUrl: Uri?) {
        Log.e("sendOnChannel5", "no==  "+remoteMessage.notification!!.toString())
        Log.e("sendOnChannel5", "data== "+ remoteMessage.data["date"])
        Log.e("sendOnChannel5", "data== "+ remoteMessage.data["premise_id"])
        Log.e("sendOnChannel5", "data== "+ remoteMessage.data["click_action"])
        Log.e("sendOnChannel5", "data== "+ remoteMessage.data.toString())
        Log.e("sendOnChannel5", "data== "+ remoteMessage.data["premise_id"])

        val intent = Intent(this,SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        // Create a PendingIntent to handle the tap on the notification
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val title: String = remoteMessage.notification!!.title.toString()
        val message: String = remoteMessage.notification!!.body.toString()
        val default: Notification = NotificationCompat.Builder(this, CHANNEL_4_ID)
            .setSmallIcon(R.drawable.app_icon_notification)
            .setAutoCancel(true)
            .setLargeIcon(getBitmapFromURL(imageUrl))
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setStyle(NotificationCompat.BigPictureStyle().bigPicture(getBitmapFromURL(imageUrl)))
            .build()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager!!.notify(5, default)
    }
    private fun sendRequestOffsite(remoteMessage: RemoteMessage, imageUrl: Uri?) {
//        Log.e("sendOnChannel5", "no===  "+remoteMessage.notification!!.toString())
//        Log.e("sendOnChannel5", "data=== "+ remoteMessage.data["date"])
//        Log.e("sendOnChannel5", "data=== "+ remoteMessage.data["premise_id"])
//        Log.e("sendOnChannel5", "data=== "+ remoteMessage.data["click_action"])
//        Log.e("sendOnChannel5", "data=== "+ remoteMessage.data.toString())
        Log.e("sendOnChannel5", "notification=== "+ remoteMessage.notification!!.clickAction.toString())
        Log.e("sendOnChannel5", "data=== "+ remoteMessage.data["premise_id"])

        if(remoteMessage.data.toString() !=null && remoteMessage.data.toString() !="null"){
            ACTIVITY_PREMISE_ID= remoteMessage.data["premise_id"].toString()
            RetriveRequestOffsiteDate= remoteMessage.data["date"].toString()
            isNotify=true
        }

        val intent = Intent(this,BottomNavigationBarActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        // Create a PendingIntent to handle the tap on the notification
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val title: String = remoteMessage.notification!!.title.toString()
        val message: String = remoteMessage.notification!!.body.toString()
        val default: Notification = NotificationCompat.Builder(this, CHANNEL_4_ID)
            .setSmallIcon(R.drawable.app_icon_notification)
            .setAutoCancel(true)
            .setLargeIcon(getBitmapFromURL(imageUrl))
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setStyle(NotificationCompat.BigPictureStyle().bigPicture(getBitmapFromURL(imageUrl)))
            .build()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager!!.notify(5, default)
    }


    private fun getBitmapFromURL(strURL: Uri?): Bitmap? {
        return try {
            val url = URL(strURL.toString())
            val connection =
                url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }


}



