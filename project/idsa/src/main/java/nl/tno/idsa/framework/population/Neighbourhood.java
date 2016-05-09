package nl.tno.idsa.framework.population;

import nl.tno.idsa.Constants;
import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.semantics_impl.groups.Group;
import nl.tno.idsa.framework.utils.RandomNumber;
import nl.tno.idsa.framework.world.Area;
import nl.tno.idsa.framework.world.Polygon;
import nl.tno.idsa.framework.world.Vertex;
import nl.tno.idsa.library.locations.House;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// Split neighborhood and neighborhood generator code.
// TODO No documentation.
// TODO Code formatting.
public class Neighbourhood {

    private final String neighbourhoodName;
    private final Statistics expected;
    private final HashMap<House, Area> areas;
    private final Polygon polygon;
    private final List<House> houses;

    public Neighbourhood(String name, int occupants, int males, int females, double percentage0to15, double percentage15to25, double percentage25to45, double percentage45to65, double percentage65plus, Polygon polygon) {
        this.neighbourhoodName = name;
        this.expected = new Statistics(occupants, males, females,
                (int) Math.round(occupants * (percentage0to15 / 100)),
                (int) Math.round(occupants * (percentage15to25 / 100)),
                (int) Math.round(occupants * (percentage25to45 / 100)),
                (int) Math.round(occupants * (percentage45to65 / 100)),
                (int) Math.round(occupants * (percentage65plus / 100))
        );
        this.polygon = polygon;
        this.houses = new ArrayList<>();
        this.areas = new HashMap<>();
    }

    //Hill climbing
    public void optimize(List<Group> householdPool) {
        Statistics current = Statistics.compute(houses);
        // TODO Printouts to logger.
        // Before
        int currentTAE = expected.calculateTAE(current);
        System.out.println(neighbourhoodName + ": initial error: " + currentTAE);
        for (int i = 0; i < Constants.NEIGHBORHOOD_OPTIMIZATION_ITERATIONS; ++i) {
            // Pick a random house to optimize
            House house = houses.get(RandomNumber.nextInt(houses.size()));
            Group houseHousehold = house.getHousehold();
            // Pick a random househol from the pool
            int index = RandomNumber.nextInt(householdPool.size());
            Group poolHousehold = householdPool.get(index);
            // Compute difference in error when swapping
            Statistics newStats = new Statistics(current);
            // Remove existing household from stats
            for (Agent agent : houseHousehold) {
                newStats.remove(agent);
            }
            // Add newly considered household
            for (Agent agent : poolHousehold) {
                newStats.add(agent);
            }
            // Compute new error
            int newTAE = expected.calculateTAE(newStats);
            // Check whether this is an improvement
            if (newTAE < currentTAE) {
                currentTAE = newTAE;
                current = newStats;
                // Perform swap
                house.setHousehold(poolHousehold);
                householdPool.set(index, houseHousehold);
            }
        }
        // After
        System.out.println(neighbourhoodName + ": error after optimising: " + currentTAE);
        relocateHouseholds();
    }

    private void relocateHouseholds() {
        for (House house : houses) {
            Group household = house.getHousehold();
            for (Agent familyMember : household) {
                familyMember.allocateHome(areas.get(house));
                Vertex vertex = familyMember.getHouse();
                familyMember.setLocation(vertex.getPoint());
            }
        }
    }

    public void addHouse(House house, Area area) {
        houses.add(house);
        areas.put(house, area);
    }

    public Polygon getPolygon() {
        return polygon;
    }

    public List<House> getHouses() {
        return houses;
    }

    private static class Statistics {

        public int occupants;
        public int males;
        public int females;
        public int nr0to15;
        public int nr15to25;
        public int nr25to45;
        public int nr45to65;
        public int nr65plus;

        public Statistics() {
            this(0, 0, 0, 0, 0, 0, 0, 0);
        }

        public Statistics(Statistics other) {
            this(other.occupants, other.males, other.females, other.nr0to15, other.nr15to25, other.nr25to45, other.nr45to65, other.nr65plus);
        }

        public Statistics(int occupants, int males, int females, int nr0to15, int nr15to25, int nr25to45, int nr45to65, int nr65plus) {
            this.occupants = occupants;
            this.males = males;
            this.females = females;
            this.nr0to15 = nr0to15;
            this.nr15to25 = nr15to25;
            this.nr25to45 = nr25to45;
            this.nr45to65 = nr45to65;
            this.nr65plus = nr65plus;
        }

        public int calculateTAE(Statistics other) {
            int result = 0;
            result += Math.abs(other.occupants - occupants);
            result += Math.abs(other.males - males);
            result += Math.abs(other.females - females);
            result += Math.abs(other.nr0to15 - nr0to15);
            result += Math.abs(other.nr15to25 - nr15to25);
            result += Math.abs(other.nr25to45 - nr25to45);
            result += Math.abs(other.nr45to65 - nr45to65);
            result += Math.abs(other.nr65plus - nr65plus);
            return result;
        }

        public static Statistics compute(List<House> houses) {
            Statistics result = new Statistics();
            for (House house : houses) {
                Group household = house.getHousehold();
                for (Agent agent : household) {
                    result.add(agent);
                }
            }
            return result;
        }

        public void add(Agent a) {
            update(a, +1);
        }

        public void remove(Agent a) {
            update(a, -1);
        }

        private void update(Agent a, int increment) {
            occupants += increment;
            if (a.getGender() == Gender.MALE) {
                males += increment;
            } else {
                females += increment;
            }
            double age = a.getAge();
            if (age < 15) {
                nr0to15 += increment;
            } else if (age < 25) {
                nr15to25 += increment;
            } else if (age < 45) {
                nr25to45 += increment;
            } else if (age < 65) {
                nr45to65 += increment;
            } else {
                nr65plus += increment;
            }
        }
    }
}
