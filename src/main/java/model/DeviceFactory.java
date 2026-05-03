package model;

import java.lang.reflect.Constructor;
import java.util.UUID;

import static lang.ErrorMessages.CLASS_NOT_FOUND;

public class DeviceFactory {

    //kann mit dem gefunden String des DeviceScanner ein richtiges Java Objekt bauen

    private static final String PACKAGE_NAME = "devices";

    //hier wird reflection genutzt
    public static AbstractDevice createDevice(String className, UUID id, String name) {
        try {
            String fullClassName = PACKAGE_NAME + "." + className;

            //sucht die "Bauanleitung" des zu erstellenen Gerätes
            Class<?> clazz = Class.forName(fullClassName);

            //sucht dann nach dem Constructor, der die 3 Parameter hat
            Constructor<?> constructor = clazz.getConstructor(UUID.class, String.class);

            //fürht den Constructor aus und gibt das erstellte Gerät dann zurück
            Object device = constructor.newInstance(id, name);

            /// todo: gliech mal ausprobieren ob hier auch smartdevice geht
            return (AbstractDevice) device;

        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(CLASS_NOT_FOUND + className, e);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while creating the device: " + className, e);
        }

    }
}
