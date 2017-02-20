/*
   Copyright 2017 Fabian Streitel

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package be.rottenrei.simpletrial;


import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import java.util.Date;

/**
 * Simple implementation of a trial period. The trial starts when the user installs the
 * application (not when they first open the app).
 * <p>
 * Uses both the first install date provided by the package manager as well as an entry in a
 * shared preference file. You should make sure that shared preference file is backed up to
 * ensure the user cannot circumvent the trial restrictions by reinstalling the app!
 * <p>
 * To back up the shared preference, use the
 * {@link android.app.backup.SharedPreferencesBackupHelper} class or enable auto backup for your
 * app.
 */
public class SimpleTrial {

    /**
     * The default shared preference file to cache the trial start timestamp in.
     */
    public static final String DEFAULT_SHARED_PREFERENCES_FILE = "simple_trial";

    /**
     * The default name of the shared preference under which the trial start timestamp will be
     * cached.
     */
    public static final String DEFAULT_PREFERENCE_NAME = "trial_start";

    /**
     * Timestamp to return when the trial start timestamp cannot be determined.
     */
    private static final long NOT_AVAILABLE_TIMESTAMP = -1;

    /**
     * The context.
     */
    private final Context context;

    /**
     * The shared preferences file to cache the start timestamp in.
     */
    private final String sharedPreferencesFile;

    /**
     * The name of the preference under which to cache the start timestamp.
     */
    private final String preferenceName;

    /**
     * The duration of the trial in milliseconds.
     */
    private final long trialDurationInMilliseconds;

    /**
     * In-memory cache of the trial start timestamp.
     */
    private long trialStartTimestamp;

    /**
     * Whether key-value backup is used to persist the trial information across reinstallations.
     */
    private boolean usesKeyValueBackup;

    /**
     * Creates a new simple trial, calculates the trial start timestamp, immediately stores it
     * in the given shared preference.
     * <p>
     * If you are using auto backup, no further action is required.
     * <p>
     * If you'd like to add additional checks, you can use {@link #getTrialStartDate()} and
     * {@link #updateTrialStartDate(Date)}.
     *
     * @param context               the context (application context is enough).
     * @param sharedPreferencesFile the shared preference file in which to cache the trial
     *                              timestamp.
     * @param preferenceName        the name of the preference under which to cache the start
     *                              timestamp.
     * @param trialDurationInDays   the duration of the trial in days.
     * @param usesKeyValueBackup    wether key-value backup is used to persist the trial
     *                              information across reinstallations. Set this to false if you
     *                              either don't want to use any backup mechanism or if you are
     *                              using Auto Backup. If this is true, a backup will be
     *                              requested immediately.
     */
    public SimpleTrial(Context context, String sharedPreferencesFile, String preferenceName,
            long trialDurationInDays, boolean usesKeyValueBackup) {
        this.context = context;
        this.sharedPreferencesFile = sharedPreferencesFile;
        this.preferenceName = preferenceName;
        this.trialDurationInMilliseconds = trialDurationInDays * 24L * 3600 * 1000;
        this.usesKeyValueBackup = usesKeyValueBackup;
        trialStartTimestamp = calculateTrialStartTimestamp();
        persistTrialStartTimestamp();
    }

    /**
     * Creates a new simple trial using the {@link #DEFAULT_SHARED_PREFERENCES_FILE} and
     * {@link #DEFAULT_PREFERENCE_NAME}, calculates the trial start timestamp, immediately
     * stores it in the given shared preference.
     * <p>
     * If you are using auto backup, no further action is required.
     *
     * @param context             the context (application context is enough).
     * @param trialDurationInDays the duration of the trial in days.
     * @param usesKeyValueBackup    wether key-value backup is used to persist the trial
     *                              information across reinstallations. Set this to false if you
     *                              either don't want to use any backup mechanism or if you are
     *                              using Auto Backup. If this is true, a backup will be
     *                              requested immediately.
     */
    public SimpleTrial(Context context, long trialDurationInDays, boolean usesKeyValueBackup) {
        this(context, DEFAULT_SHARED_PREFERENCES_FILE, DEFAULT_PREFERENCE_NAME,
                trialDurationInDays, usesKeyValueBackup);
    }

    /**
     * Returns true if the trial period has ended.
     */
    public boolean isTrialPeriodFinished() {
        return new Date().getTime() >= trialStartTimestamp + trialDurationInMilliseconds;
    }

    /**
     * Returns the start date of the trial.
     */
    public Date getTrialStartDate() {
        return new Date(trialStartTimestamp);
    }

    /**
     * Stores the trial start timestamp in the shared preference.
     */
    private void persistTrialStartTimestamp() {
        SharedPreferences preferences = context
                .getSharedPreferences(sharedPreferencesFile, Context.MODE_PRIVATE);
        preferences.edit().putLong(preferenceName, trialStartTimestamp).apply();

        if (usesKeyValueBackup) {
            new BackupManager(context).dataChanged();
        }
    }

    /**
     * Allows you to manually override the trial start date. The date will be persisted to the
     * shared preferences.
     */
    public void updateTrialStartDate(Date trialStartDate) {
        trialStartTimestamp = trialStartDate.getTime();
        persistTrialStartTimestamp();
    }

    /**
     * Calculates the trial start timestamp.
     * <p>
     * If there is no value in the shared preferences, we use the installation timestamp instead.
     * If both are available, we use the smaller timestamp.
     */
    private long calculateTrialStartTimestamp() {
        long packageManagerTimestamp = getPackageManagerTimestamp();
        long preferencesTimestamp = getPreferencesTimestamp();

        if (preferencesTimestamp == NOT_AVAILABLE_TIMESTAMP) {
            return packageManagerTimestamp;
        }
        return Math.min(packageManagerTimestamp, preferencesTimestamp);
    }

    /**
     * Returns the installation timestamp reported by the package manage.
     */
    private long getPackageManagerTimestamp() {
        try {
            return context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).firstInstallTime;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Your own package " + context.getPackageName() +
                    " is not availbable. This should never happen", e);
        }
    }

    /**
     * Returns the timestamp stored in the shared preference file.
     */
    private long getPreferencesTimestamp() {
        SharedPreferences preferences = context
                .getSharedPreferences(sharedPreferencesFile, Context.MODE_PRIVATE);
        return preferences.getLong(preferenceName, NOT_AVAILABLE_TIMESTAMP);
    }

}
