package ir.mahdiparastesh.fortuna.sect

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.drawable.RippleDrawable
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import ir.mahdiparastesh.fortuna.R
import ir.mahdiparastesh.fortuna.Vita
import ir.mahdiparastesh.fortuna.databinding.BackupBinding
import ir.mahdiparastesh.fortuna.util.Dropbox
import ir.mahdiparastesh.fortuna.util.Kit
import ir.mahdiparastesh.fortuna.util.Kit.color
import ir.mahdiparastesh.fortuna.util.Kit.create
import ir.mahdiparastesh.fortuna.util.NumberUtils.z
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream

/**
 * Shows the status of the automatically backed-up file with 3 action buttons:<br />
 * - Backup: manually backs up the data.<br />
 * - Restore: overwrites the backup file on the main file.<br />
 * - Export: exports the backup file.
 */
class BackupDialog : Kit.BaseDialogue() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val b = BackupBinding.inflate(layoutInflater)

        b.updateStatus()
        b.backup.setOnClickListener {
            c.c.backupVita()
            b.updateStatus()

            CoroutineScope(Dispatchers.IO).launch {
                val res = c.dropbox!!.backup()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        c, if (res) R.string.dropboxSuccess else R.string.dropboxFailure,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        b.restore.setOnClickListener {
            MaterialAlertDialogBuilder(c).apply {
                setTitle(R.string.restore)
                setMessage(
                    c.resources.getString(R.string.backupRestoreSure, lastBackup())
                )
                setPositiveButton(R.string.yes) { _, _ ->
                    c.c.vita = Vita(c.c, c.c.backup)
                        .also { vita -> vita.save() }
                    c.updateGrid()
                    Toast.makeText(c, R.string.done, Toast.LENGTH_LONG).show()
                }
                setNegativeButton(R.string.no, null)
            }.show()
        }
        b.export.setOnClickListener {
            if (!c.c.backup.exists()) return@setOnClickListener
            c.sendFile(
                FileInputStream(c.c.backup).use { it.readBytes() },
                R.string.backup_file
            )
        }

        if (c.dropbox == null) c.dropbox = Dropbox(c.c)
        b.updateDropbox()
        b.dropbox.background = RippleDrawable(
            ColorStateList.valueOf(
                c.color(com.google.android.material.R.attr.colorPrimaryVariant)
            ), null, MaterialShapeDrawable(
                ShapeAppearanceModel.Builder().apply {
                    val dim = resources.getDimension(R.dimen.mediumCornerSize)
                    setBottomLeftCorner(CornerFamily.CUT, dim)
                    setBottomRightCorner(CornerFamily.CUT, dim)
                }.build()
            )
        )

        return MaterialAlertDialogBuilder(c).apply {
            setTitle(R.string.backup)
            setMessage(R.string.backupDesc)
            setView(b.root)
        }.create()
    }

    override fun isCancelable(): Boolean = true

    /** @return the human-readable modification date of the backup. */
    private fun lastBackup(): String {
        if (!c.c.backup.exists()) return getString(R.string.never)
        val d = c.c.calType.create().apply { timeInMillis = c.c.backup.lastModified() }
        return "${z(d[Calendar.YEAR], 4)}.${z(d[Calendar.MONTH] + 1)}." +
                "${z(d[Calendar.DAY_OF_MONTH])} - ${z(d[Calendar.HOUR_OF_DAY])}:" +
                "${z(d[Calendar.MINUTE])}:${z(d[Calendar.SECOND])}"
    }

    /** Updates the modification date of the backup file. */
    private fun BackupBinding.updateStatus() {
        status.text = getString(
            R.string.backupStatus, lastBackup(), Kit.showBytes(c, c.c.backup.length())
        )
    }

    /** Checks whether the user has logged in to Dropbox and update the UI accordingly. */
    @SuppressLint("SetTextI18n")
    private fun BackupBinding.updateDropbox() {
        val auth = c.dropbox!!.isAuthenticated()

        dropbox.text = getString(R.string.dropbox) +
                (getString(if (auth) R.string.cloudOn else R.string.cloudOff))
        dropbox.setOnClickListener(
            if (!auth) View.OnClickListener {
                c.dropbox!!.login(c) { updateDropbox() }
            } else View.OnClickListener {
                MaterialAlertDialogBuilder(c).apply {
                    setTitle(R.string.logout)
                    setMessage(R.string.dropboxLogoutSure)
                    setPositiveButton(R.string.yes) { _, _ ->
                        CoroutineScope(Dispatchers.IO).launch {
                            c.dropbox!!.logout()
                            withContext(Dispatchers.Main) { updateDropbox() }
                        }
                    }
                    setNegativeButton(R.string.no, null)
                }.show()
            }
        )
    }
}
