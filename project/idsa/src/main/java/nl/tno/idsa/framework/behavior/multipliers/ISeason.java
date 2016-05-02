package nl.tno.idsa.framework.behavior.multipliers;

import nl.tno.idsa.framework.semantics_base.enumerations.IRuntimeEnumElement;

/**
 * Empty interface on top of multiplier and runtime enum. This means we can list all ISeason implementations at runtime.
 */
public interface ISeason extends IRuntimeEnumElement, IMultiplier {

}
