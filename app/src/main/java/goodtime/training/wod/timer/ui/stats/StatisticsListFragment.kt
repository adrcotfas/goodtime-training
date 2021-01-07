package goodtime.training.wod.timer.ui.stats

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.databinding.FragmentStatisticsListBinding
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class StatisticsListFragment : Fragment(), KodeinAware {

    override val kodein by closestKodein()
    private lateinit var binding: FragmentStatisticsListBinding

    private val viewModelFactory : LogViewModelFactory by instance()
    private lateinit var viewModel: StatisticsViewModel

    private val itemClickListener = object: StatisticsAdapter.Listener {
        override fun onClick(position: Int) {
            //TODO: implement this
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireParentFragment(), viewModelFactory).get(StatisticsViewModel::class.java)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatisticsListBinding.inflate(inflater, container, false)

        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val logAdapter = StatisticsAdapter(itemClickListener)
        recyclerView.adapter = logAdapter

        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, LinearLayout.VERTICAL)
        dividerItemDecoration.setDrawable(resources.getDrawable(R.drawable.recycler_separator))
        recyclerView.addItemDecoration(dividerItemDecoration)

        viewModel = ViewModelProvider(this, viewModelFactory).get(StatisticsViewModel::class.java)
        viewModel.getSessions().observe(viewLifecycleOwner, { sessions ->
            logAdapter.data = sessions
            binding.recyclerView.isVisible = sessions.isNotEmpty()
            binding.emptyState.isVisible= sessions.isEmpty()
        })

        return binding.root
    }
}
