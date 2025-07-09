package com.may.paperleap.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.may.paperleap.model.domain.UserTeam;
import com.may.paperleap.service.UserTeamService;
import com.may.paperleap.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author 28789
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2025-07-09 11:44:32
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {

}




