package com.tvstream.app.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.tvstream.app.R
import com.tvstream.app.databinding.ActivityMainBinding
import com.tvstream.app.utils.DeviceUtils

/**
 * MainActivity – single entry point for both Android TV and Phone.
 *
 * At runtime it detects the device type and loads the appropriate fragment:
 *  - TV  → [HomeBrowseFragment]  (Leanback BrowseSupportFragment, D-pad optimized)
 *  - Phone → [HomePhoneFragment] (RecyclerView grid, touch optimized)
 *
 * This "one APK, two UIs" pattern keeps the distribution footprint small and
 * avoids maintaining two separate app modules.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding – no synthetic imports, no findViewById
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Only add fragment on first creation to avoid double-stacking on rotation
        if (savedInstanceState == null) {
            val fragment = if (DeviceUtils.isTV(this)) {
                HomeBrowseFragment()
            } else {
                HomePhoneFragment()
            }

            supportFragmentManager.commit {
                replace(R.id.fragment_container, fragment)
            }
        }
    }
}
