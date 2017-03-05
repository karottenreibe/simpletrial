package be.rottenrei.simpletrial;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Stores and reads the trial timestamp from a shared preferences file and optionally triggers a
 * backup via the {@link BackupManager} after updating the timestamp. Shared preference files are
 * lost when the user clears the app's cache or reinstall. They are, however, restored
 * automatically upon a reinstallation if they were previously backed up with the
 * {@link BackupManager}. Reinstalling and then clearing the cache is then the only way to
 * permanently remove the value stored in the shared preference file.
 * <p>
 * Please note that in order for the backup to work, you will either have to enable Android
 * AutoBackup or create a {@link android.app.backup.BackupAgentHelper}. C.f. <a
 * href="https://developer.android.com/guide/topics/data/backup.html">the Android
 * developer documentation</a>
 */
public class SharedPreferencesTrialFactor extends TrialFactor {

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
     * The configuration for this factor.
     */
    private final Config config;

    /**
     * @see SharedPreferencesTrialFactor
     */
    public SharedPreferencesTrialFactor(Config config) {
        this.config = config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void persistTimestamp(long timestamp, Context context) {
        SharedPreferences preferences = context
                .getSharedPreferences(config.preferenceFile, Context.MODE_PRIVATE);
        preferences.edit().putLong(config.preferenceName, timestamp).apply();

        if (config.shouldTriggerBackup) {
            new BackupManager(context).dataChanged();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long readTimestamp(Context context) {
        SharedPreferences preferences = context
                .getSharedPreferences(config.preferenceFile, Context.MODE_PRIVATE);
        return preferences.getLong(config.preferenceName, NOT_AVAILABLE_TIMESTAMP);
    }

    /**
     * Configuration for the {@link SharedPreferencesTrialFactor}. If you don't change any of the
     * defaults, the timestamp will be stored in a shared preference file named {@value
     * #DEFAULT_SHARED_PREFERENCES_FILE} in a preference named {@value #DEFAULT_PREFERENCE_NAME}
     * and a backup will be triggered when persisting the timestamp.
     */
    public static class Config {

        /**
         * The shared preferences file to cache the start timestamp in.
         */
        private String preferenceFile = DEFAULT_SHARED_PREFERENCES_FILE;

        /**
         * The name of the preference under which to cache the start timestamp.
         */
        private String preferenceName = DEFAULT_PREFERENCE_NAME;

        /**
         * Whether the {@link BackupManager} should be triggered after updating the trial start
         * timestamp. This helps in persisting the timestamp cached in the shared preference
         * across reinstallations of the app.
         */
        private boolean shouldTriggerBackup = true;

        /**
         * Changes the name of the shared preference file in which the timestamp will be persisted.
         */
        public Config preferenceFile(String preferenceFile) {
            this.preferenceFile = preferenceFile;
            return this;
        }

        /**
         * Changes the name of the preference under which the timestamp will be persisted.
         */
        public Config preferenceName(String preferenceName) {
            this.preferenceName = preferenceName;
            return this;
        }

        /**
         * Changes whether the {@link BackupManager} should be invoked after persisting the
         * timestamp.
         */
        public Config shouldTriggerBackup(boolean shouldTriggerBackup) {
            this.shouldTriggerBackup = shouldTriggerBackup;
            return this;
        }
    }
}
