package no.pharmacy.infrastructure;

public class ExceptionUtil {

    public static RuntimeException softenException(Exception e) {
        return softenHelper(e);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Exception> T softenHelper(Exception e) throws T {
        throw (T)e;
    }

}
