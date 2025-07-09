package com.may.paperleap.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.may.paperleap.model.domain.Team;
import com.may.paperleap.service.TeamService;
import com.may.paperleap.mapper.TeamMapper;
import org.springframework.stereotype.Service;

/**
* @author 28789
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2025-07-09 11:42:20
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

}




