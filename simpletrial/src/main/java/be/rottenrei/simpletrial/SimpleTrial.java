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

public class SimpleTrial {

    public static final String DEFAULT_SHARED_PREFERENCES_FILE = "trial_repo";
    public static final String DEFAULT_PREFERENCE_NAME = "trial_start";

    private static final long NOT_AVAILABLE_TIMESTAMP = -1;

    private final Context context;
    private final String sharedPreferencesFile;
    private final String preferenceName;
    private final long trialDurationInMilliseconds;

    private long trialStartTimestamp;

    public SimpleTrial(Context context, String sharedPreferencesFile, String preferenceName,
            long trialDurationInDays) {
        this.context = context;
        this.sharedPreferencesFile = sharedPreferencesFile;
        this.preferenceName = preferenceName;
        this.trialDurationInMilliseconds = trialDurationInDays * 24L * 3600 * 1000;
        trialStartTimestamp = getTrialStartTimestamp();
        persistTrialStartTimestamp();
        BackupManager backupManager = new BackupManager(context);
        backupManager.dataChanged();
    }

    public SimpleTrial(Context context, long trialDurationInDays) {
        this(context, DEFAULT_SHARED_PREFERENCES_FILE, DEFAULT_PREFERENCE_NAME,
                trialDurationInDays);
    }

    public boolean isTrialPeriodFinished() {
        return new Date().getTime() >= trialStartTimestamp + trialDurationInMilliseconds;
    }

    private void persistTrialStartTimestamp() {
        SharedPreferences preferences = context
                .getSharedPreferences(sharedPreferencesFile, Context.MODE_PRIVATE);
        preferences.edit().putLong(preferenceName, trialStartTimestamp).apply();
    }

    private long getTrialStartTimestamp() {
        long packageManagerTimestamp = getPackageManagerTimestamp();
        long preferencesTimestamp = getPreferencesTimestamp();

        if (preferencesTimestamp == NOT_AVAILABLE_TIMESTAMP) {
            return packageManagerTimestamp;
        }
        return Math.min(packageManagerTimestamp, preferencesTimestamp);
    }

    private long getPackageManagerTimestamp() {
        try {
            return context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).firstInstallTime;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Your own package " + context.getPackageName() +
                    " is not availbable. This should never happen", e);
        }
    }

    private long getPreferencesTimestamp() {
        SharedPreferences preferences = context
                .getSharedPreferences(sharedPreferencesFile, Context.MODE_PRIVATE);
        return preferences.getLong(preferenceName, NOT_AVAILABLE_TIMESTAMP);
    }

}
