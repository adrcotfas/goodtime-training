package goodtime.training.wod.timer.common

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

fun openStorePage(c: Context) {
    val uri = Uri.parse("market://details?id=" + c.packageName)
    val goToMarket = Intent(Intent.ACTION_VIEW, uri)
    goToMarket.addFlags(
        Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK
    )
    try {
        c.startActivity(goToMarket)
    } catch (e: ActivityNotFoundException) {
        c.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=" + c.packageName)
            )
        )
    }
}

fun openStoreAppsList(c: Context) {
    val url = "https://play.google.com/store/apps/developer?id=Goodtime"
    val i = Intent(Intent.ACTION_VIEW)
    i.data = Uri.parse(url)
    c.startActivity(i)
}