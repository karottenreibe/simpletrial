package be.rottenrei.simpletrial;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Stores the trial timestamp in a file. In order for this to work, you must set the correct
 * permissions in your Manifest, c.f.
 * <a href="https://developer.android.com/guide/topics/data/data-storage.html#filesExternal">The
 * Android developer documentation</a>.
 * <p>
 * Note that starting with Android Marshmallow (API level 23), you will have to also request
 * runtime permissions as well, if you want to persist data in a non-app-private location. As
 * long as these permissions have not been granted, this factor will do nothing. You must
 * request these permissions yourself from the user in one of your activities, c.f.
 * <a href="http://stackoverflow.com/questions/33162152/storage-permission-error-in-marshmallow">
 * this StackOverflow post</a>.
 */
public class FileTrialFactor extends TrialFactor {

    /**
     * The file in which to persist the timestamp.
     */
    private final File file;

    /**
     * @see FileTrialFactor
     */
    public FileTrialFactor(File file) {
        this.file = file;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void persistTimestamp(long timestamp, Context context) {
        if (Build.VERSION.SDK_INT >= 23 &&
                context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {
            return;
        }

        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(Long.toString(timestamp));
        } catch (IOException e) {
            // ignore
        } finally {
            close(writer);
        }

    }

    /**
     * Closes the given closable, if it's null. Swallows any {@link IOException}s that may be
     * raised.
     */
    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public long readTimestamp(Context context) {
        if (Build.VERSION.SDK_INT >= 23 &&
                context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {
            return NOT_AVAILABLE_TIMESTAMP;
        }

        if (!file.exists()) {
            return NOT_AVAILABLE_TIMESTAMP;
        }

        FileReader reader = null;
        BufferedReader bufferedReader = null;
        try {
            reader = new FileReader(file);
            bufferedReader = new BufferedReader(reader);
            String data = bufferedReader.readLine();
            return Long.parseLong(data);
        } catch (IOException | NumberFormatException e) {
            return NOT_AVAILABLE_TIMESTAMP;
        } finally {
            close(reader);
            close(bufferedReader);
        }
    }
}
