package sedonac.util;

import sedonac.ast.FieldDef;

import java.util.HashMap;

public class SedonaUnits {
    public static final HashMap<String, String> UNIT_MAP = new HashMap<>();

    static {
        SedonaUnits.UNIT_MAP.put("celsius", "°C");
        SedonaUnits.UNIT_MAP.put("kelvin", "K");
        SedonaUnits.UNIT_MAP.put("percent", "%");
        SedonaUnits.UNIT_MAP.put("kilowatt", "kW");
        SedonaUnits.UNIT_MAP.put("kilowatt_hour", "kWh");
        SedonaUnits.UNIT_MAP.put("nanosecond", "ns");
        SedonaUnits.UNIT_MAP.put("millisecond", "ms");
        SedonaUnits.UNIT_MAP.put("second", "s");
        SedonaUnits.UNIT_MAP.put("minute", "min");
        SedonaUnits.UNIT_MAP.put("hour", "h");
        SedonaUnits.UNIT_MAP.put("volt", "V");
        SedonaUnits.UNIT_MAP.put("day", "d");
        SedonaUnits.UNIT_MAP.put("julian_month", "month");
        SedonaUnits.UNIT_MAP.put("degrees_kelvin_per_minute", "K/m");
        SedonaUnits.UNIT_MAP.put("degrees_kelvin_per_second", "K/s");
        SedonaUnits.UNIT_MAP.put("per_minute", "min<sup>-1</sup>");
        SedonaUnits.UNIT_MAP.put("liters_per_minute", "l/min");
        SedonaUnits.UNIT_MAP.put("per_second", "s<sup>-1</sup>");
    }


    private SedonaUnits() {}

    /**
     * Gets the unit symbol for the given field.
     * @param fieldDef the field
     * @return the physical symbol
     */
    public static String getUnitAsString(FieldDef fieldDef) {
        if (fieldDef.facets().isEmpty()) return "";

        String unitId = fieldDef.facets().gets("unit");
        if (unitId == null) {
            return "";
        }

        if (UNIT_MAP.containsKey(unitId)) {
            return UNIT_MAP.get(unitId);
        } else {
            return "(" + unitId + ")";
        }
    }


}
