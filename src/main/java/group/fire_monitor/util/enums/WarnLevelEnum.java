package group.fire_monitor.util.enums;

public enum WarnLevelEnum {
    NOTHING(0),
    LOG_ONLY(1),
    STORE_INFO(2),
    NOTICE_USER(3),

//    HIGH(3),
//    ADMIN(4)
    ;

    private final Integer level;

    WarnLevelEnum(Integer level) {
        this.level = level;
    }

    public Integer getLevel() {
        return level;
    }
}
