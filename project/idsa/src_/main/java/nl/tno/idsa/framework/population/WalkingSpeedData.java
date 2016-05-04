package nl.tno.idsa.framework.population;

import nl.tno.idsa.framework.utils.RandomNumber;

public class WalkingSpeedData {

    /**
     * Returns a comfortable walking speed in m/s.
     */
    public static double getComfortableSpeedInMs(double age, Gender gender) {

        double comfortableWalkingSpeed;

        if (gender == Gender.MALE) {
            if (age < 30) {
                comfortableWalkingSpeed = RandomNumber.nextGaussian(1.393, 0.153);
            } else if (age < 40) {
                comfortableWalkingSpeed = RandomNumber.nextGaussian(1.458, 0.94);
            } else if (age < 50) {
                comfortableWalkingSpeed = RandomNumber.nextGaussian(1.462, 0.162);
            } else if (age < 60) {
                comfortableWalkingSpeed = RandomNumber.nextGaussian(1.393, 0.229);
            } else if (age < 70) {
                comfortableWalkingSpeed = RandomNumber.nextGaussian(1.359, 0.205);
            } else {
                comfortableWalkingSpeed = RandomNumber.nextGaussian(1.330, 0.196);
            }
        } else {
            if (age < 30) {
                comfortableWalkingSpeed = RandomNumber.nextGaussian(1.407, 0.175);
            } else if (age < 40) {
                comfortableWalkingSpeed = RandomNumber.nextGaussian(1.415, 0.127);
            } else if (age < 50) {
                comfortableWalkingSpeed = RandomNumber.nextGaussian(1.391, 0.158);
            } else if (age < 60) {
                comfortableWalkingSpeed = RandomNumber.nextGaussian(1.395, 0.151);
            } else if (age < 70) {
                comfortableWalkingSpeed = RandomNumber.nextGaussian(1.296, 0.213);
            } else {
                comfortableWalkingSpeed = RandomNumber.nextGaussian(1.272, 0.211);
            }
        }
        if (isBelievableComfortableSpeed(comfortableWalkingSpeed))
            return comfortableWalkingSpeed;
        // Potential stackoverflow
        return getComfortableSpeedInMs(age, gender);
    }

    private static boolean isBelievableComfortableSpeed(double speed) {
        return !(speed < 0.5 || speed > 2.77);
    }
}
