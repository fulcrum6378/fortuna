package ir.mahdiparastesh.fortuna.util

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
import android.widget.Toast
import androidx.annotation.WorkerThread
import androidx.core.net.toUri
import fi.iki.elonen.NanoHTTPD
import ir.mahdiparastesh.fortuna.BuildConfig
import ir.mahdiparastesh.fortuna.Fortuna
import ir.mahdiparastesh.fortuna.Luna
import ir.mahdiparastesh.fortuna.Main
import ir.mahdiparastesh.fortuna.R
import ir.mahdiparastesh.fortuna.util.NumberUtils.toKey
import ir.mahdiparastesh.fortuna.util.NumberUtils.write
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.ServerSocket
import java.time.temporal.ChronoField

/**
 * An Android-served website that provides a Single-Paged Application version of Fortuna.
 *
 * Note: You can opt to use Ktor instead if NanoHTTPD won't operate well enough.
 */
class Server : Service() {
    private val c: Fortuna by lazy { applicationContext as Fortuna }
    private val cncManager by lazy { c.getSystemService(ConnectivityManager::class.java) }
    private val ntfManager by lazy { c.getSystemService(NotificationManager::class.java) }
    private var httpServer: HttpServer? = null

    companion object {
        const val TCP_PORT = 7007
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
        val network = cncManager.getLinkProperties(cncManager.activeNetwork)
        if (network != null) {

            // check if our port is free
            try {
                ServerSocket(TCP_PORT).close()
            } catch (_: IOException) {
                stopSelf()
                return
            }

            // check if a local server can be established
            try {
                httpServer = HttpServer(network)
            } catch (_: NoHostAddressException) {
                stopSelf()
                return
            }

            startForeground(NTF_ID, notification())
            cncManager.registerDefaultNetworkCallback(MyNetworkCallback())

        } else {
            Toast.makeText(
                c, R.string.ntfServerNoNetwork, Toast.LENGTH_SHORT
            ).show()
            stopSelf()
        }
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
            try {
                httpServer = HttpServer(linkProperties)
            } catch (_: NoHostAddressException) {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    inner class HttpServer(linkProperties: LinkProperties) : NanoHTTPD(
        try {
            linkProperties.linkAddresses.find { it.prefixLength == 24 }!!.address.hostAddress
        } catch (_: NullPointerException) {
            Toast.makeText(
                c, R.string.ntfServerNoHostAddress, Toast.LENGTH_LONG
            ).show()
            throw NoHostAddressException()
        },
        TCP_PORT
    ) {

        init {
            start(SOCKET_READ_TIMEOUT, false)
        }

        @WorkerThread
        override fun serve(session: IHTTPSession?): Response? = when (session?.uri) {

            "/" -> {
                val ass = readAsset("index.html")
                newFixedLengthResponse(
                    Response.Status.OK,
                    "text/html",
                    ass, ass.available().toLong()
                )
            }

            "/favicon.svg" -> {
                val ass = readAsset("favicon.svg")
                newFixedLengthResponse(
                    Response.Status.OK,
                    "image/svg+xml",
                    ass, ass.available().toLong()
                )
            }

            "/style.css" -> {
                val ass = readAsset("style.css")
                newFixedLengthResponse(
                    Response.Status.OK,
                    "text/css",
                    ass,
                    ass.available().toLong()
                )
            }

            "/jquery-3.7.1.min.js" -> {
                val ass = readAsset("jquery-3.7.1.min.js")
                newFixedLengthResponse(
                    Response.Status.OK,
                    "text/javascript",
                    ass, ass.available().toLong()
                )
            }

            "/script.js" -> {
                val ass = readAsset("script.js")
                newFixedLengthResponse(
                    Response.Status.OK,
                    "text/javascript",
                    ass, ass.available().toLong()
                )
            }

            "/quattrocento_bold.ttf" -> {
                val ass = readAsset("quattrocento_bold.ttf")
                newFixedLengthResponse(
                    Response.Status.OK,
                    "font/ttf",
                    ass, ass.available().toLong()
                )
            }

            "/quattrocento_regular.ttf" -> {
                val ass = readAsset("quattrocento_regular.ttf")
                newFixedLengthResponse(
                    Response.Status.OK,
                    "font/ttf",
                    ass, ass.available().toLong()
                )
            }
            // fonts are compressed and cannot be called from `c.assets.openNonAssetFd()`,
            // and `c.assets.openNonAsset()` is not exposed by the Android API.


            /* ---------- BEGINNING OF THE API ---------- */

            "/calendar" -> {
                val monthNames = resources.getStringArray(R.array.luna)
                    .joinToString(",") { "\"$it\"" }
                val numeral = Numerals.build(
                    c.sp.getString(Fortuna.SP_NUMERAL_TYPE, null)
                        ?.let { if (it == Fortuna.SP_NUMERAL_TYPE_DEF) null else it }
                )
                val maxDays = c.chronology.range(ChronoField.DAY_OF_MONTH).maximum.toInt()
                val numerals =
                    if (numeral != null)
                        "[" + List(maxDays) { numeral.write(it + 1) }
                            .joinToString(",") { "\"$it\"" } + "]"
                    else
                        "null"

                newFixedLengthResponse(
                    Response.Status.OK,
                    "application/json",
                    "{" +
                            "\"monthNames\":[" + monthNames + "]," +
                            "\"numerals\":" + numerals + "," +
                            "\"thisYear\":${c.todayDate[ChronoField.YEAR]}," +
                            "\"thisMonth\":${c.todayDate[ChronoField.MONTH_OF_YEAR]}," +
                            "\"thisDay\":${c.todayDate[ChronoField.DAY_OF_MONTH]}" +
                            "}"
                )
            }

            "/luna" -> {  // "?year=?&month=?"
                val year = session.parameters!!["year"]!![0].toInt()
                val month = session.parameters!!["month"]!![0].toInt()

                val date = c.chronology.date(year, month, 1)
                val len = date.lengthOfMonth()
                val lunaKey = date.toKey()
                val luna = if (lunaKey in c.vita) c.vita[lunaKey] else null

                newFixedLengthResponse(
                    Response.Status.OK,
                    "application/json",
                    "{" +
                            "\"dayCount\":$len," +
                            "\"defaultScore\":${luna?.default}," +
                            "\"defaultEmoji\":${luna?.emoji?.let { JSONObject.quote(it) }}," +
                            "\"defaultVerbum\":${luna?.verbum?.let { JSONObject.quote(it) }}," +
                            "\"scores\":${
                                List(len) { luna?.diebus[it] }
                            }," +
                            "\"emojis\":[${
                                List(len) { luna?.emojis[it] }
                                    .joinToString(",") { if (it != null) "\"$it\"" else "$it" }
                            }]," +
                            "\"verba\":${
                                List(len) { if (luna?.verba[it].isNullOrEmpty()) "0" else "1" }
                            }" +
                            "}"
                )
            }

            "/dies" -> {  // "?year=?&month=?&day=?"
                val year = session.parameters!!["year"]!![0].toInt()
                val month = session.parameters!!["month"]!![0].toInt()
                val day = session.parameters!!["day"]!![0].toInt()

                val date = c.chronology.date(year, month, if (day > 0) day else 1)
                val lunaKey = date.toKey()
                val luna = if (lunaKey in c.vita) c.vita[lunaKey] else null

                val score = if (day > 0) luna?.diebus[day - 1] else luna?.default
                val emoji = if (day > 0) luna?.emojis[day - 1] else luna?.emoji
                val verbum = if (day > 0) luna?.verba[day - 1] else luna?.verbum

                newFixedLengthResponse(
                    Response.Status.OK,
                    "application/json",
                    "{" +
                            "\"score\":${score}," +
                            "\"emoji\":${emoji?.let { JSONObject.quote(it) }}," +
                            "\"verbum\":${verbum?.let { JSONObject.quote(it) }}" +
                            "}"
                )
            }

            "/save" -> {  // "?year=?&month=?&day=?"
                val year = session.parameters!!["year"]!![0].toInt()
                val month = session.parameters!!["month"]!![0].toInt()
                val day = session.parameters!!["day"]!![0].toInt()

                // POST parameters
                val score = session.parameters!!["score"]!![0].toFloat()
                val emoji = session.parameters!!["emoji"]!![0]
                    .let { if (it == "null") null else it }
                val verbum = session.parameters!!["verbum"]!![0]
                    .let { if (it == "null") null else it }

                // find Luna
                val date = c.chronology.date(year, month, if (day > 0) day else 1)
                val len = date.lengthOfMonth()
                val lunaKey = date.toKey()
                if (lunaKey !in c.vita) c.vita[lunaKey] =
                    Luna(len, null, null, null)
                val luna = c.vita[lunaKey]

                // alter Luna
                luna.set(day - 1, score, emoji, verbum)
                c.vita.save()

                // update our UI here
                if (c.luna == lunaKey)
                    Main.handler?.obtainMessage(Main.HANDLE_VITA_DAY_CHANGED, day == 0)
                        ?.sendToTarget()

                newFixedLengthResponse(
                    Response.Status.OK,
                    "application/json",
                    "{\"status\":\"ok\"}"
                )
            }

            "/clear" -> {  // "?year=?&month=?&day=?"
                val year = session.parameters!!["year"]!![0].toInt()
                val month = session.parameters!!["month"]!![0].toInt()
                val day = session.parameters!!["day"]!![0].toInt()

                val date = c.chronology.date(year, month, if (day > 0) day else 1)
                val lunaKey = date.toKey()
                val luna = if (lunaKey in c.vita) c.vita[lunaKey] else null

                if (luna != null) {
                    luna.set(day - 1, null, null, null)
                    c.vita.save()

                    if (c.luna == lunaKey)
                        Main.handler?.obtainMessage(Main.HANDLE_VITA_DAY_CHANGED, day == 0)
                            ?.sendToTarget()
                }

                newFixedLengthResponse(
                    Response.Status.OK,
                    "application/json",
                    "{\"status\":\"ok\"}"
                )
            }

            /* ---------- END OF THE API ---------- */


            else -> newFixedLengthResponse(
                Response.Status.NOT_FOUND,
                "text/plain",
                "Not Found!"
            )
        }.apply {
            if (BuildConfig.DEBUG) addHeader("Access-Control-Allow-Origin", "*")
        }

        fun address(): String = "http://$hostname:$listeningPort/"

        fun readAsset(path: String): InputStream = c.resources.assets.open(path)
    }

    class NoHostAddressException :
        IllegalArgumentException("Could not find a proper host address")
}
