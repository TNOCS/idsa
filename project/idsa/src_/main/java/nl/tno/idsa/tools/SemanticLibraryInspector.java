package nl.tno.idsa.tools;

import nl.tno.idsa.framework.semantics_base.JavaSubclassFinder;
import nl.tno.idsa.framework.semantics_base.SemanticLibrary;
import nl.tno.idsa.framework.semantics_base.objects.SemanticObject;
import nl.tno.idsa.framework.utils.TextUtils;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Inspect the semantic library in a command-line output.
 */
public class SemanticLibraryInspector {
    public static void main(String[] args) {

        // Gather all classes.
        Set<Class<? extends SemanticObject>> semanticClasses = JavaSubclassFinder.listSubclasses(SemanticObject.class);

        // Print information on subclass relations.
        System.out.println("Subclass relations:");
        SortedSet<String> subclassStrings = new TreeSet<>();
        for (Class<? extends SemanticObject> semanticClass : semanticClasses) {
            Set<Class<? extends SemanticObject>> subclasses = SemanticLibrary.getInstance().listSemanticSubclasses(semanticClass);
            subclasses.remove(semanticClass);
            subclassStrings.add(semanticClass.getSimpleName() + " -> " + TextUtils.classNamesToString(subclasses));
        }
        for (String s : subclassStrings) {
            System.out.println(s);
        }

        // TODO We could print more here, e.g. the relations between objects.
    }
}
