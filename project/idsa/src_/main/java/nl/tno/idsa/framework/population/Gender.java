package nl.tno.idsa.framework.population;

public enum Gender {

    MALE, FEMALE;

    static Gender other(Gender gender) {
        return gender == MALE ? FEMALE : MALE;
    }
}
