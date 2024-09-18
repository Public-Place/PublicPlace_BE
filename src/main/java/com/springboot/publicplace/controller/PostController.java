package com.springboot.publicplace.controller;

import com.springboot.publicplace.dto.ResultDto;
import com.springboot.publicplace.dto.request.CommentRequestDto;
import com.springboot.publicplace.dto.request.PostRequestDto;
import com.springboot.publicplace.dto.response.PostDetailResponseDto;
import com.springboot.publicplace.dto.response.PostListResponseDto;
import com.springboot.publicplace.service.PostService;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/v1/post")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping("/createPost")
    public ResponseEntity<ResultDto> createPost(@RequestBody PostRequestDto requestDto,
                                                HttpServletRequest servletRequest) {
        ResultDto resultDto = postService.createPost(servletRequest, requestDto);
        return ResponseEntity.status(HttpStatus.OK).body(resultDto);
    }

    @PutMapping("/updatePost/{postId}")
    public ResponseEntity<ResultDto> updatePost(@PathVariable Long postId,
                                                HttpServletRequest servletRequest,
                                                @RequestBody PostRequestDto requestDto) {
        ResultDto resultDto = postService.updatePost(postId, servletRequest, requestDto);
        return ResponseEntity.status(HttpStatus.OK).body(resultDto);
    }

    @DeleteMapping("/deletePost/{postId}")
    public ResponseEntity<ResultDto> deletePost(@PathVariable Long postId,
                                                HttpServletRequest servletRequest) {
        ResultDto resultDto = postService.deletePost(postId, servletRequest);
        return ResponseEntity.status(HttpStatus.OK).body(resultDto);
    }

    @GetMapping("/getPostsByCategory")
    public ResponseEntity<List<PostListResponseDto>> getPostsByCategory(
            @ApiParam(value = "카테고리", allowableValues = "전체, 자유, 해외축구, 국내축구, 직관모임", required = true)
            @RequestParam(defaultValue = "전체") String category,

            @RequestParam(defaultValue = "1") int page,

            @ApiParam(value = "정렬 기준", allowableValues = "views, likes, createdAt", required = true)
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        List<PostListResponseDto> postListResponseDtos = postService.getPostsByCategory(category, page - 1, sortBy);
        return ResponseEntity.status(HttpStatus.OK).body(postListResponseDtos);
    }

    @GetMapping("/getPostDetail/{postId}")
    public ResponseEntity<PostDetailResponseDto> getPostDetail(@PathVariable Long postId,
                                                               HttpServletRequest servletRequest) {
        PostDetailResponseDto postDetailResponseDto = postService.getPostDetails(postId, servletRequest);
        return ResponseEntity.status(HttpStatus.OK).body(postDetailResponseDto);
    }

    @PostMapping("/comments")
    public ResponseEntity<ResultDto> createComment(HttpServletRequest servletRequest,
                                                   CommentRequestDto commentRequestDto) {
        ResultDto resultDto = postService.createComment(servletRequest, commentRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(resultDto);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ResultDto> deleteComment(@PathVariable Long commentId,
                                                   HttpServletRequest servletRequest) {
        ResultDto resultDto = postService.deleteComment(commentId, servletRequest);
        return ResponseEntity.status(HttpStatus.OK).body(resultDto);
    }

    @PostMapping("/like/{postId}")
    public ResponseEntity<ResultDto> toggleLike(@PathVariable Long postId, HttpServletRequest servletRequest) {
        ResultDto resultDto = postService.toggleLike(postId, servletRequest);
        return ResponseEntity.status(HttpStatus.OK).body(resultDto);
    }

}
