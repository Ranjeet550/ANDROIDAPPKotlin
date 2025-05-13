package com.constructionmanagement.app

import android.os.Bundle
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // Customize bottom navigation icons
        adjustBottomNavigationIconSize(navView)

        // Wait for the fragment to be attached
        navView.post {
            val navController = findNavController(R.id.nav_host_fragment)

            // Set up the bottom navigation with the nav controller
            val appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.navigation_dashboard,
                    R.id.navigation_workers,
                    R.id.navigation_sites,
                    R.id.navigation_payments,
                    R.id.navigation_reports
                )
            )

            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)
        }
    }

    /**
     * Adjusts the size of the icons in the bottom navigation view
     */
    private fun adjustBottomNavigationIconSize(bottomNavigationView: BottomNavigationView) {
        val menuView = bottomNavigationView.getChildAt(0) as BottomNavigationMenuView
        val iconSizeInDp = 30 // Icon size in dp
        val iconSizeInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            iconSizeInDp.toFloat(),
            resources.displayMetrics
        ).toInt()

        for (i in 0 until menuView.childCount) {
            val item = menuView.getChildAt(i)
            val iconView = item.findViewById<ImageView>(com.google.android.material.R.id.navigation_bar_item_icon_view)
            val layoutParams = iconView.layoutParams
            layoutParams.width = iconSizeInPx
            layoutParams.height = iconSizeInPx
            iconView.layoutParams = layoutParams
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
