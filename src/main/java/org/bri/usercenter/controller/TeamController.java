package org.bri.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.bri.usercenter.common.BaseResponse;
import org.bri.usercenter.common.BusinessException;
import org.bri.usercenter.common.ErrorCode;
import org.bri.usercenter.model.domain.Team;
import org.bri.usercenter.model.dto.TeamQuery;
import org.bri.usercenter.service.TeamService;
import org.bri.usercenter.service.UserService;
import org.bri.usercenter.utils.ResponseUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/team")
public class TeamController {
    @Autowired
    private UserService userService;

    @Autowired
    private TeamService teamService;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody Team team) {
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean saved = teamService.save(team);
        if (!saved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加失败");
        }
        return ResponseUtils.success(team.getId());
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody Long teamId) {
        if (teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean removed = teamService.removeById(teamId);
        if (!removed) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResponseUtils.success(true);
    }

    @PostMapping("/update")
    public BaseResponse<Long> updateTeam(@RequestBody Team team) {
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean updated = teamService.updateById(team);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
        }
        return ResponseUtils.success(team.getId());
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
    public BaseResponse<List<Team>> listTeam(@RequestParam TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery, team);
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<Team>(team);

        List<Team> teamList = teamService.list(teamQueryWrapper);
        return ResponseUtils.success(teamList);
    }

}
