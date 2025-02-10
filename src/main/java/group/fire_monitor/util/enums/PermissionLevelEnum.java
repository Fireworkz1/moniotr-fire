package group.fire_monitor.util.enums;

public enum PermissionLevelEnum {
    READ_ONLY(1),
    NORMAL(2),
    HIGH(3),
    ADMIN(4);


    private final Integer level;

    PermissionLevelEnum(Integer level) {
        this.level = level;
    }

    public Integer getPermissionLevel() {
        return level;
    }
}
