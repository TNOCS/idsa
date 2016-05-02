package nl.tno.idsa.library.models;

import nl.tno.idsa.framework.messaging.Messenger;
import nl.tno.idsa.library.roles.Civilian;

/**
 * Created by jongsd on 26-8-15.
 */
@SuppressWarnings("unused")
public class ModelComeToAidOfCrimeVictim extends SetRoleModel {

    public ModelComeToAidOfCrimeVictim() {
        super(Civilian.class, Who.TARGETS);
    }

    @Override
    public boolean doSingleStep() {
        Messenger.broadcast(String.format("Agent(s) %s is/are coming to the aid of %s at %s", getActors(), getTargets(), getLocationAndEndTime()));
        return super.doSingleStep();
    }
}
