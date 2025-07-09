package com.may.paperleap.service;

import com.may.paperleap.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.may.paperleap.model.domain.User;
import com.may.paperleap.model.dto.TeamQuery;
import com.may.paperleap.model.request.TeamJoinRequest;
import com.may.paperleap.model.request.TeamUpdateRequest;
import com.may.paperleap.model.vo.TeamUserVo;

import java.util.List;

/**
* @author 28789
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2025-07-09 11:42:20
*/
public interface TeamService extends IService<Team> {
    long addTeam(Team team, User userLogin);

    List<TeamUserVo> listTeams(TeamQuery teamQuery, Boolean isAdmin);

    Boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User userLogin);

    Boolean joinTeam(TeamJoinRequest teamJoinRequest, User userLogin);

    Boolean exitTeam(Long id, User userLogin);

    Boolean disband(Long id, User userLogin);
}
