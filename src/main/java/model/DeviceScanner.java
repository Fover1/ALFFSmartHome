package model;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static lang.ErrorMessages.ERROR_SCANNING_INTERNAL_PACKAGE;

public class DeviceScanner {

    public static List<String> getAllDeviceTypes(String packageName) {
        List<String> deviceTypes = new ArrayList<>();

        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(path);

        if (resource != null) {
            try {
                URI uri = resource.toURI();

                if ("jar".equals(uri.getScheme())) {
                    try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
                        Path jarPath = fileSystem.getPath(path);
                        scanPathForClasses(jarPath, deviceTypes);
                    }
                } else {
                    Path localPath = Paths.get(uri);
                    scanPathForClasses(localPath, deviceTypes);
                }
            } catch (Exception e) {
                System.err.println(ERROR_SCANNING_INTERNAL_PACKAGE + e.getMessage());
            }
        }
        File pluginFolder = new File("devices");

        File[] externalFiles = pluginFolder.listFiles((dir, name) -> name.endsWith(".class"));
        if (externalFiles != null) {
            for (File file : externalFiles) {
                String className = file.getName().replace(".class", "");
                //Fuer jeden Eventlistener erstellt Java eine weitere .class Datei (mit $). Um die "richtige" Klasse zu finden, braucht man diese weitere Unterschiedung
                if (!className.contains("$") && !deviceTypes.contains(className)) {
                    deviceTypes.add(className);
                }
            }
        }
        return deviceTypes;
    }

    private static void scanPathForClasses(Path path, List<String> deviceTypes) throws IOException {
        if (Files.exists(path)) {
            try (Stream<Path> walk = Files.walk(path, 1)) {
                walk.forEach(p -> {
                    String fileName = p.getFileName().toString();
                    if (fileName.endsWith(".class") && !fileName.contains("$")) {
                        String className = fileName.substring(0, fileName.length() - 6);
                        if (!deviceTypes.contains(className)) {
                            deviceTypes.add(className);
                        }
                    }
                });
            }
        }
    }
}