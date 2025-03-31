package ir.mahdiparastesh.fortuna.util

import androidx.core.content.edit
import com.dropbox.core.DbxException
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import ir.mahdiparastesh.fortuna.BuildConfig
import ir.mahdiparastesh.fortuna.Fortuna
import ir.mahdiparastesh.fortuna.Main
import ir.mahdiparastesh.fortuna.R
import java.io.FileInputStream

/**
 * Wrapper for the Dropbox Java API which can log in to Dropbox and upload files.
 *
 * @see <a href="https://github.com/dropbox/dropbox-sdk-java/">dropbox-sdk-java</a>
 */
@Suppress("RedundantSuspendModifier")
class Dropbox(private val c: Fortuna) {
    private var awaitingLogin: (() -> Unit)? = null

    fun requestConfig() = DbxRequestConfig("db-${BuildConfig.DROPBOX_APP_KEY}")

    fun login(c: Main, listener: () -> Unit) {
        Auth.startOAuth2PKCE(
            c, BuildConfig.DROPBOX_APP_KEY, requestConfig(), listOf("files.content.write")
        )
        awaitingLogin = listener
    }

    fun onResume() {
        if (awaitingLogin == null) return
        Auth.getDbxCredential()?.also { credential ->
            c.sp.edit {
                putString(Kit.SP_DROPBOX_CREDENTIAL, DbxCredential.Writer.writeToString(credential))
            }
        }
        awaitingLogin!!()
        awaitingLogin = null
    }

    fun credential(): DbxCredential? = try {
        c.sp.getString(Kit.SP_DROPBOX_CREDENTIAL, null)?.let { DbxCredential.Reader.readFully(it) }
    } catch (_: Exception) {
        removeCredential()
        null
    }

    fun isAuthenticated() = credential() != null

    fun removeCredential() {
        c.sp.edit { remove(Kit.SP_DROPBOX_CREDENTIAL) }
    }

    fun client() = DbxClientV2(requestConfig(), credential())

    /** @return true if the backup was successful */
    suspend fun backup(): Boolean {
        if (!isAuthenticated()) return false
        val fis = FileInputStream(c.stored)
        val success = try {
            client()
                .files()
                .uploadBuilder("/" + c.getString(R.string.export_file))
                .withMode(WriteMode.OVERWRITE)
                .uploadAndFinish(fis)
            true
        } catch (_: DbxException) { // includes when no network is available
            false
        }
        fis.close()
        return success
    }

    suspend fun logout() {
        if (!isAuthenticated()) return
        try {
            client().auth().tokenRevoke()
        } catch (_: DbxException) {
        }
        removeCredential()
    }
}
