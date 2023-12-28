package mrfast.sbf.core;

import com.google.gson.*;
import mrfast.sbf.SkyblockFeatures;

import java.io.*;
import java.lang.annotation.*;
import java.util.HashMap;

public class ConfigManager {
    public static HashMap<String, Object> defaultValues = new HashMap<>();
    public enum PropertyType {
        SLIDER,
        TOGGLE, // switch
        COLOR,
        DROPDOWN, // selector
        BUTTON,
        NUMBER,
        CHECKBOX,
        KEYBIND,
        TEXT // paragraph
        // Add other property types as needed
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Property {
        String name();
        String description() default "";
        String category();
        String subcategory();
        String placeholder() default "";
        boolean hidden() default false;
        int min() default 0;
        int max() default 100;
        boolean isParent() default false;
        String parentName() default "";
        String[] options() default {};
        boolean risky() default false;
        PropertyType type();
    }

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void saveConfig(Object obj) {
        try {
            String json = saveProperties(obj);
            saveToFile(json);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception as needed
        }
    }
    public static void loadConfiguration(Object obj) {
        try {
            String json = loadFromFile();
            loadProperties(obj, json);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception as needed
        }
    }


    private static String saveProperties(Object obj) {
        JsonObject jsonObject = new JsonObject();
        addPropertiesToJson(obj, jsonObject);
        return gson.toJson(jsonObject);
    }

    private static void addPropertiesToJson(Object obj, JsonObject jsonObject) {
        // Use recursion to construct a simplified JSON containing only type, feature name, and value
        // Customize this method based on your specific needs
        // Here, we assume that obj has fields with getter methods
        for (java.lang.reflect.Field field : obj.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object value = field.get(obj);
                Property propertyAnnotation = field.getAnnotation(Property.class);

                if (propertyAnnotation != null) {
                    if(propertyAnnotation.type() == PropertyType.BUTTON) continue;

                    JsonObject propertyJson = new JsonObject();
                    propertyJson.add("value", gson.toJsonTree(value));
                    jsonObject.add(field.getName(), propertyJson);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private static void saveToFile(String json) throws IOException {
        File configFile = new File(SkyblockFeatures.modDir, "config.json");
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write(json);
        }
    }

    private static String loadFromFile() throws IOException {
        File configFile = new File(SkyblockFeatures.modDir, "config.json");
        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            return stringBuilder.toString();
        }
    }

    private static void loadProperties(Object obj, String json) {
        JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
        processJsonProperties(obj, jsonObject);
    }

    private static void processJsonProperties(Object obj, JsonObject jsonObject) {
        for (java.lang.reflect.Field field : obj.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                String fieldName = field.getName();
                if (jsonObject.has(fieldName)) {
                    JsonElement propertyJson = jsonObject.get(fieldName);
                    Property propertyAnnotation = field.getAnnotation(Property.class);
                    defaultValues.put(propertyAnnotation.name(),field.get(obj));
                    Object value = gson.fromJson(propertyJson.getAsJsonObject().get("value"), field.getType());
                    field.set(obj, value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
