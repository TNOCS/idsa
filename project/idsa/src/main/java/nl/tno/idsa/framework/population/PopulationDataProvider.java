package nl.tno.idsa.framework.population;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.utils.RandomNumber;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class PopulationDataProvider {

    protected final Map<HouseholdTypes, Integer> householdTypes;

    protected final Map<HouseholdTypes, Map<Gender, Integer>> householdGenderDistributions;

    protected final Map<HouseholdTypes, Map<Gender, List<Integer>>> ageDistributions;

    protected final Map<HouseholdTypes, List<List<Integer>>> numChildren;

    protected final Map<Gender, Integer> childrenGenderDistributions;

    protected final Map<Gender, List<Integer>> childrenAgeDistributions;

    // Age step size in distributions in years, i.e. [[0 .. ageStepSize], [ageStepSize .. 2 * ageStepSize]. ..]
    protected final int ageStepSize;
    // Maximum age of a member of the population
    protected final int maxAge;
    // Hardcoded minimum age for a mother to get her first child
    protected static final double MIN_AGE_MOTHER = 15.0;
    // Hardcoded maximum age for a mother to get her last child
    protected static final double MAX_AGE_MOTHER = 48.0;

    /**
     * Subclasses must have a default constructor.
     * @param ageCategoryRange
     * @param maxAge
     */
    public PopulationDataProvider(int ageCategoryRange, int maxAge) {
        this.ageStepSize = ageCategoryRange;
        this.maxAge = maxAge;
        this.householdTypes = new EnumMap<>(HouseholdTypes.class);
        this.householdGenderDistributions = new EnumMap<>(HouseholdTypes.class);
        this.ageDistributions = new EnumMap<>(HouseholdTypes.class);
        this.numChildren = new EnumMap<>(HouseholdTypes.class);
        this.childrenGenderDistributions = new EnumMap<>(Gender.class);
        this.childrenAgeDistributions = new EnumMap<>(Gender.class);
    }

    HouseholdTypes getRandomHouseholdType() {
        return RandomNumber.drawFromEnumeratedMap(HouseholdTypes.class, householdTypes);
    }

    Gender getRandomGender(HouseholdTypes householdType) {
        return RandomNumber.drawFromEnumeratedMap(Gender.class, householdGenderDistributions.get(householdType));
    }

    Gender getRandomGenderOfChild() {
        return RandomNumber.drawFromEnumeratedMap(Gender.class, childrenGenderDistributions);
    }

    double getRandomAge(HouseholdTypes householdType, Gender gender) {
        int ageCategory = RandomNumber.drawIndex(ageDistributions.get(householdType).get(gender));
        return RandomNumber.nextDouble(ageStepSize) + ageStepSize * ageCategory;
    }

    int getAgeCategory(double age) {
        return Math.max(0, Math.min((maxAge / ageStepSize) - 1, (int) (age / ageStepSize)));
    }

    HouseholdRoles getHouseholdRole(HouseholdTypes hhType, Gender gender) {
        HouseholdRoles result;
        switch (hhType) {
            case PAIR:
                result = gender == Gender.MALE ? HouseholdRoles.FATHER : HouseholdRoles.MOTHER;
                break;
            case SINGLE_PARENT:
                result = gender == Gender.MALE ? HouseholdRoles.FATHER : HouseholdRoles.MOTHER;
                break;
            case SINGLE:
            default:
                result = HouseholdRoles.SINGLE;
                break;
        }
        return result;
    }

    int getRandomNumberOfChildren(HouseholdTypes hhType, double age) {
        int result;
        int ageCategory = getAgeCategory(age);
        switch (hhType) {
            case PAIR:
            case SINGLE_PARENT:
                result = RandomNumber.drawIndex(numChildren.get(hhType).get(ageCategory)); // NOTE: index 3 means 3+ children!
                break;
            case SINGLE:
            default:
                result = 0;
                break;
        }
        return result;
    }

    double getRandomAgeOfChild(List<Agent> household, Gender gender) {
        double restrictingAge = maxAge;
        for (Agent a : household) {
            if (a.getHouseholdRole() == HouseholdRoles.MOTHER) {
                restrictingAge = Math.min(restrictingAge, a.getAge());
            }
        }
        // min and max age restrictions, used in sampling
        double minAge = Math.max(0.0, restrictingAge - MAX_AGE_MOTHER);
        double maxAge = Math.max(0.0, restrictingAge - MIN_AGE_MOTHER);
        int ageCategory = RandomNumber.drawIndex(childrenAgeDistributions.get(gender), (int) (minAge / ageStepSize), (int) (maxAge / ageStepSize));
        return RandomNumber.nextDouble(ageStepSize) + ageStepSize * ageCategory;
    }
}

