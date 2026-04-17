package model;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DeviceScanner {

    ///  todo: hier den packageNamen hardcodieren?

    public static List<String> getAllDeviceTypes(String packageName) {
        List<String> deviceTypes = new ArrayList<>();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        String path = packageName.replace('.', '/');
        URL resource = classLoader.getResource(path);

        if (resource != null) {
            File directory = new File(resource.getFile());

            if (directory.exists()) {
                String[] files = directory.list();
                if (files != null) {
                    for (String file : files) {
                        if (file.endsWith(".class")) {
                            String className = file.substring(0, file.length() - 6);


                            /// todo: vllt löschen, wenn im anderen Package liegt?
                            if (!className.equals("Device") &&
                                    !className.equals("SmartDevice") &&
                                    !className.equals("DeviceFunction")) {

                                deviceTypes.add(className);
                            }
                        }
                    }
                }
            }
        } else {
            System.err.println("Package nicht gefunden: " + packageName);
        }

        return deviceTypes;
    }
}