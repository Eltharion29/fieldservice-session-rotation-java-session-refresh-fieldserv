public final class RefreshRotationTest {
    public static void main(String[] args) {
        SessionRotationService service = new SessionRotationService();
        service.register("session-old", "refresh-old");
        SessionRotationService.Rotation result = service.rotate("session-old", "session-new", "refresh-old");
        check("session-old".equals(result.revokedSessionId()), "old session id");
        check(!service.isActive("session-old"), "old session revoked");
        check(service.isActive("session-new"), "replacement session active");
        check("refresh-old:rotated".equals(result.refreshToken()), "new refresh token");
        System.out.println("PASS refresh rotation revokes the prior session");
    }
    private static void check(boolean condition, String label) { if (!condition) throw new AssertionError(label); }
}
