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
            String systemPrompt = "사용자는 특정 도시 정보를 입력하고 여행 일정을 요청합니다. 주어진 데이터를 기반으로 다음 규칙에 따라 여행 일정을 작성하세요.\n" +
                    "\n" +
                    "1. 일정 데이터(`plans`)가 **비어 있는 경우**:\n" +
                    "   - `place` 필드에 입력된 도시 정보를 바탕으로 해당 도시의 인기 여행지를 추천하세요.\n" +
                    "   - 하루 기준으로 오전과 오후를 나눠 **최소 7개, 최대 8개 일정**을 작성하세요.\n" +
                    "   - 각 일정은 다음 필드를 포함합니다:\n" +
                    "     - `time`: 일정 시작 시간. **\"오전/오후 HH:MM\"** 형식을 사용하세요.\n" +
                    "     - `title`: 활동 이름.\n" +
                    "     - `place`: 장소 이름.\n" +
                    "     - `mapPlace`: 장소의 상세 주소나 설명.\n" +
                    "     - `memo`: 추천 이유나 팁.\n" +
                    "     - `isDone`: 항상 `false`로 설정하세요.\n" +
                    "     - `puzzles`: 빈 객체 `{}`로 설정하세요.\n" +
                    "     - `AI`: 항상 `true`로 설정하세요.\n" +
                    "     - `feedback`: 추천 이유를 명시하세요.\n" +
                    "\n" +
                    "2. 일정 데이터(`plans`)가 **이미 포함된 경우**:\n" +
                    "   - 기존 일정과 새로 추천한 일정을 **시간 순서**로 통합 정렬하세요.\n" +
                    "     - `time` 필드 기준으로 **오전/오후를 고려하여 오름차순**으로 정렬합니다.\n" +
                    "     - 예: \"오전 09:00\" → \"오전 10:30\" → \"오후 01:00\" → \"오후 03:30\" → \"오후 06:30\".\n" +
                    "   - 기존 일정은 **삭제하지 마세요**.\n" +
                    "   - 추가 추천 일정은 하루 기준으로 오전과 오후를 나눠 **최소 7개, 최대 8개 일정**이 포함되도록 보장하세요.\n" +
                    "   - 모든 일정에 대해 **필요시 피드백을 작성**하고, 기존 피드백은 유지하세요.\n" +
                    "   - 사용자가 이미 포함한 일정의 데이터는 수정하지 말고 그대로 유지하세요. 추가 일정만 작성하세요.\n" +
                    "\n" +
                    "3. **응답 형식**:\n" +
                    "   - 반드시 아래 JSON 형식으로 작성하세요. **JSON 외 다른 형식(텍스트, 주석 등)은 포함하지 마세요**.\n" +
                    "{\n" +
                    "  \"plans\": [\n" +
                    "    {\n" +
                    "      \"time\": \"오전/오후 HH:MM\",\n" +
                    "      \"title\": \"활동 이름\",\n" +
                    "      \"place\": \"장소 이름\",\n" +
                    "      \"mapPlace\": \"장소 주소\",\n" +
                    "      \"memo\": \"추천 이유나 꿀팁\",\n" +
                    "      \"isDone\": false,\n" +
                    "      \"puzzles\": {},\n" +
                    "      \"AI\": true,\n" +
                    "      \"feedback\": \"이 일정을 추천 또는 개선하는 이유\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}\n" +
                    "\n" +
                    "4. **추가 요구사항**:\n" +
                    "   - 모든 일정은 **시간 순서**로 제공해야 합니다.\n" +
                    "   - 여행지뿐만 아니라 맛집도 포함할 수 있습니다.\n" +
                    "   - 기존 일정은 **삭제하지 않고 유지**하세요.\n" +
                    "   - 추천 일정이 **최소 7개, 최대 8개**가 되도록 보장하세요.\n" +
                    "   - 기존 피드백과 새로 작성된 피드백 모두 포함해야 합니다.\n" +
                    "   - 일정은 항상 **하루 기준 오전과 오후로 구분**되도록 작성하세요.\n" +
                    "   - 사용자 입력 데이터의 모든 기존 요소를 그대로 유지하고, 추가한 항목은 명확히 `AI` 필드에서 구분할 수 있어야 합니다.\n" +
                    "\n" +
                    "5. **예외 처리**:\n" +
                    "   - 기존 일정에서 시간(`time`)이 중복되더라도 수정하지 마세요.\n" +
                    "   - 새로운 일정이 기존 시간과 겹치는 경우, 겹치지 않는 시간으로 자동 조정해 추가하세요.\n";



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
