package ir.mahdiparastesh.fortuna.http

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.net.toUri
import fi.iki.elonen.NanoHTTPD
import ir.mahdiparastesh.fortuna.BuildConfig
import ir.mahdiparastesh.fortuna.Fortuna
import ir.mahdiparastesh.fortuna.R

class Server : Service() {
    private val c: Fortuna by lazy { applicationContext as Fortuna }
    private val cncManager by lazy { getSystemService(ConnectivityManager::class.java) }
    private val ntfManager by lazy { getSystemService(NotificationManager::class.java) }
    private var httpServer: HttpServer? = null

    companion object {
        private const val NTF_CHANNEL_ID = "serve"
        private const val NTF_ID = 202
    }

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent.action == "stop") {
            httpServer?.stop()
            httpServer = null
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        ntfManager.createNotificationChannel(notificationChannel())
        httpServer = HttpServer(
            cncManager.getLinkProperties(cncManager.activeNetwork)!!
        )
        startForeground(NTF_ID, notification())
        cncManager.registerDefaultNetworkCallback(MyNetworkCallback())
    }

    private fun notificationChannel() =
        NotificationChannel(
            NTF_CHANNEL_ID, getString(R.string.ntfServerTitle),
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = getString(R.string.ntfServerDesc) }

    private fun notification() =
        Notification.Builder(this, NTF_CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_monochrome)
            .setColor(resources.getColor(R.color.CP, theme))
            .setContentTitle(getString(R.string.ntfServer))
            .setAutoCancel(false)
            .setShowWhen(false)
            .addAction(
                Notification.Action.Builder(
                    null, getString(R.string.ntfServerBrowse),
                    PendingIntent.getActivity(
                        this, 0,
                        Intent(Intent.ACTION_VIEW, httpServer!!.address().toUri())
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                            PendingIntent.FLAG_MUTABLE
                        else PendingIntent.FLAG_UPDATE_CURRENT
                    )
                ).build()
            )
            .addAction(
                Notification.Action.Builder(
                    null, getString(R.string.ntfServerStop),
                    PendingIntent.getService(
                        this, 0,
                        Intent(this, Server::class.java)
                            .apply { action = "stop" },
                        PendingIntent.FLAG_IMMUTABLE
                    )
                ).build()
            )
            .build()


    inner class MyNetworkCallback : ConnectivityManager.NetworkCallback() {
        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            httpServer?.stop()
            httpServer = null
            httpServer = HttpServer(linkProperties)
        }
    }

    inner class HttpServer(linkProperties: LinkProperties) : NanoHTTPD(
        linkProperties.linkAddresses.find { it.prefixLength == 24 }?.address?.hostAddress,
        7007
    ) {
        init {
            start(SOCKET_READ_TIMEOUT, false)
        }

        override fun serve(session: IHTTPSession?): Response? = when (session?.uri) {

            "/" -> newFixedLengthResponse(
                Response.Status.OK,
                "text/html",
                readFile("index.html")
            )

            "/style.css" -> newFixedLengthResponse(
                Response.Status.OK,
                "text/css",
                readFile("style.css")
            )

            "/jquery-3.7.1.min.js" -> newFixedLengthResponse(
                Response.Status.OK,
                "text/javascript",
                readFile("jquery-3.7.1.min.js")
            )

            "/script.js" -> newFixedLengthResponse(
                Response.Status.OK,
                "text/javascript",
                readFile("script.js")
            )

            // TODO /favicon.ico

            "/calendar" -> newFixedLengthResponse(
                Response.Status.OK,
                "application/json",
                "{" +
                        "\"monthNames\": [" + resources.getStringArray(R.array.luna)
                    .joinToString(",") { "\"$it\"" } + "]" +
                        "}"
            )

            "/luna" -> {
                Log.d("LUNA", session.queryParameterString)
                newFixedLengthResponse(
                    Response.Status.OK,
                    "application/json",
                    "{" +
                            "}"
                )
            }

            else -> newFixedLengthResponse(
                Response.Status.NOT_FOUND,
                "text/plain",
                "Not Found!"
            )
        }.apply {
            if (BuildConfig.DEBUG) addHeader("Access-Control-Allow-Origin", "*")
        }

        fun address(): String = "http://$hostname:$listeningPort/"

        fun readFile(path: String): String = c.resources.assets.open("web_app/$path")
            .readBytes().toString(charset = Charsets.UTF_8)
    }
}
