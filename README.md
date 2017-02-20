# simpletrial

Simple Android library to facilitate a trial period.

_License:_ Apache 2

## Problem

Many developers want to give their users a grace-period after installing their app to test out premium features.
After that period these features have to be bought. If the app cannot associate some account with the user, permanently
storing their trial status is hard (c.f. [this StackOverflow question][1]).

## Solution

There are many different indicators that can be used to facilitate a trial period, but, as outlined in
[this StackOverflow post][2], the least intrusive for the user are [PackageManager.firstInstallTime][3]
and shared preferences, as they requrie no special permissions.

Combined with either [Auto Backup or key-value backup][4], the user has to reinstall the app and subsequently clear
its cache to reset the trial. This is sufficient work to deter the vast majority of users.

## Backup

simpletrial requires that you backup its shared preference file to the cloud so it is restored when
the user reinstalls the application. You have two options:

- Enable [Auto Backup][5]. It will automatically back up all shared preference files. This is
  only available starting with Android 6.
- Use [key-value backup][6]. This also works with lower Android versions but requires you to write
  some code.

To use key-value backup, you must
add a `BackupAgentHelper` to your project and register a `SharedPreferencesBackupHelper`
[as described in the Android developer documentation][7] to back up
the shared preference file used by simplebackup. simplebackup will take care itself to request a
backup from the `BackupManager` when necessary.

## Combining with other factors

simpletrial is designed to let you add additional factors like a file stored in the user's external
storage. You can simply query and update the trial start date stored in the
shared preferences file.

The usual procedure would be:

- Create the `SimpleTrial` object
- Read the trial start date from your additional factor
- Compare the date obtained from `SimpleTrial` with your factor's date
- Update the simple trial and your factor to both store the minimum of both dates

[1]: http://stackoverflow.com/q/995719/1396068
[2]: http://stackoverflow.com/a/42321380/1396068
[3]: https://developer.android.com/reference/android/content/pm/PackageInfo.html#firstInstallTime
[4]: https://developer.android.com/guide/topics/data/backup.html
[5]: https://developer.android.com/guide/topics/data/autobackup.html
[6]: https://developer.android.com/guide/topics/data/keyvaluebackup.html
[7]: https://developer.android.com/guide/topics/data/keyvaluebackup.html#SharedPreferences