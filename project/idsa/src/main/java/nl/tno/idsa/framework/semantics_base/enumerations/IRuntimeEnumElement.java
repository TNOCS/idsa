package nl.tno.idsa.framework.semantics_base.enumerations;

/**
 * Runtime enumerations create the opportunity to create options (like normal enums) that are not fixed in code, but
 * rather gathered from implementing classes. For example, all days of the week implement DayOfWeek, which implements
 * the IRuntimeEnumElement interface. We can then list the various days of the week by calling
 * the listOptions method of RuntimeEnum.getInstance(DayOfWeek.class). Outside this class, this is all type safe.
 */
public interface IRuntimeEnumElement {
    /**
     * Enum elements must be ordered by a unique index.
     */
    public int getIndex();
}
