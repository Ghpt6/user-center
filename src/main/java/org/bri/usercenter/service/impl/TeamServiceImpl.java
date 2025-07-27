package org.bri.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.bri.usercenter.model.domain.Team;
import org.bri.usercenter.service.TeamService;
import org.bri.usercenter.mapper.TeamMapper;
import org.springframework.stereotype.Service;

/**
* @author ThinkPad
* @description 针对表【team(队伍表)】的数据库操作Service实现
* @createDate 2025-07-25 14:59:16
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService {

}




