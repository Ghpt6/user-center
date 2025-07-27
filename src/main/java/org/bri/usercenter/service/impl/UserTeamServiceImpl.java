package org.bri.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.bri.usercenter.model.domain.UserTeam;
import org.bri.usercenter.service.UserTeamService;
import org.bri.usercenter.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author ThinkPad
* @description 针对表【user_team(用户队伍关系表)】的数据库操作Service实现
* @createDate 2025-07-25 15:00:24
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {

}




