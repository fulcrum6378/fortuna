package ir.mahdiparastesh.fortuna.misc

import android.Manifest
import android.app.Activity
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import ir.mahdiparastesh.fortuna.Main
import ir.mahdiparastesh.fortuna.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DriveApi(private val c: Main) {
    private var acc: GoogleSignInAccount? = null
    private var drive: Drive? = null
    private val scopes = setOf(DriveScopes.DRIVE_READONLY, DriveScopes.DRIVE_FILE)

    private val signIn =
        c.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
            if (res.resultCode != Activity.RESULT_OK || res.data == null)
                return@registerForActivityResult // problem with the app in Google Cloud
            GoogleSignIn.getSignedInAccountFromIntent(res.data)
                .addOnSuccessListener { acc = it; initDrive() }
                .addOnFailureListener { exception: Exception? ->
                    Toast.makeText(c, "Unable to sign in: $exception", Toast.LENGTH_LONG).show()
                }
        }

    fun signIn() {
        if (acc != null) {
            initDrive()
            return; }
        acc = GoogleSignIn.getLastSignedInAccount(c)
        val mScopes = scopes.map { Scope(it) }
        if (acc == null) signIn.launch(
            GoogleSignIn.getClient(
                c, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestScopes(mScopes[0], *mScopes.subList(1, mScopes.size).toTypedArray())
                    .build()
            ).signInIntent
        ) else initDrive()
    }

    private fun initDrive() {
        if (acc == null || drive != null) {
            query(); return; }
        drive = Drive.Builder(
            AndroidHttp.newCompatibleTransport(), GsonFactory(),
            GoogleAccountCredential.usingOAuth2(c, scopes).apply { selectedAccount = acc!!.account }
        ).setApplicationName(c.getString(R.string.app_name)).build()
        query()
    }

    @RequiresPermission(Manifest.permission.INTERNET)
    private fun query() {
        if (drive == null) return
        CoroutineScope(Dispatchers.IO).launch {
            val rootId = drive!!.files().get("root").setFields("id").execute().id
            val list = drive!!.files().list()
                .setOrderBy("name")
                .setFields("files(id, name)")
                .setQ(
                    "\'$rootId\' in parents and trashed=false and " +
                            "mimeType=\'application/vnd.google-apps.folder\'"
                )
                .setSpaces("drive")
                .execute()
            val sb = StringBuilder()
            for (f: File in list.files) sb.append("${f.name}\n")//${f.id} :
            if (list.files.isNullOrEmpty())
                sb.append("No files found; kind=${list.kind} incompleteSearch:${list.incompleteSearch}")
            withContext(Dispatchers.Main) {
                MaterialAlertDialogBuilder(c).setTitle("Test").setMessage(sb.toString()).show()
            }
        }
    }

    fun signOut() {
        if (acc == null) {
            Toast.makeText(c, "not signed in", Toast.LENGTH_SHORT).show()
            return; }
        GoogleSignIn.getClient(
            c, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        ).apply {
            revokeAccess()
            drive = null
            signOut()
            acc = null
        }
    }
}
