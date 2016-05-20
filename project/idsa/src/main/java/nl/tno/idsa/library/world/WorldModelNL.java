package nl.tno.idsa.library.world;

import nl.tno.idsa.framework.utils.FeatureUtils;
import nl.tno.idsa.framework.world.Area;
import nl.tno.idsa.framework.world.WorldModel;
import nl.tno.idsa.library.locations.*;
import org.opengis.feature.Feature;

public class WorldModelNL extends WorldModel {

    // BAG building fields
    private static final String OBJECT_ID_FIELD = "PAND_ID";
    private static final String BUILDING_FUNCTION_FIELD = "GEBR_DOEL1";
    private static final String SURFACE_FIELD = "OPPERVLAK";

    // OSM building type fields
    private static final String PARK = "park";
    private static final String PLAYGROUND = "playground";
    private static final String SPORT = "sport";
    private static final String SQUARE = "square";
    private static final String WATER = "water";
    private static final String PARKING = "parking";
    private static final String SCHOOL = "school";
    private static final String RETAIL = "retail";
    private static final String INDUSTRIAL = "industrial";
    private static final String HOUSEBOAT = "houseboat";
    private static final String HOUSE = "house";
    private static final String COMMERCIAL = "commercial";
    private static final String APARTMENTS = "apartments";

    @Override
    public long getObjectIdFromField(Feature feature) {
        long result = -1;
        String id = FeatureUtils.getFieldValue(feature, OBJECT_ID_FIELD);
        if (id != null) {
            result = Long.parseLong(id);
        }
        return result;
    }

    @Override
    public String getBuildingTypeFromField(Feature feature) {
        String result = FeatureUtils.getFieldValue(feature, "building"); // TODO Hardcoded property.
        if (result == null || result.isEmpty()) {
            result = FeatureUtils.getFieldValue(feature, "type");        // TODO Hardcoded property.
        }
        return result;
    }

    @Override
    public String getBuildingFunctionFromField(Feature feature) {
        return FeatureUtils.getFieldValue(feature, BUILDING_FUNCTION_FIELD);
    }

    @Override
    public Integer getBuildingSurfaceFromField(Feature feature) {
        Integer result = null;
        Long surface = FeatureUtils.getFieldValue(feature, SURFACE_FIELD);
        if (surface != null) {
            result = surface.intValue();
        }
        return result;
    }

    @Override
    public void addFunctionsBasedOnBuildingFunction(Area area, String function, Integer surface) {
        int surfaceInt = 0;
        if (surface != null) {
            surfaceInt = surface;
        }
        if (function != null) {
            switch (function) {
                case "gezondheidszorgfunctie":                      // TODO Hardcoded property.
                    area.addFunction(new Workplace(150));
                    break;
                case "industriefunctie":                            // TODO Hardcoded property.
                    area.addFunction(new Workplace(250));
                    break;
                case "kantoorfunctie":                               // TODO Hardcoded property.
                    area.addFunction(new Workplace(150));
                    break;
                case "logiesfunctie":                                  // TODO Hardcoded property.
                    area.addFunction(new Workplace(50));
                    break;
                case "onderwijsfunctie":                                 // TODO Hardcoded property.
                    area.addFunction(new School(400));
                    area.addFunction(new Workplace(30));
                    break;
                case "woonfunctie":
                    area.addFunction(new House(getHouseCapacity(surfaceInt)));
                    break;
                case "winkelfunctie":              // TODO Hardcoded property.
                    area.addFunction(new Shop(Integer.MAX_VALUE));
                    area.addFunction(new Workplace(10));
                    break;
                case "bijeenkomstfunctie":    // TODO Hardcoded property.
                    area.addFunction(new Workplace(5));
                    break;
                case "sportfunctie":    // TODO Hardcoded property.
                    area.addFunction(new SportsField(30));
                    area.addFunction(new Workplace(10));
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void addFunctionsBasedOnBuildingType(Area area, String buildingType) {
        int surface = (int) Math.round(area.getSurface());
        if (!area.hasAnyFunction() && buildingType != null) {
            switch (buildingType) {
                case APARTMENTS:           // TODO Hardcoded property.
                    area.addFunction(new House(surface));
                    break;
                case COMMERCIAL:            // TODO Hardcoded property.
                    area.addFunction(new Workplace(50)); // TODO Diversify work places.
                    break;
                case HOUSE:                  // TODO Hardcoded property.
                    area.addFunction(new House(surface));
                    break;
                case HOUSEBOAT:               // TODO Hardcoded property.
                    area.addFunction(new House(surface));
                    break;
                case INDUSTRIAL:                 // TODO Hardcoded property.
                    area.addFunction(new Workplace(250));
                    break;
                case RETAIL:                   // TODO Hardcoded property.
                    area.addFunction(new Shop(Integer.MAX_VALUE));
                    area.addFunction(new Workplace(5));
                    break;
                case SCHOOL:                  // TODO Hardcoded property.
                    area.addFunction(new School(400));
                    area.addFunction(new Workplace(30));
                    break;
                case PARKING:                 // TODO Hardcoded property.
                    area.addFunction(new Square(surface * 2));
                    return;
                case WATER:                  // TODO Hardcoded property.
                    area.addFunction(new Water());
                    break;
                case SQUARE:                     // TODO Hardcoded property.
                    area.addFunction(new Square(surface * 2));
                    break;
                case SPORT:                     // TODO Hardcoded property.
                    area.addFunction(new SportsField(30));
                    break;

                // TODO There are no parks and playgrounds.
                case PLAYGROUND:                   // TODO Hardcoded property.
                    area.addFunction(new Playground(100));
                    break;
                case PARK:                       // TODO Hardcoded property.
                    area.addFunction(new Park(Integer.MAX_VALUE));
                    break;

                default:
                    break;
            }
        }
        // Final resort
        if (!area.hasAnyFunction() && surface > 8) {
            if (surface < 220) {
                // Assumed house
                area.addFunction(new House(surface));
            } else if (surface < 545) {
                // Assumed shop
                area.addFunction(new Shop(Integer.MAX_VALUE));
                area.addFunction(new Workplace(5));
            } else if (surface >= 545) {
                // Assumed office
                area.addFunction(new Workplace(50));
            }
        }
    }

    private int getHouseCapacity(int surface) {
        return (int) Math.ceil(surface / 20.0);
    }
}
