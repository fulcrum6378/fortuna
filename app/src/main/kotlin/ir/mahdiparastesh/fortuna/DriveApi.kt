package ir.mahdiparastesh.fortuna

import android.app.Activity
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes

class DriveApi(private val c: Main) {
    private var drive: Drive? = null
    private val jsonFactory = GsonFactory()
    private val scopes = setOf(DriveScopes.DRIVE_FILE)

    private val signIn =
        c.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK || it.data == null) {
                Toast.makeText(c, "${it.resultCode} ${it.data!!}", Toast.LENGTH_LONG)
                    .show() // RESULT_CANCELED
                return@registerForActivityResult
            }
            GoogleSignIn.getSignedInAccountFromIntent(it.data)
                .addOnSuccessListener { googleAccount: GoogleSignInAccount ->
                    Toast.makeText(c, googleAccount.email, Toast.LENGTH_LONG).show()
                    drive = Drive.Builder(
                        AndroidHttp.newCompatibleTransport(), jsonFactory,
                        GoogleAccountCredential.usingOAuth2(c, scopes)
                            .apply { selectedAccount = googleAccount.account }
                    ).setApplicationName(c.getString(R.string.app_name)).build()
                }
                .addOnFailureListener { exception: Exception? ->
                    Toast.makeText(c, "Unable to sign in: $exception", Toast.LENGTH_LONG).show()
                }
        }

    /*@Throws(IOException::class)
    private fun getCredentials(httpTransport: NetHttpTransport): Credential? {
        val clientSecrets = GoogleClientSecrets.load(
            jsonFactory, InputStreamReader(c.resources.openRawResource(R.raw.credentials))
        )
        val flow = GoogleAuthorizationCodeFlow.Builder(
            httpTransport, jsonFactory, clientSecrets, scopes
        )
            .setDataStoreFactory(FileDataStoreFactory(c.filesDir))
            .setAccessType("offline")
            .build()
        val receiver = LocalServerReceiver.Builder().setPort(8888).build()
        return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }*/

    val pick =
        c.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK || it.data == null) return@registerForActivityResult
            //openFileFromFilePicker(it.data!!.data!!)
        }

    fun signIn() {
        signIn.launch(
            GoogleSignIn.getClient(
                c, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestScopes(Scope(DriveScopes.DRIVE_FILE))
                    .build()
            ).signInIntent
        )
        /*val httpTransport = AndroidHttp.newCompatibleTransport()
        drive = Drive.Builder(
            httpTransport, jsonFactory,
            getCredentials(httpTransport as NetHttpTransport)
        ).setApplicationName(c.getString(R.string.app_name)).build()*/
    }
}
