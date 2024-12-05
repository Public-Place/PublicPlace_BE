package com.springboot.publicplace.pieceoftravel;

import com.springboot.publicplace.dto.ResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class FeedBackController {

    private final FeedBackService feedBackService;

    @PostMapping("/feedback")
    public ResponseEntity<ResultDto> provideFeedback(@RequestBody Map<String, Object> itineraryData) {
        if (itineraryData == null || itineraryData.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResultDto.builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .msg("일정 데이터가 비어 있습니다. 유효한 데이터를 제공해주세요.")
                            .success(false)
                            .build());
        }

        // 피드백 생성
        ResultDto feedback = feedBackService.generateFeedback(itineraryData);

        return ResponseEntity.ok(feedback);
    }
}
