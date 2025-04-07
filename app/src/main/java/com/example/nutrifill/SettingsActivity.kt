package com.example.nutrifill

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.*
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.snackbar.Snackbar

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            // Handle dark mode preference
            setupDarkModePreference()

            // Handle language preference
            setupLanguagePreference()

            // Handle dietary restrictions
            setupDietaryRestrictions()

            // Handle calorie goal
            setupCalorieGoal()

            // Handle notification settings
            setupNotificationPreferences()

            // Handle data and privacy settings
            setupDataPrivacyPreferences()

            // Handle about section
            setupAboutPreferences()
        }

        private fun setupDarkModePreference() {
            findPreference<SwitchPreferenceCompat>("dark_mode")?.setOnPreferenceChangeListener { _, newValue ->
                val darkModeEnabled = newValue as Boolean
                AppCompatDelegate.setDefaultNightMode(
                    if (darkModeEnabled) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
                true
            }
        }

        private fun setupLanguagePreference() {
            findPreference<ListPreference>("language")?.setOnPreferenceChangeListener { _, newValue ->
                // Implement language change logic
                activity?.recreate()
                true
            }
        }

        private fun setupDietaryRestrictions() {
            findPreference<MultiSelectListPreference>("dietary_restrictions")?.setOnPreferenceChangeListener { _, newValue ->
                @Suppress("UNCHECKED_CAST")
                val restrictions = newValue as Set<String>
                // Save dietary restrictions
                requireActivity().getSharedPreferences("NutriFillPrefs", AppCompatActivity.MODE_PRIVATE)
                    .edit()
                    .putStringSet("dietary_restrictions", restrictions)
                    .apply()
                true
            }
        }

        private fun setupCalorieGoal() {
            findPreference<EditTextPreference>("calorie_goal")?.setOnPreferenceChangeListener { _, newValue ->
                val calorieGoal = (newValue as String).toIntOrNull()
                if (calorieGoal != null && calorieGoal > 0) {
                    // Save calorie goal
                    requireActivity().getSharedPreferences("NutriFillPrefs", AppCompatActivity.MODE_PRIVATE)
                        .edit()
                        .putString("calorie_goal", calorieGoal.toString())
                        .apply()
                    true
                } else {
                    Snackbar.make(requireView(), "Please enter a valid calorie goal", Snackbar.LENGTH_SHORT).show()
                    false
                }
            }
        }

        private fun setupNotificationPreferences() {
            findPreference<SwitchPreferenceCompat>("notifications_enabled")?.setOnPreferenceChangeListener { _, newValue ->
                val enabled = newValue as Boolean
                // Update notification settings
                requireActivity().getSharedPreferences("NutriFillPrefs", AppCompatActivity.MODE_PRIVATE)
                    .edit()
                    .putBoolean("notifications_enabled", enabled)
                    .apply()
                true
            }

            findPreference<SeekBarPreference>("reminder_frequency")?.setOnPreferenceChangeListener { _, newValue ->
                val hours = newValue as Int
                // Update reminder frequency
                requireActivity().getSharedPreferences("NutriFillPrefs", AppCompatActivity.MODE_PRIVATE)
                    .edit()
                    .putInt("reminder_frequency", hours)
                    .apply()
                true
            }
        }

        private fun setupDataPrivacyPreferences() {
            findPreference<Preference>("clear_history")?.setOnPreferenceClickListener {
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Clear History")
                    .setMessage("Are you sure you want to clear all nutrition and scanning history?")
                    .setPositiveButton("Yes") { _, _ ->
                        // Clear history
                        requireActivity().getSharedPreferences("NutriFillPrefs", AppCompatActivity.MODE_PRIVATE)
                            .edit()
                            .remove("food_history")
                            .apply()
                        Snackbar.make(requireView(), "History cleared", Snackbar.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("No", null)
                    .show()
                true
            }
        }

        private fun setupAboutPreferences() {
            // Set version number
            findPreference<Preference>("app_version")?.apply {
                val versionName = requireActivity().packageManager
                    .getPackageInfo(requireActivity().packageName, 0).versionName
                summary = "Version $versionName"
            }

            // Handle privacy policy
            findPreference<Preference>("privacy_policy")?.setOnPreferenceClickListener {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://nutrifill.com/privacy")))
                } catch (e: Exception) {
                    Snackbar.make(requireView(), "Unable to open privacy policy", Snackbar.LENGTH_SHORT).show()
                }
                true
            }
        }
    }
}
