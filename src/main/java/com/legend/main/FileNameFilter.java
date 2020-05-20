package com.legend.main;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Locale;

/**
 * @author Legend
 * @data by on 20-5-4.
 * @description
 */
public class FileNameFilter extends FileFilter {
    // Description of this filter.
    private final String description;
    // File name
    private String name = "";
    // Known extensions.
    private final String[] extensions;
    // Cached ext
    private final String[] lowerCaseExtensions;

    public FileNameFilter(String description, String... extensions) {
        if (extensions == null || extensions.length == 0) {
            throw new IllegalArgumentException(
                    "Extensions must be non-null and not empty");
        }
        this.description = description;
        this.extensions = new String[extensions.length];
        this.lowerCaseExtensions = new String[extensions.length];
        for (int i = 0; i < extensions.length; i++) {
            if (extensions[i] == null || extensions[i].length() == 0) {
                throw new IllegalArgumentException(
                        "Each extension must be non-null and not empty");
            }
            this.extensions[i] = extensions[i];
            lowerCaseExtensions[i] = extensions[i].toLowerCase(Locale.ENGLISH);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean accept(File f) {
        if (f != null) {
            if (f.isDirectory()) {
                return true;
            }
            // NOTE: we tested implementations using Maps, binary search
            // on a sorted list and this implementation. All implementations
            // provided roughly the same speed, most likely because of
            // overhead associated with java.io.File. Therefor we've stuck
            // with the simple lightweight approach.
            String fileName = f.getName();
            if (!fileName.contains(name)) {
                return false;
            }
            int i = fileName.lastIndexOf('.');
            if (i > 0 && i < fileName.length() - 1) {
                String desiredExtension = fileName.substring(i+1).
                        toLowerCase(Locale.ENGLISH);
                for (String extension : lowerCaseExtensions) {
                    if (desiredExtension.equals(extension)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String getDescription() {
        return description;
    }

    public String[] getExtensions() {
        String[] result = new String[extensions.length];
        System.arraycopy(extensions, 0, result, 0, extensions.length);
        return result;
    }

    public String toString() {
        return super.toString() + "[description=" + getDescription() +
                " extensions=" + java.util.Arrays.asList(getExtensions()) + "]";
    }
}
