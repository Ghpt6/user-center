package org.bri.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页请求体
 */
@Data
public class PageRequest implements Serializable {

    /**
     * 每页显示多少记录
     */
    private int pageSize;

    /**
     * 查询第几页
     */
    private int curPage;
}
