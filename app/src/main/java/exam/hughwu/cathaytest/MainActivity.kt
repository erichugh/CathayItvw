package exam.hughwu.cathaytest

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity Navigation host.
 *
 * `activity_main.xml` hosts a `NavHostFragment` (`app:defaultNavHost="true"`)
 * driving `res/navigation/nav_graph.xml`: `HomeFragment` (start) → the three
 * stock-list variants (XML / Hybrid / Compose). `defaultNavHost` wires the
 * system back button to the nav back stack, so no manual transaction is needed.
 *
 * There is no Activity-level ActionBar; each destination carries its own
 * toolbar, so NavigationUI / AppBarConfiguration is intentionally not used.
 *
 * Status bar is forced to a black scrim with white icons in both light and
 * dark themes via [SystemBarStyle.dark]. `android:statusBarColor` is ignored
 * on SDK 35+ under enforced edge-to-edge so [enableEdgeToEdge] is the
 * supported path.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // `enableEdgeToEdge` must run before super.onCreate / setContentView for the
        // window flags to apply during the first frame.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.BLACK),
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
