package be.rottenrei.simpletrial;

import android.content.Context;

import org.junit.Test;
import org.mockito.ArgumentMatcher;

import java.util.Date;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.longThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SimpleTrialTest {

    @Test
    public void oldTimestampMustCauseTrialToBeOver() {
        Context context = mock(Context.class);
        TrialFactor factor = mock(TrialFactor.class);
        when(factor.readTimestamp(any(Context.class))).thenReturn(123L);

        SimpleTrial trial = new SimpleTrial(context, new SimpleTrial.Config().factors(factor));

        verify(factor).persistTimestamp(123L, context);
        assertTrue(trial.isTrialPeriodFinished());
    }

    @Test
    public void noTimestampMustDefaultToCurrentTimestamp() {
        Context context = mock(Context.class);
        TrialFactor factor = mock(TrialFactor.class);
        when(factor.readTimestamp(any(Context.class)))
                .thenReturn(TrialFactor.NOT_AVAILABLE_TIMESTAMP);

        long minTimestamp = new Date().getTime();
        SimpleTrial trial = new SimpleTrial(context, new SimpleTrial.Config().factors(factor));
        long maxTimestamp = new Date().getTime();

        verify(factor).persistTimestamp(longThat(between(minTimestamp, maxTimestamp)), eq(context));
        assertFalse(trial.isTrialPeriodFinished());
    }

    @Test
    public void availableFactorSupersedesNotAvailableOne() {
        Context context = mock(Context.class);
        TrialFactor factor = mock(TrialFactor.class);
        when(factor.readTimestamp(any(Context.class)))
                .thenReturn(TrialFactor.NOT_AVAILABLE_TIMESTAMP);
        TrialFactor factor2 = mock(TrialFactor.class);
        when(factor2.readTimestamp(any(Context.class))).thenReturn(234L);

        new SimpleTrial(context, new SimpleTrial.Config().factors(factor, factor2));

        verify(factor).persistTimestamp(234L, context);
    }

    @Test
    public void oldestTimestampWins() {
        Context context = mock(Context.class);
        TrialFactor factor = mock(TrialFactor.class);
        when(factor.readTimestamp(any(Context.class))).thenReturn(123L);
        TrialFactor factor2 = mock(TrialFactor.class);
        when(factor2.readTimestamp(any(Context.class))).thenReturn(234L);

        new SimpleTrial(context, new SimpleTrial.Config().factors(factor, factor2));

        verify(factor).persistTimestamp(123L, context);
    }

    /**
     * Matches if the long is non-null and between the two given values (both inclusive).
     */
    private ArgumentMatcher<Long> between(final long minTimestamp, final long maxTimestamp) {
        return new ArgumentMatcher<Long>() {
            @Override
            public boolean matches(Long argument) {
                return argument != null && argument <= maxTimestamp && argument >= minTimestamp;
            }

            @Override
            public String toString() {
                return "[between " + minTimestamp + " and " + maxTimestamp + "]";
            }
        };
    }

}