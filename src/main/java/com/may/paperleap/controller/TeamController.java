package com.may.paperleap.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.may.paperleap.common.BaseResponse;
import com.may.paperleap.common.CodeError;
import com.may.paperleap.common.ResultUtils;
import com.may.paperleap.exception.BusinessException;
import com.may.paperleap.model.domain.Team;
import com.may.paperleap.model.domain.User;
import com.may.paperleap.model.domain.UserTeam;
import com.may.paperleap.model.dto.TeamQuery;
import com.may.paperleap.model.request.TeamAddRequest;
import com.may.paperleap.model.request.TeamJoinRequest;
import com.may.paperleap.model.request.TeamUpdateRequest;
import com.may.paperleap.model.vo.TeamUserVo;
import com.may.paperleap.service.TeamService;
import com.may.paperleap.service.UserService;
import com.may.paperleap.service.UserTeamService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jodd.bean.BeanUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @author May20242
 */
@RestController
@RequestMapping("/team")
@Tag(name = "队伍管理", description = "队伍相关接口")
public class TeamController {

    @Resource
    private TeamService teamService;

    @Resource
    private UserService userService;

    @Resource
    private UserTeamService userTeamService;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(CodeError.PARAMS_ERROR);
        }
        User userLogin = userService.getCurrentUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        long result = teamService.addTeam(team, userLogin);

        return ResultUtils.success(CodeError.SUCCESS, result);
    }

    @GetMapping("/list")
    public BaseResponse<List<TeamUserVo>> listTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if(teamQuery == null) {
            throw new BusinessException(CodeError.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        List<TeamUserVo> teamUserVos = teamService.listTeams(teamQuery, isAdmin);
        return ResultUtils.success(CodeError.SUCCESS, teamUserVos);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if(teamUpdateRequest == null) {
            throw new BusinessException(CodeError.PARAMS_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);
        Boolean result = teamService.updateTeam(teamUpdateRequest, currentUser);

        return ResultUtils.success(CodeError.SUCCESS, result);
    }

    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if(teamJoinRequest == null) {
            throw new BusinessException(CodeError.PARAMS_ERROR);
        }
        User userLogin = userService.getCurrentUser(request);
        Boolean result = teamService.joinTeam(teamJoinRequest, userLogin);

        return ResultUtils.success(CodeError.SUCCESS, result);
    }

    @GetMapping("/exit")
    public BaseResponse<Boolean> exitTeam(Long id, HttpServletRequest request){
        if(id == null || id <= 0) {
            throw new BusinessException(CodeError.PARAMS_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);
        Boolean result = teamService.exitTeam(id, currentUser);

        return ResultUtils.success(CodeError.SUCCESS, result);
    }

    @GetMapping("/disband")
    public BaseResponse<Boolean> disbandTeam(Long id, HttpServletRequest request) {
        if(id == null || id <= 0) {
            throw new BusinessException(CodeError.PARAMS_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);
        Boolean result = teamService.disband(id, currentUser);

        return ResultUtils.success(CodeError.SUCCESS, result);
    }

    @GetMapping("/hasJoin")
    public BaseResponse<List<TeamUserVo>> hasJoinTeams(HttpServletRequest request) {
        User currentUser = userService.getCurrentUser(request);
        Long userId = currentUser.getId();
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        List<TeamUserVo> result = new ArrayList<>();
        for(UserTeam userTeam : userTeamList) {
            Long teamId = userTeam.getTeamId();
            TeamQuery teamQuery = new TeamQuery();
            teamQuery.setId(teamId);
            List<TeamUserVo> teamUserVos = teamService.listTeams(teamQuery, true);
            if(!CollectionUtils.isEmpty(teamUserVos)) {
                result.add(teamUserVos.get(0));
            }
        }

        return ResultUtils.success(CodeError.SUCCESS, result);
    }

}
