package group.fire_monitor.util.enums;

public enum WarnNoticeEnum {
    WARNING(1),
    SAFE(0)
//    HIGH(3),
//    ADMIN(4)
    ;

    private final Integer level;

    WarnNoticeEnum(Integer level) {
        this.level = level;
    }

    public Integer getLevel() {
        return level;
    }
}

