package fr.adbonnin.issue.utils;

public class ArrayUtil {

    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    public static String[] nullToEmpty(String[] array) {
        return array == null ? EMPTY_STRING_ARRAY : array;
    }

    private ArrayUtil() { /* Cannot be instantiated */ }
}
