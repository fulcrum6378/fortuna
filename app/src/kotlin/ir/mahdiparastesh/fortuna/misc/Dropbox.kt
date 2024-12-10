package ir.mahdiparastesh.fortuna.misc

import androidx.core.content.edit
import com.dropbox.core.DbxException
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.WriteMode
import ir.mahdiparastesh.fortuna.BuildConfig
import ir.mahdiparastesh.fortuna.Kit
import ir.mahdiparastesh.fortuna.Main
import ir.mahdiparastesh.fortuna.R
import java.io.InputStream

@Suppress("RedundantSuspendModifier")
class Dropbox(private val c: Main) {
    val requestConfig = DbxRequestConfig("db-${BuildConfig.DROPBOX_APP_KEY}")
    val client get() = DbxClientV2(requestConfig, credential())
    var isAwaitingResult = false


    fun login() {
        Auth.startOAuth2PKCE(
            c, BuildConfig.DROPBOX_APP_KEY, requestConfig, listOf("files.content.write")
        )
        isAwaitingResult = true
    }

    fun onResume() {
        if (isAwaitingResult) {
            val authDbxCredential = Auth.getDbxCredential()
            isAwaitingResult = false
            if (authDbxCredential != null) c.sp.edit {
                putString(
                    Kit.SP_DROPBOX_CREDENTIAL, DbxCredential.Writer.writeToString(authDbxCredential)
                )
            }
        }
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

    suspend fun logout() {
        if (!isAuthenticated()) return
        client.auth().tokenRevoke()
        removeCredential()
    }


    suspend fun uploadFile(input: InputStream): UploadApiResponse = try {
        val fileMetadata = client
            .files()
            .uploadBuilder("/" + c.getString(R.string.export_file))
            .withMode(WriteMode.OVERWRITE)
            .uploadAndFinish(input)
        UploadApiResponse.Success(fileMetadata)
    } catch (exception: DbxException) {
        UploadApiResponse.Failure(exception)
    }

    sealed class UploadApiResponse {
        data class Success(val fileMetadata: FileMetadata) : UploadApiResponse()
        data class Failure(val exception: DbxException) : UploadApiResponse()
    }
}
