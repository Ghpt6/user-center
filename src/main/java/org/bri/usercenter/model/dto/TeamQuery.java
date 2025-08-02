package org.bri.usercenter.model.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bri.usercenter.common.PageRequest;

import java.util.Date;
import java.util.List;

/**
 * 队伍请求包装类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TeamQuery extends PageRequest {


    private Long id;

    /**
     * id列表，查询包含其中的队伍
     */
    private List<Long> idList;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 搜索关键词( 根据关键词可以搜索名称，描述等任一个包含的队伍）
     */
    private String searchTerm;

    /**
     * 队伍描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 队伍创建人id
     */
    private Long founderId;

    /**
     * 状态（0-公开，1-私有，2-加密）
     */
    private Integer status;
}
