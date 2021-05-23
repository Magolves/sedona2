package sedonac.util;

import java.io.File;

public class FileUtil {
    /**
     * Checks if the target is up-to-date. The target is up-to-date, if the target exists
     * and is younger than the source.
     * @param source the source path
     * @param target the target path
     * @return true, if target is up-to-date; otherwise false
     */
    public static boolean isUpToDate(String source, String target) {
        return isUpToDate(new File(source), new File(target));
    }

    /**
     * Checks if the target file is up-to-date. The target is up-to-date, if the target (file) exists
     * and is younger than the source.
     * @param source the source file
     * @param target the target file
     * @return true, if target is up-to-date; otherwise false
     */
    public static boolean isUpToDate(File source, File target) {
        if (!source.exists()) return true; // can't tell ?

        if (!target.exists()) return false;

        return source.lastModified() <= target.lastModified();
    }
}
