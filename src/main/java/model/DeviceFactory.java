package model;

import java.lang.reflect.Constructor;

import static lang.ErrorMessages.CLASS_NOT_FOUND;

public class DeviceFactory {

    private static final String PACKAGE_NAME = "devices";

    public static AbstractDevice createDevice(String className, String id, String name, Room room) {
        try {
            String fullClassName = PACKAGE_NAME + "." + className;
            Class<?> clazz = Class.forName(fullClassName);

            Constructor<?> constructor = clazz.getConstructor(String.class, String.class, Room.class);

            Object device = constructor.newInstance(id, name, room);
            return (AbstractDevice) device;

        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(CLASS_NOT_FOUND + className, e);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while creating the device: " + className, e);
        }

    }
}
