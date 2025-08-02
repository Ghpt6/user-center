package org.bri.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import org.bri.usercenter.common.BaseResponse;
import org.bri.usercenter.common.BusinessException;
import org.bri.usercenter.common.ErrorCode;
import org.bri.usercenter.model.domain.Team;
import org.bri.usercenter.model.domain.User;
import org.bri.usercenter.model.domain.UserTeam;
import org.bri.usercenter.model.request.DeleteRequest;
import org.bri.usercenter.model.request.TeamAddRequest;
import org.bri.usercenter.model.dto.TeamQuery;
import org.bri.usercenter.model.request.TeamJoinRequest;
import org.bri.usercenter.model.request.TeamUpdateRequest;
import org.bri.usercenter.model.vo.TeamUserVO;
import org.bri.usercenter.service.TeamService;
import org.bri.usercenter.service.UserService;
import org.bri.usercenter.service.UserTeamService;
import org.bri.usercenter.utils.ResponseUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/team")
public class TeamController {
    @Autowired
    private UserService userService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private UserTeamService userTeamService;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        long addedTeam = teamService.addTeam(team, request);
        return ResponseUtils.success(addedTeam);
    }

    /**
     * 创建人解散队伍
     *
     * @param deleteRequest
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = deleteRequest.getId();
        if (teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean removed = teamService.deleteTeam(teamId, request);
        if (!removed) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResponseUtils.success(true);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean updated = teamService.updateTeam(teamUpdateRequest, request);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
        }
        return ResponseUtils.success(true);
    }

    @GetMapping("/get")
    public BaseResponse<Team> getTeam(@RequestParam Long teamId) {
        if (teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return ResponseUtils.success(team);
    }

    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> listTeam(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, request, false);
        return ResponseUtils.success(teamList);
    }

    /**
     * 获取当前用户创建的队伍
     *
     * @param request
     * @return
     */
    @GetMapping("/list/created")
    public BaseResponse<List<TeamUserVO>> listCreatedTeam(HttpServletRequest request) {
        User loginUser = userService.getCurLoginUser(request);
        TeamQuery teamQuery = new TeamQuery();
        teamQuery.setFounderId(loginUser.getId());

        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, request, true);
        return ResponseUtils.success(teamList);
    }

    /**
     * 获取已加入的队伍
     *
     * @param request
     * @return
     */
    @GetMapping("/list/joined")
    public BaseResponse<List<TeamUserVO>> listJoinedTeam(HttpServletRequest request) {
        User loginUser = userService.getCurLoginUser(request);

        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId", loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
        List<Long> teamIdList = userTeamList.stream().map(userTeam -> userTeam.getTeamId()).toList();

        TeamQuery teamQuery = new TeamQuery();
        teamQuery.setIdList(teamIdList);

        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, request, true);
        return ResponseUtils.success(teamList);
    }

    //todo 分页
    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamByPage(TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery, team);
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<Team>(team);
        Page<Team> page = new Page<>(teamQuery.getCurPage(), teamQuery.getPageSize());
        Page<Team> teamList = teamService.page(page, teamQueryWrapper);
        return ResponseUtils.success(teamList);
    }

    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean joined = teamService.joinTeam(teamJoinRequest, request);
        if (!joined) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "加入失败");
        }
        return ResponseUtils.success(true);
    }

    /**
     * 队员退出队伍
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/exit")
    public BaseResponse<Boolean> exitTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = deleteRequest.getId();
        Boolean exited = teamService.exitTeam(teamId, request);
        if (!exited) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "退出失败");
        }
        return ResponseUtils.success(true);
    }
}
