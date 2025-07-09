package com.may.paperleap.mapper;

import com.may.paperleap.model.domain.Team;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.may.paperleap.model.domain.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
* @author 28789
* @description 针对表【team(队伍)】的数据库操作Mapper
* @createDate 2025-07-09 11:42:20
* @Entity generator.domain.Team
*/
public interface TeamMapper extends BaseMapper<Team> {
    @Select("select u.* from team t left join user_team ut on t.id = ut.teamId left join user u on ut.userId = u.id where t.id = #{teamId};")
    List<User> selectTeamUsers(@Param("teamId") Long teamId);
}




