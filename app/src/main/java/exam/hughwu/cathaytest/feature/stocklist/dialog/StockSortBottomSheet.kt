package exam.hughwu.cathaytest.feature.stocklist.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import exam.hughwu.cathaytest.databinding.BottomSheetStockSortBinding
import exam.hughwu.cathaytest.feature.stocklist.StockListUiState.SortOrder

/**
 * Sort selector shown when the toolbar filter icon is tapped.
 */
class StockSortBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetStockSortBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = BottomSheetStockSortBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val current = arguments?.getString(ARG_CURRENT_ORDER)
            ?.let { runCatching { SortOrder.valueOf(it) }.getOrNull() }
            ?: SortOrder.CodeDesc

        binding.optionCodeDesc.icon =
            if (current == SortOrder.CodeDesc) binding.optionCodeDesc.icon else null
        binding.optionCodeAsc.icon =
            if (current == SortOrder.CodeAsc) binding.optionCodeAsc.icon else null

        binding.optionCodeDesc.setOnClickListener { dispatch(SortOrder.CodeDesc) }
        binding.optionCodeAsc.setOnClickListener { dispatch(SortOrder.CodeAsc) }
    }

    private fun dispatch(order: SortOrder) {
        setFragmentResult(
            REQUEST_KEY,
            Bundle().apply { putString(RESULT_SORT_ORDER, order.name) },
        )
        dismiss()
    }

    override fun onStart() {
        super.onStart()
        // Default behaviour rules requested by spec:
        //  - When content fits the screen (typical portrait): open fully expanded.
        //  - When content overflows (e.g. landscape with little vertical space):
        //    keep the rounded top corners and let the user scroll inside the sheet
        //    instead of letting Material strip the corners on full expansion.
        val dialog = dialog as? BottomSheetDialog ?: return
        val sheet = dialog.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet,
        ) ?: return
        BottomSheetBehavior.from(sheet).apply {
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_EXPANDED
            isFitToContents = true
            // Material 1.10+: keep our 10dp top corners even when the sheet reaches
            // the top of the screen (overflow case).
            isShouldRemoveExpandedCorners = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val REQUEST_KEY = "StockSortBottomSheet.requestKey"
        const val RESULT_SORT_ORDER = "sortOrder"

        private const val ARG_CURRENT_ORDER = "currentOrder"

        fun newInstance(current: SortOrder): StockSortBottomSheet =
            StockSortBottomSheet().apply {
                arguments = Bundle().apply { putString(ARG_CURRENT_ORDER, current.name) }
            }
    }
}
