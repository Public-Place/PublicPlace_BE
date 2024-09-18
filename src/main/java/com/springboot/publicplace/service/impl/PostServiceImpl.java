package com.springboot.publicplace.service.impl;

import com.springboot.publicplace.config.security.JwtTokenProvider;
import com.springboot.publicplace.dto.ResultDto;
import com.springboot.publicplace.dto.request.CommentRequestDto;
import com.springboot.publicplace.dto.request.PostRequestDto;
import com.springboot.publicplace.dto.response.CommentResponseDto;
import com.springboot.publicplace.dto.response.PostCommentResponseDto;
import com.springboot.publicplace.dto.response.PostDetailResponseDto;
import com.springboot.publicplace.dto.response.PostListResponseDto;
import com.springboot.publicplace.entity.Comment;
import com.springboot.publicplace.entity.LikePost;
import com.springboot.publicplace.entity.Post;
import com.springboot.publicplace.entity.User;
import com.springboot.publicplace.repository.CommentRepository;
import com.springboot.publicplace.repository.LikePostRepository;
import com.springboot.publicplace.repository.PostRepository;
import com.springboot.publicplace.repository.UserRepository;
import com.springboot.publicplace.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikePostRepository likePostRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public ResultDto createPost(HttpServletRequest servletRequest, PostRequestDto postRequestDto) {
        // ResultDto 객체 초기화
        ResultDto resultDto = new ResultDto();

        try {
            // 토큰에서 사용자 이메일 추출
            String token = jwtTokenProvider.resolveToken(servletRequest);
            String email = jwtTokenProvider.getUsername(token);
            User user = userRepository.findByEmail(email);

            // 토큰 유효성 검증
            if (jwtTokenProvider.validationToken(token)) {
                // Post 객체 생성 및 데이터 설정
                Post post = new Post();
                post.setTitle(postRequestDto.getTitle());
                post.setContent(postRequestDto.getContent());
                post.setCategory(postRequestDto.getCategory());
                post.setPostImg(postRequestDto.getPostImg());
                post.setUser(user); // 게시글 작성자 설정

                // 게시글 저장
                postRepository.save(post);

                // 성공 시 결과 설정
                resultDto.setSuccess(true);
                resultDto.setMsg("게시글이 성공적으로 작성되었습니다.");
            }
        } catch (Exception e) {
            // 예외 발생 시 실패 결과 설정
            resultDto.setSuccess(false);
            resultDto.setMsg("게시글 작성에 실패했습니다: " + e.getMessage());
        }
        return resultDto;
    }

    @Override
    public ResultDto updatePost(Long postId, HttpServletRequest servletRequest, PostRequestDto postRequestDto) {
        String token = jwtTokenProvider.resolveToken(servletRequest);
        String email = jwtTokenProvider.getUsername(token);
        User user = userRepository.findByEmail(email);

        ResultDto resultDto = new ResultDto();

        if(jwtTokenProvider.validationToken(token)){
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));
            if (post.getUser().equals(user)) {
                post.setTitle(postRequestDto.getTitle());
                post.setContent(postRequestDto.getContent());
                post.setCategory(postRequestDto.getCategory());
                post.setPostImg(postRequestDto.getPostImg());

                // 수정된 게시글 저장
                postRepository.save(post);

                // 성공 시 결과 설정
                resultDto.setSuccess(true);
                resultDto.setMsg("게시글이 성공적으로 수정되었습니다.");
            } else {
                // 작성자가 아닌 경우
                resultDto.setSuccess(false);
                resultDto.setMsg("본인이 작성한 게시글만 수정할 수 있습니다.");
            }
        }
        return resultDto;
    }

    @Override
    public ResultDto deletePost(Long postId, HttpServletRequest servletRequest) {
        String token = jwtTokenProvider.resolveToken(servletRequest);
        String email = jwtTokenProvider.getUsername(token);
        User user = userRepository.findByEmail(email);

        ResultDto resultDto = new ResultDto();

        if(jwtTokenProvider.validationToken(token)){
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

            if (post.getUser().equals(user)) {

                // 게시글 삭제
                postRepository.delete(post);

                // 성공 시 결과 설정
                resultDto.setSuccess(true);
                resultDto.setMsg("게시글이 성공적으로 삭제되었습니다.");
            } else {
                // 작성자가 아닌 경우
                resultDto.setSuccess(false);
                resultDto.setMsg("본인이 작성한 게시글만 삭제할 수 있습니다.");
            }
        }
        return resultDto;
    }

    public List<PostListResponseDto> getPostsByCategory(String category, int page, String sortBy) {

        Sort sort;
        if(sortBy.equals("views")){
            sort = Sort.by(Sort.Direction.DESC, "viewCount").and(Sort.by(Sort.Direction.DESC, "createdAt"));
        } else if (sortBy.equals("likes")) {
            sort = Sort.by(Sort.Direction.DESC,"liked").and(Sort.by(Sort.Direction.DESC, "createdAt"));
        } else {
            sort = Sort.by(Sort.Direction.DESC,"createdAt");
        }
        Pageable pageable = PageRequest.of(page,10, sort);
        Page<Post> posts;

        if ("전체".equals(category)) {
            posts = postRepository.findAll(pageable);
        } else {
            posts = postRepository.findByCategory(category, pageable);
        }
        Page<PostListResponseDto> postPage =posts.map(post -> PostListResponseDto.builder()
                .postId(post.getPostId())
                .category(post.getCategory())
                .title(post.getTitle())
                .authorNickname(post.getUser().getNickname())
                .viewCount(post.getViewCount())
                .commentCount(post.getComments().size())
                .likeCount(post.getLiked())
                .createdDate(post.getCreatedAt())
                .build());
        List<PostListResponseDto> postList = postPage.getContent();
        return postList;
    }

    @Override
    public PostDetailResponseDto getPostDetails(Long postId, HttpServletRequest servletRequest) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);

        PostDetailResponseDto postDetailResponseDto = new PostDetailResponseDto();
        postDetailResponseDto.setTitle(post.getTitle());
        postDetailResponseDto.setCategory(post.getCategory());
        postDetailResponseDto.setProfileImg(post.getUser().getProfileImg());
        postDetailResponseDto.setContent(post.getContent());
        postDetailResponseDto.setPostImg(post.getPostImg());
        postDetailResponseDto.setAuthorNickname(post.getUser().getNickname());
        postDetailResponseDto.setCreatedDate(post.getCreatedAt());
        postDetailResponseDto.setViewCount(post.getViewCount());
        postDetailResponseDto.setLikeCount(post.getLiked());
        postDetailResponseDto.setCommentCount(post.getComments().size());

        List<PostCommentResponseDto> postCommentResponseDtos = post.getComments().stream()
                .map(comment -> {
                    PostCommentResponseDto postCommentResponseDto = new PostCommentResponseDto();
                    postCommentResponseDto.setCommentId(comment.getCommentId());
                    postCommentResponseDto.setProfileImg(comment.getUser().getProfileImg());
                    postCommentResponseDto.setContent(comment.getContent());
                    postCommentResponseDto.setNickname(comment.getUser().getNickname());
                    postCommentResponseDto.setCreatedDate(comment.getCreatedAt());
                    return postCommentResponseDto;
                })
                .collect(Collectors.toList());
        postDetailResponseDto.setComments(postCommentResponseDtos);

        return postDetailResponseDto;
    }

    @Override
    public ResultDto createComment(HttpServletRequest servletRequest, CommentRequestDto commentRequestDto) {
        ResultDto resultDto = new ResultDto();
        try {
            String token = jwtTokenProvider.resolveToken(servletRequest);
            String email = jwtTokenProvider.getUsername(token);
            User user = userRepository.findByEmail(email);

            Post post = postRepository.findById(commentRequestDto.getPostId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

            Comment comment = new Comment();
            comment.setContent(commentRequestDto.getContent());
            comment.setUser(user);
            comment.setPost(post);

            commentRepository.save(comment);

            resultDto.setSuccess(true);
            resultDto.setMsg("댓글이 성공적으로 작성되었습니다");
        } catch (Exception e) {
            resultDto.setSuccess(false);
            resultDto.setMsg("댓글 작성에 실패했습니다: " + e.getMessage());
        }
        return resultDto;
    }

    @Override
    public ResultDto deleteComment(Long commentId, HttpServletRequest servletRequest) {
        ResultDto resultDto = new ResultDto();

        try {
            String token = jwtTokenProvider.resolveToken(servletRequest);
            String email = jwtTokenProvider.getUsername(token);
            User user = userRepository.findByEmail(email);

            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));

            // 댓글 작성자 & 게시글 작성자만 댓글 삭제 가능
            if (comment.getUser().equals(user) || comment.getPost().getUser().equals(user)) {
                commentRepository.delete(comment);
                resultDto.setSuccess(true);
                resultDto.setMsg("댓글이 삭제되었습니다.");
            } else {
                // 권한이 없을 때
                resultDto.setSuccess(false);
                resultDto.setMsg("댓글을 삭제할 권한이 없습니다.");
            }
        } catch (Exception e) {
            resultDto.setSuccess(false);
            resultDto.setMsg("댓글을 삭제하는 도중 에러가 발생함: " + e.getMessage());
        }
        return resultDto;
    }

    @Override
    public ResultDto toggleLike(Long postId, HttpServletRequest servletRequest) {
        String token = jwtTokenProvider.resolveToken(servletRequest);
        String email = jwtTokenProvider.getUsername(token);
        User user = userRepository.findByEmail(email);

        ResultDto resultDto = new ResultDto();

        if (jwtTokenProvider.validationToken(token)) {
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));
            LikePost existingLike = likePostRepository.findByUserAndPost(user, post);

            if (existingLike != null) {
                likePostRepository.delete(existingLike);
                post.setLiked(post.getLiked() - 1);
                resultDto.setMsg("좋아요가 취소되었습니다");
            } else {
                LikePost likePost = new LikePost();
                likePost.setUser(user);
                likePost.setPost(post);
                likePostRepository.save(likePost);
                post.setLiked(post.getLiked() + 1);
                resultDto.setMsg("좋아요가 추가되었습니다.");
            }
            postRepository.save(post);
            resultDto.setSuccess(true);
        } else {
            resultDto.setSuccess(false);
            resultDto.setMsg("로그인을 먼저 해주세요");
        }
        return resultDto;
    }

//    @Override
//    public List<CommentResponseDto> getCommentsByPost(Long postId) {
//        List<Comment> comments = commentRepository.findByPost_PostId(postId);
//
//        List<CommentResponseDto> commentResponseDtoList = comments.stream()
//                .map(comment -> {
//                    CommentResponseDto commentResponseDto = new CommentResponseDto();
//                    commentResponseDto.setProfileImg(comment.getUser().getProfileImg());
//                    commentResponseDto.setNickname(comment.getUser().getNickname());
//                    commentResponseDto.setContent(comment.getContent());
//                    commentResponseDto.setCreatedDate(comment.getCreatedAt());
//                    return commentResponseDto;
//                })
//                .collect(Collectors.toList());
//
//        return commentResponseDtoList;
//    }
}
