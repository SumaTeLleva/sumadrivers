package mx.suma.drivers.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import mx.suma.drivers.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_screen, rootKey)
    }

    override fun onResume() {
        super.onResume()

        // TODO: use logEvent
        /*Firebase.analytics.setCurrentScreen(
            requireActivity(), "Settings", SettingsFragment::class.java.toString())*/
    }
}
