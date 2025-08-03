package org.bri.usercenter.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 队伍用户信息封装类
 */
@Data
public class TeamUserVO {
    private Long id;

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
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 队伍的创建人信息
     */
    private UserVO founderUser;

    /**
     * 当前登录用户已加入该队伍？
     */
    private Boolean hasJoined;

    private Integer joinCount;
}
