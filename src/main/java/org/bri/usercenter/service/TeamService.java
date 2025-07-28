package org.bri.usercenter.service;

import jakarta.servlet.http.HttpServletRequest;
import org.bri.usercenter.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author ThinkPad
* @description 针对表【team(队伍表)】的数据库操作Service
* @createDate 2025-07-25 14:59:16
*/
public interface TeamService extends IService<Team> {
    /**
     * 用户创建队伍
     *
     * @param team
     * @param request 用户请求
     * @return 创建的队伍id
     */
    long addTeam(Team team, HttpServletRequest request);
}
