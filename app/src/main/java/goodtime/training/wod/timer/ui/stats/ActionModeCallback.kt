package goodtime.training.wod.timer.ui.stats

import android.view.ActionMode
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import goodtime.training.wod.timer.R

class ActionModeCallback(private val listener: Listener) : ActionMode.Callback {

    interface Listener {
        fun onSelectAllItems()
        fun onDeleteItem()
        fun onCloseActionMode()
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        val inflater: MenuInflater = mode.menuInflater
        inflater.inflate(R.menu.menu_stats_action_mode, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> listener.onDeleteItem()
            R.id.action_select_all -> listener.onSelectAllItems()
        }
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        listener.onCloseActionMode()
    }
}