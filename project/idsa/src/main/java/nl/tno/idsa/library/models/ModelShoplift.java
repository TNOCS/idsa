package nl.tno.idsa.library.models;

import nl.tno.idsa.framework.messaging.Messenger;
import nl.tno.idsa.library.roles.Shoplifter;

/**
 * Created by jongsd on 26-8-15.
 */
public class ModelShoplift extends SetRoleModel {

    public ModelShoplift() {
        super(Shoplifter.class, Who.ACTORS);
    }

    @Override
    protected boolean doSingleStep() {
        Messenger.broadcast(String.format("Agent(s) %s is/are shoplifting at %s", getActors(), getLocationAndEndTime()));
        return super.doSingleStep();
    }
}
