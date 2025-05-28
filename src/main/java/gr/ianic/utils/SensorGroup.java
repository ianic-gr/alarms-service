package gr.ianic.utils;

import java.util.List;

public class SensorGroup {
    public static String getGroup(String sensorName, String variableName) {
        if (List.of("TSE01B_Μετρητής_παροχής", "TSE03_Μετρητής_παροχής").contains(sensorName + "_" + variableName)) {
            return "Δ1Υ";
        } else if (List.of("P02S_flow", "P02N_flow", "TSE01A_Μετρητής_παροχής_2").contains(sensorName + "_" + variableName)) {
            return "Δ1Χ";
        }
        return "Other";
    }
}