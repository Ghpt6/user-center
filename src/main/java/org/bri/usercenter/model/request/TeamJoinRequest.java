package org.bri.usercenter.model.request;

import lombok.Data;

@Data
public class TeamJoinRequest {
    /**
     *
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;
}
