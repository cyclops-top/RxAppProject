package justin.rxapp;

/**
 * @author justin on 2017/04/19 09:57
 * @version V1.0
 */
public class Permission {
    private final String permission;
    private final boolean isGranted;

    public Permission(String permission, boolean isGranted) {
        this.permission = permission;
        this.isGranted = isGranted;
    }

    public String getPermission() {
        return permission;
    }

    public boolean isGranted() {
        return isGranted;
    }

    @Override
    public String toString() {
        return "Permission{" +
                "permission='" + permission + '\'' +
                ", isGranted=" + isGranted +
                '}';
    }
}
