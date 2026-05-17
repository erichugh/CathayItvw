package exam.hughwu.cathaytest.common

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * Pads this view with the left / right / bottom **system-bar + display-cutout**
 * insets.
 *
 * The app runs edge-to-edge ([androidx.activity.enableEdgeToEdge] in
 * `MainActivity`), so in landscape the navigation bar and the camera cutout
 * sit on the side; without this the toolbar title and list get clipped under
 * them. The **top** inset is intentionally left untouched so the
 * `AppBarLayout` (`fitsSystemWindows`) keeps drawing its black status-bar
 * scrim. Insets are returned unconsumed so the AppBarLayout still receives the
 * top inset.
 */
fun View.applyHorizontalAndBottomInsets() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or
                WindowInsetsCompat.Type.displayCutout(),
        )
        v.updatePadding(left = bars.left, right = bars.right, bottom = bars.bottom)
        insets
    }
}
