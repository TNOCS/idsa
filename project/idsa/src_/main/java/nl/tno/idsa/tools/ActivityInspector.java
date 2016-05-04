package nl.tno.idsa.tools;

import nl.tno.idsa.framework.behavior.activities.possible.PossibleActivity;
import nl.tno.idsa.framework.behavior.activities.possible.PossibleTimeIntervals;
import nl.tno.idsa.framework.semantics_base.JavaSubclassFinder;
import nl.tno.idsa.framework.world.Time;

import java.lang.reflect.Constructor;
import java.util.Set;

/**
 * Created by jongsd on 22-3-16.
 */
public class ActivityInspector {

    public static void main(String[] args) {
        final Set<Class<? extends PossibleActivity>> classes = JavaSubclassFinder.listSubclasses(PossibleActivity.class);
        for (Class<? extends PossibleActivity> pac : classes) {

            try {
                final Constructor<? extends PossibleActivity> constructor = pac.getConstructor(PossibleTimeIntervals.class);
                final PossibleActivity possibleActivity = constructor.newInstance(new PossibleTimeIntervals(new Time(), new Time(), 0, 0));
                System.out.println(possibleActivity.getName() + " has priority " + possibleActivity.getPriority()); // TODO Show more info. Sort by priority.
            } catch (Exception e) {
                System.out.println(pac.getSimpleName() + " - not a valid activity class");
            }
        }
    }
}
