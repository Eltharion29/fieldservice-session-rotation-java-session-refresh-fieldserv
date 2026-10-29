public final class Json {
    private Json() {}
    public static String quote(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
