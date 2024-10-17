package com.springboot.publicplace.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamBoardDetailResponseDto {
    private Long teamBoardId;
    private String content;
    private String Image;
    private String userName;
    private String profileImg;
    private String matchLocation;
    private Long longitude;
    private Long latitude;
    private LocalDateTime createdDate;
    private List<TeamBoardCommentResponseDto> comments;
}
