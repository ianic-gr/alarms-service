package gr.ianic.utils;

import java.util.List;

public class SensorGroup {
    public static String getGroup(String sensorName) {
        if (List.of("sensorA", "sensorB", "sensorC").contains(sensorName)) {
            return "GroupA";
        } else if (List.of("sensorD", "sensorE", "sensorG").contains(sensorName)) {
            return "GroupB";
        }
        return "Other";
    }
}