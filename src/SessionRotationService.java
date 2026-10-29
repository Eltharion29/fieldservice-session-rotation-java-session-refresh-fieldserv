import java.util.HashMap;
import java.util.Map;

public final class SessionRotationService {
    private final Map<String, Session> sessions = new HashMap<>();

    public void register(String sessionId, String refreshToken) { sessions.put(sessionId, new Session(refreshToken, true)); }

    public Rotation rotate(String oldSessionId, String newSessionId, String presentedToken) {
        Session old = sessions.get(oldSessionId);
        if (old == null || !old.active || !old.refreshToken.equals(presentedToken)) throw new IllegalStateException("refresh rejected");
        old.active = false;
        sessions.put(newSessionId, new Session(presentedToken + ":rotated", true));
        return new Rotation(oldSessionId, newSessionId, sessions.get(newSessionId).refreshToken);
    }

    public boolean isActive(String sessionId) { return sessions.containsKey(sessionId) && sessions.get(sessionId).active; }
    public record Rotation(String revokedSessionId, String activeSessionId, String refreshToken) {}
    private static final class Session {
        final String refreshToken; boolean active;
        Session(String refreshToken, boolean active) { this.refreshToken = refreshToken; this.active = active; }
    }
}
