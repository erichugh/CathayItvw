package exam.hughwu.cathaytest.feature.home

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import exam.hughwu.cathaytest.R
import exam.hughwu.cathaytest.common.BaseFragment
import exam.hughwu.cathaytest.common.NoEvent
import exam.hughwu.cathaytest.common.NoIntent
import exam.hughwu.cathaytest.common.NoUiState
import exam.hughwu.cathaytest.common.network.NetworkStateManager
import exam.hughwu.cathaytest.extension.applyHorizontalAndBottomInsets
import exam.hughwu.cathaytest.databinding.FragmentHomeBinding
import exam.hughwu.cathaytest.extension.collectIn
import exam.hughwu.cathaytest.extension.setOnSingleClickListener

/**
 * Entry page with three buttons that navigate to the three UI variants of the
 * stock list (XML / Hybrid / Compose) via the Navigation component.
 *
 * Gated by [HomeViewModel.startActionWithNoNetworkHandling]: with a live
 * connection the variant buttons show; offline they are replaced by a prompt
 * that deep-links into the system network settings. The screen reacts to
 * connectivity changes by collecting [NetworkStateManager.networkStateFlow]
 * via [collectIn] — its `repeatOnLifecycle(STARTED)` also re-renders when the
 * fragment returns to the foreground (e.g. back from system Settings).
 */
@AndroidEntryPoint
class HomeFragment :
    BaseFragment<FragmentHomeBinding, NoUiState, NoIntent, NoEvent, HomeViewModel>() {

    override val viewModel: HomeViewModel by viewModels()
    override fun onUiStateChanged(uiState: NoUiState) {
    }

    override fun onUiEvent(uiEvent: NoEvent) {
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding?.let {
            with(it) {
                root.applyHorizontalAndBottomInsets()
                btnXml.setOnSingleClickListener {
                    findNavController().navigate(R.id.action_home_to_xml)
                }
                btnHybrid.setOnSingleClickListener {
                    findNavController().navigate(R.id.action_home_to_hybrid)
                }
                btnCompose.setOnSingleClickListener {
                    findNavController().navigate(R.id.action_home_to_compose)
                }
                btnOpenNetworkSettings.setOnSingleClickListener {
                    startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                }
            }
            it.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Synchronous first paint so there's no flicker before the flow's
        // first emission; the collector below keeps it live afterwards.
        renderByNetworkState()
        NetworkStateManager.networkStateFlow.collectIn(viewLifecycleOwner) {
            renderByNetworkState()
        }
    }

    private fun renderByNetworkState() {
        val b = binding ?: return
        with(b) {
            viewModel.startActionWithNoNetworkHandling(
                networkAction = {
                    contentGroup.visibility = View.VISIBLE
                    noNetworkGroup.visibility = View.GONE
                },
                showNoNetworkMessage = {
                    contentGroup.visibility = View.GONE
                    noNetworkGroup.visibility = View.VISIBLE
                },
            )
        }
    }
}
