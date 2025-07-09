package com.may.paperleap.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.may.paperleap.mapper.TeamMapper;
import com.may.paperleap.model.domain.Team;
import com.may.paperleap.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author May20242
 */
@SpringBootTest
public class TeamTest {

    @Resource
    private TeamService teamService;

    @Resource
    private TeamMapper teamMapper;
    @Test
    public void listTeamsTest() {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", 2);
        List<Team> teamList = teamMapper.selectList(queryWrapper);
        for(Team team : teamList) {
            List<User> users = teamMapper.selectTeamUsers(team.getId());
            System.out.println(users);
        }
    }

}
