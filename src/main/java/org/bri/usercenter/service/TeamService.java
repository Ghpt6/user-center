package org.bri.usercenter.service;

import jakarta.servlet.http.HttpServletRequest;
import org.bri.usercenter.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import org.bri.usercenter.model.dto.TeamQuery;
import org.bri.usercenter.model.request.TeamJoinRequest;
import org.bri.usercenter.model.request.TeamUpdateRequest;
import org.bri.usercenter.model.vo.TeamUserVO;

import java.util.List;

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

    /**
     * 搜索队伍
     *
     * @param teamQuery 队伍信息
     * @param request
     * @return
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery, HttpServletRequest request);

    /**
     * 更新队伍信息
     * @param teamUpdateRequest
     * @param request
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, HttpServletRequest request);

    /**
     * 加入队伍
     * @param teamJoinRequest
     * @param request
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, HttpServletRequest request);
}
