package nl.tno.idsa.framework.population;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.messaging.ProgressNotifier;
import nl.tno.idsa.framework.semantics_base.objects.ParameterId;
import nl.tno.idsa.framework.semantics_impl.groups.Group;
import nl.tno.idsa.framework.semantics_impl.locations.LocationFunction;
import nl.tno.idsa.framework.semantics_impl.variables.Variable;
import nl.tno.idsa.framework.utils.FeatureUtils;
import nl.tno.idsa.framework.utils.RandomNumber;
import nl.tno.idsa.framework.world.*;
import nl.tno.idsa.library.locations.House;
import org.geotools.data.FileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.metadata.identification.Progress;

import java.util.*;

public class PopulationGenerator {

    private final Environment environment;
    private final PopulationData populationData;

    private final List<Neighbourhood> neighbourhoods;
    private final List<Group> families;

    private static final int populationSurplus = 5;

    //For social network
    private static final double Mr = 2.05;
    private static final double Ms = 5;
    private static final int kmax = 100;

    public PopulationGenerator(Environment environment, PopulationData populationData) {
        this.environment = environment;
        this.populationData = populationData;
        this.neighbourhoods = new ArrayList<>(14);
        this.families = new ArrayList<>(10000);
    }

    public List<Agent> generatePopulation(String neighbourhoodDataFilename) {

        //parse the neighbourhoods
        ProgressNotifier.notifyProgressMessage("Parsing neighbourhoods...");
        int nrOfNecessaryAgents = parseNeighbourhoods(neighbourhoodDataFilename);

        // initialise population
        ProgressNotifier.notifyProgressMessage("Creating households...");
        for (int i = 0; i < nrOfNecessaryAgents * populationSurplus; ) {
            Group household = createHousehold();
            i += household.size();
            families.add(household);
            ProgressNotifier.notifyProgress(1.0 * i / (nrOfNecessaryAgents * populationSurplus));
        }

        // allocate families to houses
        ProgressNotifier.notifyUnknownProgress();
        ProgressNotifier.notifyProgressMessage("Allocating houses to households...");
        addHousesToNeighbourhoods();
        allocateHouseholdsToHouses();

        // hill climb on local data
        ProgressNotifier.notifyProgress(0); // Reset to known progress.
        for (int i = 0; i < neighbourhoods.size(); i++) {
            Neighbourhood neighbourhood = neighbourhoods.get(i);
            ProgressNotifier.notifyProgressMessage("Optimizing neighbourhood '" + neighbourhood.getNeighbourhoodName() + "'...");
            neighbourhood.optimize(families);
            ProgressNotifier.notifyProgress(1.0 * (i+1) / neighbourhoods.size());
        }

        // last names
        ProgressNotifier.notifyProgressMessage("Fixing last names for families...");
        List<Agent> result = getAgents();
        for (Agent a : result) {
            a.adaptLastNameToHousehold();
        }

        // Generate social network
        ProgressNotifier.notifyUnknownProgress();
        ProgressNotifier.notifyProgressMessage("Generating social network...");
        makeSocialNetwork(result);

        return result;
    }

    private Map<Agent, List<Agent>> makeSocialNetwork(List<Agent> agents) {

        HashMap<Agent, List<Agent>> network = new HashMap<>();

        //connect everyone to their family members
        for (Neighbourhood neighbourhood : neighbourhoods) {
            for (House house : neighbourhood.getHouses()) {
                Group household = house.getHousehold();
                for (Agent agent : household) {
                    ArrayList<Agent> connections = new ArrayList<>();
                    for (Agent connection : household) {
                        if (agent != connection) {
                            connections.add(connection);
                        }
                    }
                    network.put(agent, connections);
                    if (network.get(agent).isEmpty() && agent.getHouseholdRole() != HouseholdRoles.SINGLE) {
                        System.out.println("size = " + "0");
                    }
                }
            }
        }

        int i;
        //Initialise network
        for (i = 0; i < Math.sqrt(agents.size()); i++) {
            Agent agent = agents.get(i);
            network.put(agent, new ArrayList<Agent>());
            while (true) {
                Agent other = agents.get(RandomNumber.nextInt((int) Math.sqrt(agents.size())));
                if (agent != other) {
                    network.get(agent).add(other);
                    break;
                }
            }

        }

        for (; i < agents.size(); i++) {

            Agent agent = agents.get(i);
            HashSet<Agent> initialContacts = new HashSet<>();
            ArrayList<Agent> secondaryContacts = new ArrayList<>();
            initialContacts.addAll(network.get(agent));

            boolean initConnection = false;
            do {
                int r = RandomNumber.nextInt(Math.max(i, 1));
                Agent possibleConnection = agents.get(i);
                if (network.get(possibleConnection).size() < kmax) {
                    initialContacts.add(agents.get(r));
                    initConnection = true;
                }
            } while (RandomNumber.nextDouble() < (1.0 - (1.0 / Mr)) || !initConnection);

            //get  possible secondary contacts
            for (Agent friend : initialContacts) {
                for (Agent friendOfFriend : network.get(friend)) {
                    if (network.get(friendOfFriend).size() < kmax && !initialContacts.contains(friendOfFriend)) {
                        secondaryContacts.add(friendOfFriend);
                    }
                }
            }

            //pick secondary contacts
            if (!secondaryContacts.isEmpty()) {
                for (int r = RandomNumber.nextInt(secondaryContacts.size());
                     !secondaryContacts.isEmpty() && RandomNumber.nextDouble() < (1 - (1 / Ms));
                     r = RandomNumber.nextInt(secondaryContacts.size())) {
                    initialContacts.add(secondaryContacts.get(r));
                }
            }
            //connect agent to his friends
            network.put(agent, new ArrayList<>(initialContacts));
            //and his friends to the agent
            for (Agent friend : initialContacts) {
                network.get(friend).add(agent);
            }
        }

        //FixMe test for #contacts
        int total = 0;
        for (Map.Entry<Agent, List<Agent>> entry : network.entrySet()) {
            Agent agent = entry.getKey();
            total += entry.getValue().size();
            if (entry.getValue().isEmpty()) {
                System.out.println("size = " + entry.getValue().size());
            }
            agent.addContacts(entry.getValue());
        }
        System.out.println("Average people you know: " + (double) total / agents.size());

        return network;
    }

    private List<Agent> getAgents() {
        List<Agent> agents = new ArrayList<>();
        for (Neighbourhood neighbourhood : neighbourhoods) {
            for (House house : neighbourhood.getHouses()) {
                Group household = house.getHousehold();
                for (Agent agent : household) {
                    agents.add(agent);
                }
            }
        }
        return agents;
    }

    private void allocateHouseholdsToHouses() {
        for (Neighbourhood neighbourhood : neighbourhoods) {
            for (House house : neighbourhood.getHouses()) {
                for (int i = families.size() - 1; i >= 0; i--) {//from the back for efficiency: we expect to find a suitable household very soon
                    Group household = families.get(i);
                    if (fitsInHouse(household, house)) {
                        house.setHousehold(household);
                        families.remove(i);
                        break;
                    }
                }
            }
        }
    }

    private boolean fitsInHouse(Group household, House house) {
        Map<ParameterId, Variable> map = house.getParameters();
        Variable v = map.get(LocationFunction.Parameters.CAPACITY);
        int capacity = (Integer) v.getValue();
        return capacity >= household.size();
    }

    private void addHousesToNeighbourhoods() {
        for (Area area : environment.getWorld().getAreas()) {
            for (LocationFunction function : area.getFunctions(House.class)) {
                Vertex vertex = area.getVertices().get(0);
                Point point = vertex.getPoint();
                for (Neighbourhood neighbourhood : neighbourhoods) {
                    Polygon polygon = neighbourhood.getPolygon();
                    if (polygon.contains(point)) {
                        neighbourhood.addHouse((House) function, area);
                        break;
                    }
                }
            }
        }
    }

    private Group createHousehold() {
        HouseholdTypes householdType = populationData.getRandomHouseHoldType();
        Group output;
        switch (householdType) {
            case SINGLE:
                output = createSingleHousehold();
                break;
            case PAIR:
                output = createPairHousehold();
                break;
            case SINGLE_PARENT:
            default:
                output = createSingleParentHousehold();
                break;
        }
        if (output != null) {
            for (Agent agent : output) {
                agent.setHousehold(output);
            }
        }
        return output;
    }

    private Group createSingleHousehold() {
        HouseholdTypes hhType = HouseholdTypes.SINGLE;
        Gender gender = populationData.getRandomGender(hhType);
        double age = populationData.getRandomAge(hhType, gender);
        Agent agent = new Agent(age, gender, hhType, HouseholdRoles.SINGLE, environment.getYear());
        Group household = new Group();
        household.add(agent);
        return household;
    }

    private Group createPairHousehold() {
        HouseholdTypes hhType = HouseholdTypes.PAIR;
        Gender gender = populationData.getRandomGender(hhType);
        double age = populationData.getRandomAge(hhType, gender);
        Agent firstParent = new Agent(age, gender, hhType, populationData.getHouseholdRole(hhType, gender), environment.getYear());
        Group household = new Group();
        household.add(firstParent);
        double probHetero = 0.95; // TODO Hardcoded heterosexuality threshold.
        Gender otherGender;
        if (RandomNumber.nextDouble() < probHetero) {
            otherGender = Gender.other(gender);
        } else {
            otherGender = gender;
        }
        // TODO Introduce an age relation between parent1 and parent2; currently hard limit on 10 years difference. Via population data.
        double randomAge = Double.MAX_VALUE;
        while (Math.abs(age - randomAge) > 10) {
            randomAge = populationData.getRandomAge(hhType, otherGender);
        }
        Agent secondParent = new Agent(randomAge, otherGender, hhType, populationData.getHouseholdRole(hhType, otherGender), environment.getYear());
        household.add(secondParent);
        int numChildren = populationData.getRandomNumberOfChildren(hhType, age); // TODO Parents seem really old.
        if (numChildren == 0) {
            firstParent.setHouseholdRole(HouseholdRoles.IN_RELATIONSHIP);
            secondParent.setHouseholdRole(HouseholdRoles.IN_RELATIONSHIP);
        } else {
            for (int i = 0; i < numChildren; ++i) {
                Gender genderOfChild = populationData.getRandomGenderOfChild();
                double ageChild = populationData.getRandomAgeOfChild(household, gender);
                Agent child = new Agent(ageChild, genderOfChild, hhType, HouseholdRoles.CHILD, environment.getYear());
                household.add(child);
            }
        }
        return household;
    }

    private Group createSingleParentHousehold() {
        HouseholdTypes hhType = HouseholdTypes.SINGLE_PARENT;
        Gender gender = populationData.getRandomGender(hhType);
        double age = populationData.getRandomAge(hhType, gender);
        Agent parent = new Agent(age, gender, hhType, populationData.getHouseholdRole(hhType, gender), environment.getYear());
        Group household = new Group();
        household.add(parent);
        //TODO: Perhaps: Add normal distribution to determination of ageChild.
        //TODO: Add more than 3 children to a family.
        int numChildren = Math.max(1, populationData.getRandomNumberOfChildren(hhType, age));
        for (int i = 0; i < numChildren; ++i) {
            Gender genderOfChild = populationData.getRandomGenderOfChild();
            double ageChild = populationData.getRandomAgeOfChild(household, gender);
            Agent child = new Agent(ageChild, genderOfChild, hhType, HouseholdRoles.CHILD, environment.getYear());
            household.add(child);
        }
        return household;
    }

    // TODO refactor!
    private int parseNeighbourhoods(String filename) {
        int nrOfNecessaryAgents = 0;
        FileDataStore store = FeatureUtils.loadFeatures(filename);
        SimpleFeatureCollection features = FeatureUtils.getFeatures(store);
        if (features != null) {
            FeatureIterator iterator = features.features();
            while (iterator.hasNext()) {
                Feature feature = iterator.next();

                String name = FeatureUtils.getFieldValue(feature, "buurtnaam");
                int occupants = (int) Math.round(FeatureUtils.<Double>getFieldValue(feature, "aantal_inw"));
                nrOfNecessaryAgents += occupants;
                int males = (int) Math.round(FeatureUtils.<Double>getFieldValue(feature, "mannen"));
                int females = (int) Math.round(FeatureUtils.<Double>getFieldValue(feature, "vrouwen"));
                int percentage0to15 = (int) Math.round(FeatureUtils.<Double>getFieldValue(feature, "percenta11"));
                int percentage15to25 = (int) Math.round(FeatureUtils.<Double>getFieldValue(feature, "percenta12"));
                int percentage25to45 = (int) Math.round(FeatureUtils.<Double>getFieldValue(feature, "percenta13"));
                int percentage45to65 = (int) Math.round(FeatureUtils.<Double>getFieldValue(feature, "percenta14"));
                int percentage65plus = (int) Math.round(FeatureUtils.<Double>getFieldValue(feature, "percenta15"));

                Polygon polygon = FeatureUtils.convertPolygon(feature, environment.getWorld().getUtmRoot());

                Neighbourhood neighbourhood = new Neighbourhood(name, occupants, males, females, percentage0to15, percentage15to25, percentage25to45, percentage45to65, percentage65plus, polygon);
                neighbourhoods.add(neighbourhood);
            }
            // Clean up and release file
            iterator.close();
            store.dispose();
        }
        return nrOfNecessaryAgents;
    }
}
