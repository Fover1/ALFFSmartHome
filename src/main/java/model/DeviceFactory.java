package model;

import interfaces.SmartDevice;

import java.lang.reflect.Constructor;
import java.util.UUID;

import static lang.ErrorMessages.CLASS_NOT_FOUND;
import static lang.ErrorMessages.ERROR_CREATING_DEVICE;

//Kann mit dem gefunden String des DeviceScanner ein richtiges Java Objekt bauen
public class DeviceFactory {
    private static final String PACKAGE_NAME = "devices";

    //hier wird reflection genutzt
    public static SmartDevice createDevice(String className, UUID id, String name) {
        try {
            String fullClassName = PACKAGE_NAME + "." + className;

            //sucht die "Bauanleitung" des zu erstellenden Geraetes
            Class<?> clazz = Class.forName(fullClassName);

            //sucht dann nach dem Constructor, der die 2 Parameter hat
            Constructor<?> constructor = clazz.getConstructor(UUID.class, String.class);

            //fuehrt den Constructor aus und gibt das erstellte Geraet dann zurueck
            Object device = constructor.newInstance(id, name);

            return (SmartDevice) device;

        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(CLASS_NOT_FOUND + className, e);
        } catch (Exception e) {
            throw new RuntimeException(ERROR_CREATING_DEVICE + className, e);
        }

    }
}
