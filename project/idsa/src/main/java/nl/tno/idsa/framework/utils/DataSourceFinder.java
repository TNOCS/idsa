package nl.tno.idsa.framework.utils;

import nl.tno.idsa.framework.semantics_base.JavaSubclassFinder;
import nl.tno.idsa.framework.world.WorldModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Find usable environment data in the project path. Folders are identified by the presence of a token file
 * entitled "idsa_data_root.txt". This should contain a property 'model' that tells us which WorldModel implementation
 * is needed to parse the data, and a property 'description' that should contain a concise description of the data.
 */
// TODO For now, we traverse the directory tree upward, until we find a root from which data files are available. ...
// We then immediately stop and return those data files. I.e. no further looking or scanning.
public class DataSourceFinder {

    public static List<DataSource> listDataSources()
    throws IOException {
        File file = new File(".").getAbsoluteFile();
        ArrayList<File> files = new ArrayList<>();
        while (files.size() == 0) {
            files = DataSourceFinder.listDataSources(file);
            file = file.getParentFile();
            if (file == null) {
                break;
            }
        }
        List<DataSource> dataSources = new ArrayList<>(files.size());
        if (files.size() > 0)
        {
            Set<Class<? extends WorldModel>> modelClasses = JavaSubclassFinder.listSubclasses(WorldModel.class);
            HashMap<String, Class<? extends WorldModel>> modelClassesByName = new HashMap<>();
            for (Class<? extends WorldModel> modelClass : modelClasses) {
                modelClassesByName.put(modelClass.getSimpleName(), modelClass);
            }
            for (File dataDescriptor: files) {
                Properties p = new Properties();
                p.load(new FileInputStream(dataDescriptor));
                String path = dataDescriptor.getCanonicalPath();
                path = path.substring(0, path.lastIndexOf(File.separator));
                String model = p.getProperty("model");
                Class<? extends WorldModel> modelClass = modelClassesByName.get(model);
                String description = p.getProperty("description");
                try {
                    dataSources.add(new DataSource(path, description, modelClass.newInstance()));
                }
                catch (Exception e) {
                    // Ignore.
                }
            }
        }
        return dataSources;
    }

    private static ArrayList<File> listDataSources(File root) {
        ArrayList<File> results = new ArrayList<>();
        listDataSources(root, results);
        return results;
    }

    private static void listDataSources(File root, List<File> into)  {
        if (root == null) {
            return;
        }
        File[] files = root.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isFile() && file.getName().contains("idsa_data_root.txt")) {
                into.add(file);
            } else if (file.isDirectory()) {
                listDataSources(file, into);
            }
        }
    }

    public static class DataSource {
        private String path;
        private String description;
        private WorldModel model;

        private DataSource(String path, String description, WorldModel model) {
            this.path = path;
            this.description = description;
            this.model = model;
        }

        public String getPath() {
            return path;
        }

        public String getDescription() {
            return description;
        }

        public WorldModel getModel() {
            return model;
        }

        @Override
        public String toString() {
            return description != null ? description : path;
        }
    }
}
