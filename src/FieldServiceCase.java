import java.util.ArrayList;
import java.util.List;

public final class FieldServiceCase {
    public enum DispatchStatus { ASSIGNED, EN_ROUTE, ON_SITE, COMPLETE }
    private final String workOrderId;
    private final String technicianId;
    private final List<String> photoReferences = new ArrayList<>();
    private DispatchStatus status = DispatchStatus.ASSIGNED;
    private String followUp = "";

    public FieldServiceCase(String workOrderId, String technicianId) {
        this.workOrderId = workOrderId;
        this.technicianId = technicianId;
    }
    public void addPhoto(String reference) { photoReferences.add(reference); }
    public void dispatch(DispatchStatus next) { status = next; }
    public void recordFollowUp(String note) { followUp = note; }
    public String summary() {
        return workOrderId + " technician=" + technicianId + " status=" + status + " photos=" + photoReferences.size() + " followUp=" + followUp;
    }
}
