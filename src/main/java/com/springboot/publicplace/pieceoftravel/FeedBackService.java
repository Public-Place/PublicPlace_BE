package com.springboot.publicplace.pieceoftravel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.publicplace.dto.ResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FeedBackService {

    @Value("${openai.model}")
    private String model;

    @Value("${openai.api.url}")
    private String apiURL;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ResultDto generateFeedback(Map<String, Object> itineraryData) {
        try {
            String systemPrompt = "사용자는 특정 도시 정보를 입력하고 여행 일정을 요청합니다. 주어진 데이터를 기반으로 다음 원칙에 따라 여행 일정을 작성하세요.\n\n" +
                    "1. 일정 데이터(`plans`)가 비어 있는 경우:\n" +
                    "   - `city` 필드에 있는 도시 정보(예: 도시 이름, 국가)를 바탕으로 해당 도시의 실제 인기 여행지를 추천하세요.\n" +
                    "   - 추천 일정은 하루를 기준으로 아침, 점심, 오후, 저녁으로 나눠 최소 5개, 최대 8개의 일정을 포함합니다.\n" +
                    "   - 각 일정은 다음 필드를 포함합니다:\n" +
                    "     - `time`: 일정 시작 시간. 여행지 도착시간 이후에 시작되도록 작성하세요.\n" +
                    "     - `title`: 활동 이름.\n" +
                    "     - `place`: 장소 이름.\n" +
                    "     - `mapPlace`: 장소의 상세 주소 또는 설명.\n" +
                    "     - `memo`: 추천 이유나 유용한 팁.\n" +
                    "     - `isDone`: 항상 `false`로 설정하세요.\n" +
                    "     - `puzzles`: 빈 배열로 설정하세요.\n" +
                    "     - `AI`: 항상 `true`로 설정하세요.\n" +
                    "     - `feedback`: 해당 일정을 추천하는 이유를 명시하세요.\n\n" +
                    "2. 일정 데이터(`plans`)가 포함된 경우:\n" +
                    "   - 기존 일정의 순서를 유지하며, 각 일정에 대해 평가하고 피드백을 작성하세요.\n" +
                    "   - 피드백은 `feedback` 필드에만 작성하며, 기존 일정이 삭제되거나 순서가 변경되지 않도록 작성하세요.\n" +
                    "   - 기존 일정의 시간 이후에 추가적인 추천 일정을 작성하세요.\n\n" +
                    "3. 응답 형식:\n" +
                    "   모든 응답은 반드시 아래 JSON 형식만으로 작성하세요. JSON 외의 다른 형식(텍스트, 주석 등)을 포함하지 마세요.\n" +
                    "{\n" +
                    "  \"city\": {\n" +
                    "    \"name\": \"도시 이름\",\n" +
                    "    \"country\": \"국가 이름\"\n" +
                    "  },\n" +
                    "  \"plans\": [\n" +
                    "    {\n" +
                    "      \"time\": \"오전/오후 시간\",\n" +
                    "      \"title\": \"활동 이름\",\n" +
                    "      \"place\": \"장소 이름\",\n" +
                    "      \"mapPlace\": \"장소 주소\",\n" +
                    "      \"memo\": \"추천 이유\",\n" +
                    "      \"isDone\": false,\n" +
                    "      \"puzzles\": [],\n" +
                    "      \"AI\": true,\n" +
                    "      \"feedback\": \"이 일정을 추천 또는 개선하는 이유\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}\n\n" +
                    "4. 추가 요구사항:\n" +
                    "   - 기존 일정은 삭제하지 마세요.\n" +
                    "   - 도착 시간 이후부터 일정을 시작하세요.\n" +
                    "   - 기존 일정의 피드백과 새로운 추천 일정이 모두 포함되도록 작성하세요.\n";


            // OpenAI 요청 데이터 구성
            Map<String, Object> openAiRequest = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", "제공된 일정: " + objectMapper.writeValueAsString(itineraryData))
                    )
            );

            // OpenAI API 호출
            ResponseEntity<Map> response = restTemplate.postForEntity(apiURL, openAiRequest, Map.class);

            if (response.getBody() == null || !response.getBody().containsKey("choices")) {
                throw new RuntimeException("OpenAI API에서 유효한 응답을 받지 못했습니다.");
            }

            // AI 응답 처리
            List<?> choices = (List<?>) response.getBody().get("choices");
            if (choices.isEmpty()) {
                throw new RuntimeException("OpenAI 응답에서 choices가 비어 있습니다.");
            }

            // choices에서 content 추출
            Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
            Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");
            String aiFeedback = (String) message.get("content");

            // JSON 문자열을 객체로 변환
            JsonNode jsonNode = objectMapper.readTree(aiFeedback);

            // JSON 데이터를 파일로 저장
            saveJsonToFile(jsonNode, "test.json");

            // 결과 반환 (객체를 그대로 전달)
            return ResultDto.builder()
                    .code(200)
                    .msg(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode)) // JSON을 String으로 변환
                    .success(true)
                    .build();

        } catch (Exception e) {
            return ResultDto.builder()
                    .code(500)
                    .msg("알 수 없는 오류가 발생했습니다: " + e.getMessage())
                    .success(false)
                    .build();
        }
    }

    // JSON 데이터를 파일로 저장하는 메서드
    private void saveJsonToFile(JsonNode jsonNode, String fileName) {
        try {
            File file = new File(fileName);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, jsonNode);
        } catch (IOException e) {
            throw new RuntimeException("JSON 파일 저장 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}
