package org.bri.usercenter.model.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.util.Date;

@Data
public class TeamAddRequest {
    /**
     * 队伍名称
     */
    private String name;

    /**
     * 队伍描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 队伍过期时间
     */
    private Date expireTime;

    /**
     * 队伍创建人id
     */
    private Long founderId;

    /**
     * 状态（0-公开，1-私有，2-加密）
     */
    private Integer status;

    /**
     * 密码
     */
    private String password;
}
