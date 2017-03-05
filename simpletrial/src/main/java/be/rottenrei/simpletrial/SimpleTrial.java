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


import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
     * The default duration of a trial period in days.
     */
    private static final int DEFAULT_TRIAL_DURATION_IN_DAYS = 14;

    /**
     * The context.
     */
    private final Context context;

    /**
     * The duration of the trial in milliseconds.
     */
    private final long trialDurationInMilliseconds;

    /**
     * The trial configuration.
     */
    private final Config config;

    /**
     * In-memory cache of the trial start timestamp.
     */
    private long trialStartTimestamp;

    /**
     * Creates a new simple trial, calculates the trial start timestamp, immediately stores it
     * in the given shared preference.
     * <p>
     * If you are using auto backup, no further action is required.
     * <p>
     * If you'd like to add additional checks, you can use {@link #getTrialStartDate()} and
     * {@link #updateTrialStartDate(Date)}.
     *
     * @param context the context (application context is enough).
     * @param config  the configuration for the trial.
     */
    public SimpleTrial(Context context, Config config) {
        this.context = context;
        this.trialDurationInMilliseconds = config.trialDurationInDays * 24L * 3600 * 1000;
        this.config = config;
        trialStartTimestamp = calculateTrialStartTimestamp();
        persistTrialStartTimestamp();
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
        for (TrialFactor factor : config.factors) {
            factor.persistTimestamp(trialStartTimestamp, context);
        }
    }

    /**
     * Allows you to manually override the trial start date. The date will be persisted to all
     * factors that support persistence.
     */
    public void updateTrialStartDate(Date trialStartDate) {
        trialStartTimestamp = trialStartDate.getTime();
        persistTrialStartTimestamp();
    }

    /**
     * Calculates the trial start timestamp by querying all factors and returning the minimum
     * timestamp. If no factor reports a valid timestamp, falls back to the current time.
     */
    private long calculateTrialStartTimestamp() {
        long timestamp = Long.MAX_VALUE;
        for (TrialFactor factor : config.factors) {
            long factorTimestamp = factor.readTimestamp(context);
            if (factorTimestamp < timestamp) {
                timestamp = factorTimestamp;
            }
        }

        long currentTimestamp = new Date().getTime();
        if (timestamp > currentTimestamp) {
            // fall back to the current time
            return currentTimestamp;
        }
        return timestamp;
    }

    /**
     * Configuration for a {@link SimpleTrial}.
     * <p>
     * If you overwrite nothing, you will get a trial that lasts
     * {@value SimpleTrial#DEFAULT_TRIAL_DURATION_IN_DAYS} days and
     * that uses the
     * {@link PackageManagerTrialFactor}
     * and the {@link SharedPreferencesTrialFactor} with its default configuration.
     */
    public static class Config {

        /**
         * The factors to use.
         */
        private List<TrialFactor> factors = new ArrayList<>();

        /**
         * @see Config
         */
        public Config() {
            factors.add(new PackageManagerTrialFactor());
            factors.add(
                    new SharedPreferencesTrialFactor(new SharedPreferencesTrialFactor.Config()));
        }

        /**
         * The length of the trial period in days.
         */
        private int trialDurationInDays = DEFAULT_TRIAL_DURATION_IN_DAYS;

        /**
         * Changes the factors to use in the trial.
         */
        public void factors(TrialFactor... factors) {
            this.factors.clear();
            addFactors(factors);
        }

        /**
         * Adds an additional factor to use in the trial.
         */
        public void addFactors(TrialFactor... factors) {
            this.factors.addAll(Arrays.asList(factors));
        }

        /**
         * Changes the duration of the trial period.
         */
        public Config trialDurationInDays(int trialDurationInDays) {
            this.trialDurationInDays = trialDurationInDays;
            return this;
        }
    }

}
