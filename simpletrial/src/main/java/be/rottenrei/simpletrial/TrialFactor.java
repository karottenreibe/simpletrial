package be.rottenrei.simpletrial;

import android.content.Context;

/**
 * A factor that can determine the trial start date or persist it. Implementations may only
 * provide one of the two.
 */
public abstract class TrialFactor {

    /**
     * Timestamp to return when the trial start timestamp cannot be determined by the factor.
     * This will cause this factor to be ignored when calculating the trial start timestamp but
     * not when persisting the calculated timestamp.
     */
    public static final long NOT_AVAILABLE_TIMESTAMP = Long.MAX_VALUE;

    /**
     * Persists the given timestamp to this factor's backing storage method if it has one. The
     * default implementation is a noop.
     *
     * @param timestamp the timestamp to persist.
     * @param context   the context.
     */
    public void persistTimestamp(long timestamp, Context context) {
        // defaults to a noop
    }

    /**
     * Reads the persisted timestamp from this factor's backing storage method. If no timestamp
     * has been stored there yet or it has been cleared/reset, this method <em>must</em> return
     * {@link #NOT_AVAILABLE_TIMESTAMP}.
     *
     * @param context the context.
     */
    public long readTimestamp(Context context) {
        return NOT_AVAILABLE_TIMESTAMP;
    }

}
