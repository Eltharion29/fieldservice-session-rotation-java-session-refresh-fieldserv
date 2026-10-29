public final class FieldServiceDemo {
    public static void main(String[] args) throws Exception {
        FieldServiceCase visit = new FieldServiceCase("wo-1042", args.length > 0 ? args[0] : "technician-42");
        visit.addPhoto("photo://wo-1042-before");
        visit.dispatch(FieldServiceCase.DispatchStatus.ON_SITE);
        visit.recordFollowUp("Replace pressure valve during next visit");
        System.out.println(visit.summary());
        if (args.length < 2) return;
        String key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) throw new IllegalStateException("INFRAI_API_KEY is required");
        InfraiSessionClient client = new InfraiSessionClient(key);
        String refreshed = client.refresh(args[1]);
        System.out.println("session refresh accepted: " + refreshed.length() + " response characters");
        System.out.println("previous session revocation response: " + client.revoke("session-for-" + args[0]).length() + " response characters");
    }
}
