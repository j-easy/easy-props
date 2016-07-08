package io.github.benas.easyproperties;

/**
 * Utilities class.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public class Utils {

    /**
     * Extract the path of a file from resource of type file:/path/to/file
     * @param resource the resource path
     * @return the file path
     */
    public static String extractPath(String resource) {
        return resource.substring(resource.lastIndexOf(':') + 1);
    }

    private Utils() {

    }
}
