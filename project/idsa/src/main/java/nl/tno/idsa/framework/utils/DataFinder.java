package nl.tno.idsa.framework.utils;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Find usable data in the project path.
 */
// TODO For now, we traverse the directory tree upward, until we find a root from which data files are available. ...
// We then immediately stop and return those data files. I.e. no further looking or scanning.
public class DataFinder {

    public static String pickDataSource()
    throws IOException {
        File file = new File(".").getAbsoluteFile();
        ArrayList<File> files = new ArrayList<>();
        while (files.size() == 0) {
            files = DataFinder.listDataRoots(file);
            file = file.getParentFile();
            if (file == null) {
                break;
            }
        }
        if (files.size() == 0) {
            return null;
        }
        file = files.get(0);
        String path = file.getCanonicalPath();
        path = path.substring(0, path.lastIndexOf(File.separator));
        return path;
    }

    private static ArrayList<File> listDataRoots(File root) {
        ArrayList<File> results = new ArrayList<>();
        listDataRoots(root, results);
        return results;
    }

    private static void listDataRoots(File root, List<File> into)  {
        if (root == null) {
            return;
        }
        File[] files = root.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isFile() && file.getName().contains("idsa_data_root")) {
                into.add(file);
            } else if (file.isDirectory()) {
                listDataRoots(file, into);
            }
        }
    }
}
