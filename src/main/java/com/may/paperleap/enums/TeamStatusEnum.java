package com.may.paperleap.enums;

/**
 * @author May20242
 * 队伍状态枚举类
 */
public enum TeamStatusEnum {
    PUBLIC(0, "公开"),
    PRIVATE(1, "私有"),
    SECRET(2, "加密");

    private int value;

    private String text;

    public static TeamStatusEnum getTeamStatusEnum(int value) {
        TeamStatusEnum[] values = TeamStatusEnum.values();
        for(TeamStatusEnum statusEnum : values) {
            if(statusEnum.getValue() == value) {
                return statusEnum;
            }
        }
        return null;
    }

    TeamStatusEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }

    public int getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
