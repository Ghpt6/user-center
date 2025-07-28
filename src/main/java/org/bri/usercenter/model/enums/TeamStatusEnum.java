package org.bri.usercenter.model.enums;

import lombok.Getter;

@Getter
public enum TeamStatusEnum {
    PUBLIC(0, "公开"),
    PRIVATE(1, "私有"),
    ENCRYPTED(2, "加密"),;

    private final int status;
    private final String description;

    TeamStatusEnum(int status, String desc) {
        this.status = status;
        this.description = desc;
    }

    public static TeamStatusEnum getEnumByStatus(Integer status) {
        if (status == null)
            return null;
        for (TeamStatusEnum t : TeamStatusEnum.values()) {
            if(t.status == status) {
                return t;
            }
        }
        return null;
    }
}
