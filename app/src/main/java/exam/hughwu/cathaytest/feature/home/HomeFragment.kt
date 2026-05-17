package exam.hughwu.cathaytest.feature.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import exam.hughwu.cathaytest.R
import exam.hughwu.cathaytest.databinding.FragmentHomeBinding

/**
 * Entry page with three buttons that navigate to the three UI variants of the
 * stock list (XML / Hybrid / Compose) via the Navigation component.
 */
class HomeFragment : Fragment() {

    private var binding: FragmentHomeBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val b = binding ?: return
        b.btnXml.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_xml)
        }
        b.btnHybrid.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_hybrid)
        }
        b.btnCompose.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_compose)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
