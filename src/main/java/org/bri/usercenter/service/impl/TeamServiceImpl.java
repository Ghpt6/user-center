package org.bri.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.bri.usercenter.common.BusinessException;
import org.bri.usercenter.common.ErrorCode;
import org.bri.usercenter.model.domain.Team;
import org.bri.usercenter.model.domain.User;
import org.bri.usercenter.model.domain.UserTeam;
import org.bri.usercenter.model.enums.TeamStatusEnum;
import org.bri.usercenter.service.TeamService;
import org.bri.usercenter.mapper.TeamMapper;
import org.bri.usercenter.service.UserService;
import org.bri.usercenter.service.UserTeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
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
    @Transactional(rollbackFor = BusinessException.class)
    public long addTeam(Team team, HttpServletRequest request) {
        if(team == null) {
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
        if(teamName.isBlank() || teamName.length() > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍名称错误");
        }

        // 描述<=1024
        String description = team.getDescription();
        if( ! description.isBlank() && description.length() > 1024) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "描述<=1024");
        }

        int status = Optional.ofNullable(team.getStatus()).orElse(0);

        TeamStatusEnum enumByStatus = TeamStatusEnum.getEnumByStatus(status);
        if(enumByStatus == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "param status error");
        }

        // 加密判断
        String password = team.getPassword();
        if(enumByStatus.equals(TeamStatusEnum.ENCRYPTED) && password.isBlank() || password.length() > 32) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "param password error");
        }

        Date expireTime = team.getExpireTime();
        if(expireTime == null || expireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "时间错乱");
        }

        QueryWrapper<Team> qw = new QueryWrapper<>();
        qw.eq("founderId", loginUser.getId());
        team.setFounderId(loginUser.getId()); // 防止恶意修改创建人id
        long hasTeamNum = this.count(qw);
        if(hasTeamNum >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍创建太多");
        }

        // 事务级插入
        team.setId(null);
        boolean saved = this.save(team);
        Long teamId = team.getId();
        if(!saved || teamId == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "队伍创建失败");
        }

        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(teamId);
        userTeam.setUserId(loginUser.getId());
        userTeam.setJoinTime(new Date());
        userTeamService.save(userTeam);
        return teamId;
    }
}




