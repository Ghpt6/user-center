package org.bri.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.bri.usercenter.common.BusinessException;
import org.bri.usercenter.common.ErrorCode;
import org.bri.usercenter.model.domain.Team;
import org.bri.usercenter.model.domain.User;
import org.bri.usercenter.model.domain.UserTeam;
import org.bri.usercenter.model.dto.TeamQuery;
import org.bri.usercenter.model.enums.TeamStatusEnum;
import org.bri.usercenter.model.request.TeamJoinRequest;
import org.bri.usercenter.model.request.TeamUpdateRequest;
import org.bri.usercenter.model.vo.TeamUserVO;
import org.bri.usercenter.model.vo.UserVO;
import org.bri.usercenter.service.TeamService;
import org.bri.usercenter.mapper.TeamMapper;
import org.bri.usercenter.service.UserService;
import org.bri.usercenter.service.UserTeamService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author ThinkPad
 * @description 针对表【team(队伍表)】的数据库操作Service实现
 * @createDate 2025-07-25 14:59:16
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {
    @Autowired
    private UserService userService;

    @Autowired
    private UserTeamService userTeamService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, HttpServletRequest request) {
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "team is null");
        }
        // 当前登录用户
        User loginUser = userService.getCurLoginUser(request);

        // 队伍人数 1-30
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不符合要求");
        }
        // 名称长度<=30
        String teamName = team.getName();
        if (teamName.isBlank() || teamName.length() > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍名称错误");
        }

        // 描述<=1024
        String description = team.getDescription();
        if (!description.isBlank() && description.length() > 1024) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "描述<=1024");
        }

        int status = Optional.ofNullable(team.getStatus()).orElse(0);

        TeamStatusEnum enumByStatus = TeamStatusEnum.getEnumByStatus(status);
        if (enumByStatus == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "param status error");
        }

        // 加密判断
        String password = team.getPassword();
        if (enumByStatus.equals(TeamStatusEnum.ENCRYPTED) && password.isBlank() || password.length() > 32) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "param password error");
        }

        Date expireTime = team.getExpireTime();
        if (expireTime == null || expireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "时间错乱");
        }

        QueryWrapper<Team> qw = new QueryWrapper<>();
        qw.eq("founderId", loginUser.getId());
        team.setFounderId(loginUser.getId()); // 防止恶意修改创建人id
        long hasTeamNum = this.count(qw);
        if (hasTeamNum >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍创建太多");
        }

        // 事务级插入
        team.setId(null);
        boolean saved = this.save(team);
        Long teamId = team.getId();
        if (!saved || teamId == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "队伍创建失败");
        }

        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(teamId);
        userTeam.setUserId(loginUser.getId());
        userTeam.setJoinTime(new Date());
        userTeamService.save(userTeam);
        return teamId;
    }

    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery, HttpServletRequest request, boolean allowPrivate) {
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        if (teamQuery != null) {
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                teamQueryWrapper.eq("id", id);
            }

            List<Long> idList = teamQuery.getIdList();
            if (!CollectionUtils.isEmpty(idList)) {
                teamQueryWrapper.in("id", idList);
            }

            String name = teamQuery.getName();
            if (name != null && !name.isBlank()) {
                teamQueryWrapper.like("name", name);
            }

            String description = teamQuery.getDescription();
            if (description != null && !description.isBlank()) {
                teamQueryWrapper.like("description", description);
            }

            String searchTeam = teamQuery.getSearchTerm();
            if (searchTeam != null && !searchTeam.isBlank()) {
                // 名称或描述中包含关键词
                teamQueryWrapper.and(qw -> qw.like("name", searchTeam).or().like("description", searchTeam));
            }

            Integer maxNum = teamQuery.getMaxNum();
            // 小于等于
            if (maxNum != null && maxNum > 0) {
                teamQueryWrapper.le("maxNum", maxNum);
            }

            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByStatus(status);
            statusEnum = statusEnum == null ? TeamStatusEnum.PUBLIC : statusEnum; // 默认查询公开
            // 队伍是私密的 且 查询用户不是管理员  - 全部满足会报错
            if (statusEnum.equals(TeamStatusEnum.PRIVATE) && !userService.isAdmin(request)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            } else {
                if (allowPrivate) {
                    // 全部允许
                } else {
                    teamQueryWrapper.eq("status", statusEnum.getStatus());
                }
            }

            Long founderId = teamQuery.getFounderId();
            if (founderId != null && founderId > 0) {
                teamQueryWrapper.eq("founderId", founderId);
            }
        }
        teamQueryWrapper.and(qw -> qw.isNull("expireTime").or().ge("expireTime", new Date()));
        // 查询到队伍列表结果
        List<Team> teamList = this.list(teamQueryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        // 关联查询
        for (Team team : teamList) {
            Long founderId = team.getFounderId();
            User founder = userService.getById(founderId);
            if (founder != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(founder, userVO);

                TeamUserVO teamUserVO = new TeamUserVO();
                BeanUtils.copyProperties(team, teamUserVO);
                teamUserVO.setFounderUser(userVO);
                teamUserVOList.add(teamUserVO);
            }
        }
        return teamUserVOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        Long id = teamUpdateRequest.getId();
        Team oldTeam = getTeamById(id);

        // 只有管理员和创建人可以修改
        if (!userService.isAdmin(request) && userService.getCurLoginUser(request).getId() != oldTeam.getFounderId()) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        TeamStatusEnum newStatusEnum = TeamStatusEnum.getEnumByStatus(teamUpdateRequest.getStatus());
        TeamStatusEnum oldStatusEnum = TeamStatusEnum.getEnumByStatus(oldTeam.getStatus());
        if (newStatusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "状态参数错误");
        }
        if (newStatusEnum != oldStatusEnum && newStatusEnum.equals(TeamStatusEnum.ENCRYPTED)) {
            if (StringUtils.isBlank(teamUpdateRequest.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码缺失");
            }
        }

        // update
        Team newTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, newTeam);
        return this.updateById(newTeam);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getCurLoginUser(request);
        long userId = loginUser.getId();

        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId", userId);
        long joinedTeams = userTeamService.count(userTeamQueryWrapper);
        if (joinedTeams > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多加入5个队伍");
        }

        Long teamId = teamJoinRequest.getTeamId();
        Team team = getTeamById(teamId);

        if (team.getExpireTime() != null && team.getExpireTime().before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍过期");
        }
        long teamMembers = getTeamMembers(teamId);
        if (teamMembers >= team.getMaxNum()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数已满");
        }

        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByStatus(team.getStatus());
        if (TeamStatusEnum.PRIVATE.equals(teamStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能加入私有队伍");
        }

        String password = teamJoinRequest.getPassword();
        if (TeamStatusEnum.ENCRYPTED.equals(teamStatusEnum)) {
            if (StringUtils.isBlank(password) || !password.equals(team.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
        }

        userTeamQueryWrapper.clear();
        userTeamQueryWrapper.eq("teamId", teamId);
        userTeamQueryWrapper.eq("userId", userId);
        long userCountInTeam = userTeamService.count(userTeamQueryWrapper);
        if (userCountInTeam > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已加入该队伍");
        }

        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());

        return userTeamService.save(userTeam);
    }

    /**
     * 获取队伍当前人数
     *
     * @param teamId 队伍id
     * @return 队伍人数
     */
    private long getTeamMembers(Long teamId) {
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        return userTeamService.count(userTeamQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean exitTeam(Long teamId, HttpServletRequest request) {
        Team team = getTeamById(teamId);

        User loginUser = userService.getCurLoginUser(request);
        Long userId = loginUser.getId();
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        userTeamQueryWrapper.eq("userId", userId);
        long userCountInTeam = userTeamService.count(userTeamQueryWrapper);
        if (userCountInTeam == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未加入队伍");
        }

        long teamMembers = getTeamMembers(teamId);
        // 队伍只剩一人，直接解散
        if (teamMembers == 1) {
            boolean removed = userTeamService.remove(userTeamQueryWrapper);
            boolean removed1 = this.removeById(teamId);
            return removed && removed1;
        } else {
            // 创建人退出，约定更改创建人
            if (team.getFounderId().equals(userId)) {
                QueryWrapper<UserTeam> userTeamQueryWrapper2 = new QueryWrapper<>();
                userTeamQueryWrapper2.eq("teamId", teamId);
                userTeamQueryWrapper2.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper2);
                UserTeam nextUserTeam = userTeamList.get(1);
                Long newFounderId = nextUserTeam.getUserId();

                Team newTeam = new Team();
                newTeam.setId(teamId);
                newTeam.setFounderId(newFounderId);
                boolean updated = this.updateById(newTeam);
                boolean removed = userTeamService.remove(userTeamQueryWrapper);
                return updated && removed;
            } else {
                return userTeamService.remove(userTeamQueryWrapper);
            }
        }
    }

    /**
     * 根据id从数据库中获取队伍，不存在自动抛出异常
     *
     * @param teamId 队伍id，会自动检查是否符合要求
     * @return
     */
    private Team getTeamById(Long teamId) {
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "team id 异常");
        }

        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "不存在队伍");
        }
        return team;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(Long teamId, HttpServletRequest request) {
        Team team = getTeamById(teamId);

        User loginUser = userService.getCurLoginUser(request);
        // 是否是队伍创建人
        if (!team.getFounderId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        boolean removed = userTeamService.remove(userTeamQueryWrapper);

        boolean removed1 = removeById(teamId);
        return removed && removed1;
    }
}




