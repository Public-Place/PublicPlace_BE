package com.springboot.publicplace.service;

import com.springboot.publicplace.dto.ResultDto;
import com.springboot.publicplace.dto.request.TeamRequestDto;
import com.springboot.publicplace.dto.response.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface TeamService{
    ResultDto createTeam(HttpServletRequest request, TeamRequestDto teamRequestDto);

    ResultDto updateTeam(Long teamId, HttpServletRequest request, TeamRequestDto teamRequestDto);

    TeamResponseDto getTeamInfo(Long teamId);

    List<TeamRandomListDto> getTeamList(HttpServletRequest servletRequest);

    List<GPTTeamListDto> getGptTeamList(HttpServletRequest servletRequest);

    List<TeamListResponseDto> getTeamsByCriteria(String sortBy, String teamName);

    ResultDto checkTeamName (String teamName);

    TeamRoleResponseDto checkTeamRole(HttpServletRequest servletRequest, Long teamId);
}
