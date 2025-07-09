package com.may.paperleap.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.may.paperleap.common.CodeError;
import com.may.paperleap.constant.UserConstant;
import com.may.paperleap.enums.TeamStatusEnum;
import com.may.paperleap.exception.BusinessException;
import com.may.paperleap.model.domain.Team;
import com.may.paperleap.model.domain.User;
import com.may.paperleap.model.domain.UserTeam;
import com.may.paperleap.model.dto.TeamQuery;
import com.may.paperleap.model.request.TeamJoinRequest;
import com.may.paperleap.model.request.TeamUpdateRequest;
import com.may.paperleap.model.vo.TeamUserVo;
import com.may.paperleap.model.vo.UserVo;
import com.may.paperleap.service.TeamService;
import com.may.paperleap.mapper.TeamMapper;
import com.may.paperleap.service.UserService;
import com.may.paperleap.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
* @author 28789
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2025-07-09 11:42:20
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private TeamMapper teamMapper;

    @Resource
    private UserService userService;

    /**
     * 创建队伍
     * @param team
     * @param userLogin
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public long addTeam(Team team, User userLogin) {
//      - 请求参数是否为空
        if (team == null) {
            throw new BusinessException(CodeError.NULL_ERROR);
        }
//      - 是否登录，未登录不允许创建
        if (userLogin == null) {
            throw new BusinessException(CodeError.NOT_LOGIN);
        }
//      - 校验信息
//                - 队伍最大人数 > 1 且 ≤ 20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum <= 1 || maxNum > 20) {
            throw new BusinessException(CodeError.PARAMS_ERROR, "队伍最大人数错误");
        }
//                - 队伍标题 ≤ 20
        String teamName = team.getName();
        if (StringUtils.isBlank(teamName) || teamName.length() > 20) {
            throw new BusinessException(CodeError.PARAMS_ERROR, "队伍名称不满足要求");
        }
//                - 描述 ≤ 512
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BusinessException(CodeError.PARAMS_ERROR, "描述不满足要求");
        }
//                - status是否公开，不传默认为0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getTeamStatusEnum(status);
        if(teamStatusEnum == null) {
            throw new BusinessException(CodeError.PARAMS_ERROR, "队伍状态不满足要求");
        }
//        - 如果status是加密状态（2），一定要有密码，且密码 ≤ 32
        if (TeamStatusEnum.SECRET.equals(teamStatusEnum)) {
            String password = team.getPassword();
            if(StringUtils.isBlank(password) || password.length() > 32) {
                throw new BusinessException(CodeError.PARAMS_ERROR, "密码不符合要求");
            }
        }
//                - 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if(expireTime == null || new Date().after(expireTime)) {
            throw new BusinessException(CodeError.PARAMS_ERROR, "超时时间不符合要求");
        }
//                - 用户最多创建5个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userLogin.getId());
        int count = this.count(queryWrapper);
        if (count >= 5) {
            throw new BusinessException(CodeError.PARAMS_ERROR, "用户创建队伍数量达到上限");
        }
//                - 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userLogin.getId());
        boolean save = this.save(team);
        if (!save) {
            throw new BusinessException(CodeError.PARAMS_ERROR, "创建队伍失败");
        }
//                - 插入用户 ⇒ 队伍 关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(team.getUserId());
        userTeam.setTeamId(team.getId());
        userTeam.setJoinTime(new Date());
        boolean result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(CodeError.PARAMS_ERROR, "创建队伍失败");
        }
        return team.getId();
    }

    /**
     * 查询队伍信息（包括队伍所包含的用户信息）
     * @param teamQuery
     * @return
     */
    @Override
    public List<TeamUserVo> listTeams(TeamQuery teamQuery, Boolean isAdmin) {

        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();

        if(teamQuery != null) {
            Long id = teamQuery.getId();
            if(id != null && id > 0) {
                queryWrapper.eq("id", id);
            }

            String name = teamQuery.getName();
            if(StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }
            Integer maxNum = teamQuery.getMaxNum();
            if(maxNum != null && maxNum > 0) {
                queryWrapper.eq("maxNum", maxNum);
            }
            String description = teamQuery.getDescription();
            if(StringUtils.isNotBlank(description)) {
                queryWrapper.like("description", description);
            }
            String searchText = teamQuery.getSearchText();
            if(StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
            }
            Long userId = teamQuery.getUserId();
            if(userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }
            Integer status = Optional.ofNullable(teamQuery.getStatus()).orElse(0);
            TeamStatusEnum teamStatusEnum = TeamStatusEnum.getTeamStatusEnum(status);
            if (teamStatusEnum == null) {
                teamStatusEnum = TeamStatusEnum.PUBLIC;
            }
            if (!isAdmin && teamStatusEnum != TeamStatusEnum.PUBLIC) {
                throw new BusinessException(CodeError.NO_AUTH);
            }
            queryWrapper.eq("status", teamStatusEnum.getValue());

        }
        // 不展示已过期队伍
        queryWrapper.and(qw -> qw.isNull("expireTime").or().gt("expireTime", new Date()));
        List<Team> teamList = this.list(queryWrapper);
        if(CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        List<TeamUserVo> teamUserVos = new ArrayList<>();
        for(Team team : teamList) {
            Long teamId = team.getId();
            if(teamId == null) continue;
            List<User> users = teamMapper.selectTeamUsers(teamId);
            List<UserVo> userVoList = new ArrayList<>();
            for(User user : users) {
                UserVo userVo = new UserVo();
                BeanUtils.copyProperties(user, userVo);
                userVoList.add(userVo);
            }
            TeamUserVo teamUserVo = new TeamUserVo();
            BeanUtils.copyProperties(team, teamUserVo);
            teamUserVo.setUserVoList(userVoList);
            teamUserVos.add(teamUserVo);
        }
        return teamUserVos;
    }

    /**
     * 修改队伍信息
     * @param teamUpdateRequest
     * @param userLogin
     * @return
     */
    @Override
    public Boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User userLogin) {
        //- 判断请求参数是否为空
        if(teamUpdateRequest == null) {
            throw new BusinessException(CodeError.PARAMS_ERROR);
        }
        //- 查询队伍是否存在
        Long teamId = teamUpdateRequest.getId();
        if(teamId == null || teamId <= 0) {
            throw new BusinessException(CodeError.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if(team == null) {
            throw new BusinessException(CodeError.NULL_ERROR);
        }
        //- 只有管理员或者队伍的创建者可以修改
        if(team.getUserId() != userLogin.getId() && userLogin.getUserRole() != UserConstant.ADMIN_ROLE) {
            throw new BusinessException(CodeError.NO_AUTH);
        }
        //- 如果用户传入的新值和旧值一致，就不用修改了
        //- 如果队伍状态修改为加密，必须要有密码
        Integer status = Optional.ofNullable(teamUpdateRequest.getStatus()).orElse(0);
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getTeamStatusEnum(status);
        if(TeamStatusEnum.SECRET.equals(teamStatusEnum)) {
            String password = teamUpdateRequest.getPassword();
            if(StringUtils.isBlank(password)) {
                throw new BusinessException(CodeError.PARAMS_ERROR, "请设置密码");
            }
        }
        //- 更新成功
        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, updateTeam);
        boolean result = this.updateById(updateTeam);
        return result;
    }

    /**
     * 用户加入队伍
     * @param id
     * @param userLogin
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Boolean joinTeam(TeamJoinRequest teamJoinRequest, User userLogin) {
        Long id = teamJoinRequest.getId();
        if(id == null || id <= 0) {
            throw new BusinessException(CodeError.PARAMS_ERROR);
        }
        //- 用户最多加入5个队伍
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userLogin.getId());
        int count = userTeamService.count(queryWrapper);
        if(count >= 5) {
            throw new BusinessException(CodeError.PARAMS_ERROR, "用户加入队伍已达上限");
        }
        //- 队伍必须存在，只能加入未满、未过期的队伍
        Team team = this.getById(id);
        if(team == null) {
            throw new BusinessException(CodeError.PARAMS_ERROR, "队伍不存在");
        }
        if(team.getCurrentNum() == team.getMaxNum()) {
            throw new BusinessException(CodeError.PARAMS_ERROR, "队伍人数已满");
        }
        Date expireTime = team.getExpireTime();
        if(new Date().after(expireTime)) {
            throw new BusinessException(CodeError.PARAMS_ERROR, "队伍已过期");
        }
        //- 不能加入自己的队伍，不能重复加入已加入的队伍
        if(userLogin.getId().equals(team.getUserId())) {
            throw new BusinessException(CodeError.PARAMS_ERROR);
        }
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userLogin.getId());
        queryWrapper.eq("teamId", id);
        int userJoinCount = userTeamService.count(queryWrapper);
        if(userJoinCount > 0) {
            throw new BusinessException(CodeError.PARAMS_ERROR, "不能重复加入");
        }
        //- 禁止加入私有的队伍
        Integer status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getTeamStatusEnum(status);
        if(TeamStatusEnum.PRIVATE.equals(teamStatusEnum)) {
            throw new BusinessException(CodeError.PARAMS_ERROR, "禁止加入私有的队伍");
        }
        //- 如果加入的队伍是加密的，密码必须匹配
        if(TeamStatusEnum.SECRET.equals(teamStatusEnum)) {
            String password = teamJoinRequest.getPassword();
            if(StringUtils.isBlank(password) || !password.equals(team.getPassword())) {
                throw new BusinessException(CodeError.PARAMS_ERROR, "密码错误");
            }
        }
        //- 新增队伍-用户关联信息
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userLogin.getId());
        userTeam.setTeamId(id);
        userTeam.setJoinTime(new Date());
        boolean save = userTeamService.save(userTeam);
        //-当前加入的队伍人数 + 1
        Integer currentNum = team.getCurrentNum();
        currentNum++;
        team.setCurrentNum(currentNum);
        this.updateById(team);

        return save;
    }

    /**
     * 用户退出队伍
     * @param id
     * @param userLogin
     * @return
     */
    @Override
    public Boolean exitTeam(Long id, User userLogin) {
        //- 校验请求参数
        if(id == null || id <= 0) {
            throw new BusinessException(CodeError.PARAMS_ERROR);
        }
        //- 校验队伍是否存在
        Team team = this.getById(id);
        if(team == null) {
            throw new BusinessException(CodeError.PARAMS_ERROR);
        }
        //- 校验用户是否加入队伍
        Long userId = userLogin.getId();
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("teamId", id);
        int count = userTeamService.count(queryWrapper);
        if(count != 1) {
            throw new BusinessException(CodeError.PARAMS_ERROR, "用户未加入队伍");
        }
        //- 如果队伍：
        //    - 只剩一人，直接解散
        Integer currentNum = team.getCurrentNum();
        if(currentNum == 1) {
            boolean b = this.removeById(id);
            if(!b) {
                throw new BusinessException(CodeError.SYSTEM_ERROR, "删除队伍失败");
            }
        }else {
            //    - 还有其他人
            //        - 如果是队长，权限转移给第二加入队伍的用户——先来后到（只用取id最小的两条数据)
            if(team.getUserId().equals(userId)) {
                queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("teamId", id);
                queryWrapper.orderByAsc("joinTime").last("limit 2");
                List<UserTeam> list = userTeamService.list(queryWrapper);
                UserTeam userTeam = list.get(1);
                team.setUserId(userTeam.getUserId());
            }
            team.setCurrentNum(team.getCurrentNum() - 1);
            boolean save = this.updateById(team);
            if(!save) {
                throw new BusinessException(CodeError.SYSTEM_ERROR, "数据库异常");
            }
        }
        //        - 不是队长，自己退出队伍
        boolean remove = userTeamService.remove(queryWrapper);

        return remove;
    }

    /**
     * 解散队伍
     * @param id
     * @param userLogin
     * @return
     */
    @Override
    public Boolean disband(Long id, User userLogin) {
        //- 校验请求参数
        if(id == null || id <= 0) {
            throw new BusinessException(CodeError.PARAMS_ERROR);
        }
        //- 校验队伍是否存在
        Team team = this.getById(id);
        if(team == null) {
            throw new BusinessException(CodeError.PARAMS_ERROR, "队伍不存在");
        }
        //- 校验用户是不是队伍的队长
        Long userId = userLogin.getId();
        if(!userId.equals(team.getUserId())) {
            throw new BusinessException(CodeError.NO_AUTH, "只有队长才能解散队伍");
        }
        //- 移除所有加入队伍的关联信息
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", id);
        boolean remove = userTeamService.remove(queryWrapper);
        if(!remove) {
            throw new BusinessException(CodeError.SYSTEM_ERROR);
        }
        //- 删除队伍
        boolean result = this.removeById(id);

        return result;
    }
}




