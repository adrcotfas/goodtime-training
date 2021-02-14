package goodtime.training.wod.timer.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.LifecycleCoroutineScope
import goodtime.training.wod.timer.common.FileUtils
import goodtime.training.wod.timer.common.StringUtils
import goodtime.training.wod.timer.common.executeAsyncTask
import goodtime.training.wod.timer.data.db.GoodtimeDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class BackupOperations {
    companion object {
        fun doImport(scope: LifecycleCoroutineScope, context: Context, uri: Uri) {
            scope.executeAsyncTask(
                onPreExecute = {},
                doInBackground = {
                    try {
                        val tmpStream = context.contentResolver.openInputStream(uri)
                        val tmpPath = File(context.filesDir, "")
                        val tempFile = File.createTempFile("import", null, tmpPath)
                        var fileName: String? = null
                        tempFile.deleteOnExit()
                        FileUtils.copy(tmpStream, tempFile)
                        context.contentResolver.query(
                            uri, null, null, null, null
                        ).use { cursor ->
                            if (cursor != null && cursor.moveToFirst()) {
                                fileName =
                                    cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                            }
                        }

                        // some basic checks before importing
                        if (fileName != null && !fileName!!.contains("Goodtime-Training") || !FileUtils.isSQLite3File(tempFile)
                        ) {
                            return@executeAsyncTask false
                        }

                        val inStream = FileInputStream(tempFile)
                        val destinationPath =
                            context.getDatabasePath(GoodtimeDatabase.DATABASE_NAME)
                        FileUtils.copy(inStream, destinationPath)

                        // recreate database
                        GoodtimeDatabase.getDatabase(context)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    return@executeAsyncTask true
                },
                onPostExecute = {
                    val success = it
                    Toast.makeText(
                        context,
                        if (success) "Backup import successful" else "Backup import failed",
                        Toast.LENGTH_SHORT
                    ).show()
                })
        }

        fun doExport(lifecycleScope: LifecycleCoroutineScope, context: Context) {
            lifecycleScope.launch(Dispatchers.IO) {
                GoodtimeDatabase.closeInstance()
                val file = context.getDatabasePath(GoodtimeDatabase.DATABASE_NAME)
                val destinationPath = File(context.filesDir, "tmp")
                val destinationFile = File(
                    destinationPath, "Goodtime-Training-Backup-" + StringUtils.getDateAndTimeForBackup()
                )
                destinationFile.deleteOnExit()
                if (file.exists()) {
                    try {
                        FileUtils.copyFile(file, destinationFile)
                        if (destinationFile.exists()) {
                            val fileUri = FileProvider.getUriForFile(
                                context,
                                context.packageName,
                                destinationFile
                            )
                            val intent = Intent()
                            intent.action = Intent.ACTION_SEND
                            intent.type = "application/octet-stream"
                            intent.putExtra(Intent.EXTRA_STREAM, fileUri)
                            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            context.startActivity(
                                Intent.createChooser(
                                    intent,
                                    "Export backup"
                                )
                            )

                            // re-open database
                            GoodtimeDatabase.getDatabase(context)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(
                            context,
                            "Backup export failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}