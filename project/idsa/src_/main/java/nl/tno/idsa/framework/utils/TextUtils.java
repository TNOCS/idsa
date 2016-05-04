package nl.tno.idsa.framework.utils;

import java.util.Collection;

// TODO Document class.

public class TextUtils {

    public static String camelCaseToText(String text) {
        if (text == null || text.length() == 0) {
            return text;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(text.charAt(0));
        for (int i = 1; i < text.length(); i++) {
            if (Character.isUpperCase(text.charAt(i))) {
                builder.append(" ").append(Character.toLowerCase(text.charAt(i)));
            } else {
                builder.append(text.charAt(i));
            }
        }
        return builder.toString();
    }

    public static String classNamesToString(Collection collection) {
        return String.format("[%s]", classNamesToSimpleString(collection));
    }

    public static String classNamesToSimpleString(Collection collection) {
        String ret = "";
        if (collection != null && collection.size() > 0) {
            for (Object object : collection) {
                if (object instanceof Class) {
                    ret = ret + ((Class) object).getSimpleName() + ", ";
                } else {
                    ret = ret + object.getClass().getSimpleName() + ", ";
                }
            }
            ret = ret.substring(0, ret.length() - 2);
        }
        return ret;
    }

    public static String addSpacesToString(Object o, int totalLength) {
        String s = o.toString();
        if (s.length() >= totalLength) {
            s = s.substring(0, totalLength - 4) + "...";
        }
        while (s.length() < totalLength) {
            s = s + " ";
        }
        return s;
    }
}
