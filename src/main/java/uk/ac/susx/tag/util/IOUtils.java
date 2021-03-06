package uk.ac.susx.tag.util;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.IOException;

/**
 * @author hiam20
 * @since 25/02/2013 11:35
 */
public class IOUtils {

    private IOUtils() {
    }


    /**
     * Check whether or not the given {@code file} is creatable, or already exists.
     *
     * @param file the abstract path to check for creditability.
     * @return true if {@code file} is creatable, false otherwise
     */
    public static boolean isCreatable(final File file) throws IOException {
        Preconditions.checkNotNull(file, "file");
        File f = file.getCanonicalFile().getParentFile();
        while (f != null && !f.exists() && f.isDirectory()) {
            f = f.getParentFile();
        }
        return f != null && f.canWrite();
    }


    public static String combinePath(String part0, String... parts) {
        final StringBuilder builder = new StringBuilder();
        builder.append(part0);
        for (String part : parts) {
            builder.append(File.separatorChar);
            builder.append(part);
        }
        return builder.toString();
    }
}
