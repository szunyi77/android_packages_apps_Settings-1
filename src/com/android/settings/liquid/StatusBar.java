/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.liquid;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.PhoneConstants;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.util.CMDProcessor;
import com.android.settings.SettingsPreferenceFragment;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusBar extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener {

    private static final String TAG = "StatusBar";

    private static final String STATUS_BAR_SIGNAL = "status_bar_signal";
    private static final String STATUS_BAR_NOTIFICATION_COUNT = "status_bar_notification_count";
    private static final String STATUS_BAR_CATEGORY_GENERAL = "status_bar_general";
    private static final String STATUS_BAR_BRIGHTNESS_CONTROL = "status_bar_brightness_control";
    private static final String KEY_MISSED_CALL_BREATH = "missed_call_breath";
    private static final String KEY_MMS_BREATH = "mms_breath";
    private static final String STATUS_BAR_NOTIF_ICON_OPACITY = "status_bar_icon_opacity";
    private static final String STATUS_BAR_AUTO_HIDE = "status_bar_auto_hide";
    private static final String STATUS_BAR_QUICK_PEEK = "status_bar_quick_peek";
    private static final String STATUS_ICON_COLOR_BEHAVIOR = "status_icon_color_behavior";
    private static final String STATUS_ICON_COLOR = "status_icon_color";
    private static final String STATUS_BAR_NETWORK_STATS = "status_bar_show_network_stats";
    private static final String STATUS_BAR_NETWORK_STATS_UPDATE = "status_bar_network_status_update";
    private static final String KEY_SHOW_LTE_OR_FOURGEE = "show_lte_or_fourgee";

    private StatusBarBrightnessChangedObserver mStatusBarBrightnessChangedObserver;

    private ListPreference mStatusBarCmSignal;
    private CheckBoxPreference mStatusBarNotifCount;
    private PreferenceScreen mClockStyle;
    private PreferenceCategory mPrefCategoryGeneral;
    private CheckBoxPreference mStatusBarBrightnessControl;
    private CheckBoxPreference mMissedCallBreath;
    private CheckBoxPreference mMMSBreath;
    private ListPreference mStatusBarIconOpacity;
    private ListPreference mStatusBarAutoHide;
    private CheckBoxPreference mStatusBarQuickPeek;
    private CheckBoxPreference mStatusIconBehavior;
    private ColorPickerPreference mIconColor;
    private ListPreference mStatusBarNetStatsUpdate;
    private CheckBoxPreference mStatusBarNetworkStats;
    private CheckBoxPreference mShowLTEorFourGee;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.status_bar_general);

        PreferenceScreen prefSet = getPreferenceScreen();

        mStatusBarCmSignal = (ListPreference) prefSet.findPreference(STATUS_BAR_SIGNAL);
        int signalStyle = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.STATUS_BAR_SIGNAL_TEXT, 0);
        mStatusBarCmSignal.setValue(String.valueOf(signalStyle));
        mStatusBarCmSignal.setSummary(mStatusBarCmSignal.getEntry());
        mStatusBarCmSignal.setOnPreferenceChangeListener(this);

        mMissedCallBreath = (CheckBoxPreference) findPreference(KEY_MISSED_CALL_BREATH);
        mMissedCallBreath.setChecked(Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.MISSED_CALL_BREATH, 0) == 1);

        mMMSBreath = (CheckBoxPreference) findPreference(KEY_MMS_BREATH);
        mMMSBreath.setChecked(Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.MMS_BREATH, 0) == 1);

        mStatusBarBrightnessControl = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_BRIGHTNESS_CONTROL);
        mStatusBarBrightnessControl.setChecked((Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                            Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL, 0) == 1));
        mStatusBarBrightnessControl.setOnPreferenceChangeListener(this);

        mStatusBarIconOpacity = (ListPreference) prefSet.findPreference(STATUS_BAR_NOTIF_ICON_OPACITY);
        int iconOpacity = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.STATUS_BAR_NOTIF_ICON_OPACITY, 140);
        mStatusBarIconOpacity.setValue(String.valueOf(iconOpacity));
        mStatusBarIconOpacity.setOnPreferenceChangeListener(this);

        mStatusBarBrightnessChangedObserver = new StatusBarBrightnessChangedObserver(new Handler());
        mStatusBarBrightnessChangedObserver.startObserving();

        mStatusBarNotifCount = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_NOTIFICATION_COUNT);
        mStatusBarNotifCount.setChecked((Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.STATUS_BAR_NOTIFICATION_COUNT, 0) == 1));

        mStatusBarAutoHide = (ListPreference) prefSet.findPreference(STATUS_BAR_AUTO_HIDE);
        int statusBarAutoHideValue = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.AUTO_HIDE_STATUSBAR, 0);
        mStatusBarAutoHide.setValue(String.valueOf(statusBarAutoHideValue));
        updateStatusBarAutoHideSummary(statusBarAutoHideValue);
        mStatusBarAutoHide.setOnPreferenceChangeListener(this);

        mStatusBarQuickPeek = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_QUICK_PEEK);
        mStatusBarQuickPeek.setChecked((Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.STATUSBAR_PEEK, 0) == 1));
        mStatusBarQuickPeek.setOnPreferenceChangeListener(this);

        mStatusIconBehavior = (CheckBoxPreference) prefSet.findPreference(STATUS_ICON_COLOR_BEHAVIOR);
        mStatusIconBehavior.setChecked(Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.ICON_COLOR_BEHAVIOR, 0) == 1);

        mStatusBarNetworkStats = (CheckBoxPreference) findPreference(STATUS_BAR_NETWORK_STATS);
        mStatusBarNetworkStats.setChecked(Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.STATUS_BAR_NETWORK_STATS, 0) == 1);

        mIconColor = (ColorPickerPreference) findPreference(STATUS_ICON_COLOR);
        mIconColor.setOnPreferenceChangeListener(this);

        mPrefCategoryGeneral = (PreferenceCategory) findPreference(STATUS_BAR_CATEGORY_GENERAL);

        if (Utils.isWifiOnly(getActivity())) {
            mPrefCategoryGeneral.removePreference(mStatusBarCmSignal);
        }

        if (Utils.isTablet(getActivity())) {
            mPrefCategoryGeneral.removePreference(mStatusBarBrightnessControl);
        }

        mClockStyle = (PreferenceScreen) prefSet.findPreference("clock_style_pref");
        if (mClockStyle != null) {
            updateClockStyleDescription();
        }

        mShowLTEorFourGee = (CheckBoxPreference) findPreference(KEY_SHOW_LTE_OR_FOURGEE);
        mShowLTEorFourGee.setChecked(Settings.System.getInt(getActivity().
                getApplicationContext().getContentResolver(),
                    Settings.System.SHOW_LTE_OR_FOURGEE, 0) == 1);
        if (!deviceSupportsLTE()) {
            getPreferenceScreen().removePreference(mShowLTEorFourGee);
        }

        updateStatusBarBrightnessControl();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean result = false;

        if (preference == mStatusBarCmSignal) {
            int signalStyle = Integer.valueOf((String) newValue);
            int index = mStatusBarCmSignal.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_SIGNAL_TEXT, signalStyle);
            mStatusBarCmSignal.setSummary(mStatusBarCmSignal.getEntries()[index]);
            return true;
        } else if (preference == mStatusBarBrightnessControl) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL,
                    (Boolean) newValue ? 1 : 0);
            return true;
        } else if (preference == mStatusBarQuickPeek) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUSBAR_PEEK,
                    (Boolean) newValue ? 1 : 0);
            return true;
        } else if (preference == mStatusBarIconOpacity) {
            int iconOpacity = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_NOTIF_ICON_OPACITY, iconOpacity);
        } else if (preference == mStatusBarAutoHide) {
            int statusBarAutoHideValue = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.AUTO_HIDE_STATUSBAR, statusBarAutoHideValue);
            updateStatusBarAutoHideSummary(statusBarAutoHideValue);
            return true;
        } else if (preference == mIconColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_ICON_COLOR, intHex);
            CMDProcessor.restartSystemUI();
            return true;
        }

        return false;
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;

        if (preference == mStatusBarNotifCount) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_NOTIFICATION_COUNT,
                    mStatusBarNotifCount.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mMissedCallBreath) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.MISSED_CALL_BREATH,
                    mMissedCallBreath.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mMMSBreath) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.MMS_BREATH,
                    mMMSBreath.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mStatusBarNetworkStats) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_NETWORK_STATS,
                    ((CheckBoxPreference)preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mShowLTEorFourGee) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.SHOW_LTE_OR_FOURGEE,
                    mShowLTEorFourGee.isChecked() ? 1 : 0);
            CMDProcessor.restartSystemUI();
            return true;
        } else if (preference == mStatusIconBehavior) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.ICON_COLOR_BEHAVIOR,
                    mStatusIconBehavior.isChecked() ? 1 : 0);
            CMDProcessor.restartSystemUI();
            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void updateClockStyleDescription() {
        if (Settings.System.getInt(getActivity().getContentResolver(),
               Settings.System.STATUS_BAR_CLOCK, 1) == 1) {
            mClockStyle.setSummary(getString(R.string.clock_enabled));
        } else {
            mClockStyle.setSummary(getString(R.string.clock_disabled));
        }
    }

    private void updateStatusBarAutoHideSummary(int value) {
        if (value == 0) {
            /* StatusBar AutoHide deactivated */
            mStatusBarAutoHide.setSummary(getResources().getString(R.string.auto_hide_statusbar_off));
        } else {
            mStatusBarAutoHide.setSummary(getResources().getString(value == 1
                    ? R.string.auto_hide_statusbar_summary_nonperm
                    : R.string.auto_hide_statusbar_summary_all));
        }
    }

    private void updateStatusBarBrightnessControl() {
        int mode;
        try {
            if (mStatusBarBrightnessControl != null) {
                mode = Settings.System.getIntForUser(mContext.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

                if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                    mStatusBarBrightnessControl.setEnabled(false);
                    mStatusBarBrightnessControl.setSummary(R.string.status_bar_toggle_info);
                } else {
                    mStatusBarBrightnessControl.setEnabled(true);
                    mStatusBarBrightnessControl.setSummary(R.string.status_bar_toggle_brightness_summary);
                }
            }
        } catch (SettingNotFoundException e) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateClockStyleDescription();
        updateStatusBarBrightnessControl();
    }

    private class StatusBarBrightnessChangedObserver extends ContentObserver {
        public StatusBarBrightnessChangedObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            updateStatusBarBrightnessControl();
        }

        public void startObserving() {
            final ContentResolver cr = getActivity().getApplicationContext().getContentResolver();
            cr.registerContentObserver(
                    Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE),
                    false, this);
        }
    }
}
