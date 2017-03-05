package be.rottenrei.simpletrial;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * A {@link TrialFactor} that cannot persist but returns the first installation timestamp
 * maintained by the package manager. This timestamp may be reset by the user by reinstalling the
 * app but remains constant across updates.
 */
public class PackageManagerTrialFactor extends TrialFactor {

    /** {@inheritDoc} */
    @Override
    public long readTimestamp(Context context) {
        try {
            return context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).firstInstallTime;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            return NOT_AVAILABLE_TIMESTAMP;
        }
    }
}
