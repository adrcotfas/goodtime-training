package goodtime.training.wod.timer.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.databinding.FragmentStatisticsBinding
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance


class StatisticsFragment : Fragment(), KodeinAware {
    override val kodein by closestKodein()

    private val viewModelFactory : LogViewModelFactory by instance()

    private lateinit var binding: FragmentStatisticsBinding
    private lateinit var viewModel : LogViewModel

    private lateinit var recyclerView : RecyclerView

    private val itemClickListener = object: LogAdapter.Listener {
        override fun onClick(position: Int) {
            //TODO: implement this
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        binding = FragmentStatisticsBinding.inflate(layoutInflater, container, false)
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val logAdapter = LogAdapter(itemClickListener)
        recyclerView.adapter = logAdapter

        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, LinearLayout.VERTICAL)
        dividerItemDecoration.setDrawable(resources.getDrawable(R.drawable.recycler_separator))
        recyclerView.addItemDecoration(dividerItemDecoration)

        viewModel = ViewModelProvider(this, viewModelFactory).get(LogViewModel::class.java)
        viewModel.getSessions().observe(viewLifecycleOwner, { sessions ->
            setupEmptyState(sessions.isEmpty())
            logAdapter.data = sessions
        })

        return binding.root
    }

    private fun setupEmptyState(isEmpty: Boolean) {
        binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}
