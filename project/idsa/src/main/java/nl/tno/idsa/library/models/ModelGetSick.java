package nl.tno.idsa.library.models;

import nl.tno.idsa.framework.messaging.Messenger;
import nl.tno.idsa.library.roles.MedicalVictim;

/**
 * Created by jongsd on 26-8-15.
 */
@SuppressWarnings("unused")
public class ModelGetSick extends SetRoleModel {

    public ModelGetSick() {
        super(MedicalVictim.class, null);
    }

    @Override
    public boolean doSingleStep() {
        if (getTargets() == null) {
            who = Who.ACTORS;
            Messenger.broadcast(String.format("%s is/are gettting sick at %s", getActors(), getLocationAndEndTime()));
        } else {
            who = Who.TARGETS;
            Messenger.broadcast(String.format("%s is/are infecting %s at %s", getActors(), getTargets(), getLocationAndEndTime()));
        }
        return super.doSingleStep();
    }
}
