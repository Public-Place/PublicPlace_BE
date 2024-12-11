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
            String systemPrompt = """
사용자는 특정 도시 정보를 입력하고 여행 일정을 요청합니다. 요청에 따라 두 가지 경우로 나눠 정확한 JSON 형식을 작성해야 합니다:

1. **`items`가 비어있는 경우:**
   - 입력 데이터에 `items`가 비어있는 경우, `plans` 배열에 새로운 일정을 작성하세요.
   - 작성된 일정은 아래와 같은 형식을 따릅니다:
     {
       "id": 고유 번호 (1부터 시작),
       "time": "오전/오후 HH:MM",
       "title": "활동 이름",
       "place": "장소 이름",
       "mapPlace": "장소의 상세 주소",
       "memo": "추천 이유나 팁",
       "isDone": 항상 false,
       "puzzle": 빈 객체 {},
       "AI": 항상 true,
       "feedback": "추천 이유"
     }
   - 일정을 오전과 오후로 나누어 총 7개를 작성합니다.
   - 모든 JSON은 올바른 형식으로 제공되어야 합니다.

예시 응답:
{
  "plans": [
    {
      "id": 1,
      "time": "오전 8:30",
      "title": "카페에서 아침 식사",
      "place": "무지카페 텐진",
      "mapPlace": "1 Chome-3-1 Daimyo, Chuo Ward, Fukuoka, 810-0041",
      "memo": "심플하고 건강한 아침 메뉴 제공",
      "isDone": false,
      "puzzle": {},
      "AI": true,
      "feedback": "이른 아침부터 문을 여는 곳으로, 깔끔하고 건강한 식사를 할 수 있어 좋습니다."
    },
    {
      "id": 2,
      "time": "오전 10:00",
      "title": "다자이후 텐만구 방문",
      "place": "다자이후 텐만구",
      "mapPlace": "4 Chome-7-1 Saifu, Dazaifu, Fukuoka 818-0117",
      "memo": "일본 학문의 신을 모신 신사",
      "isDone": false,
      "puzzle": {},
      "AI": true,
      "feedback": "후쿠오카 근교 명소로 일본 전통문화를 느낄 수 있으며, 주변 상점가도 매력적입니다."
    },
    {
      "id": 3,
      "time": "오전 12:00",
      "title": "점심 식사",
      "place": "우동 타이라",
      "mapPlace": "Fukuoka, Chuo Ward, Watanabe-dori, 1-10-1",
      "memo": "후쿠오카의 쫄깃한 우동을 맛볼 수 있는 유명 맛집",
      "isDone": false,
      "puzzle": {},
      "AI": true,
      "feedback": "점심시간 줄이 길 수 있지만 맛과 분위기가 훌륭해 기다릴 가치가 있습니다."
    },
    {
      "id": 4,
      "time": "오후 2:00",
      "title": "오호리 공원 산책",
      "place": "오호리 공원",
      "mapPlace": "1-2 Ohorikoen, Chuo Ward, Fukuoka, 810-0051",
      "memo": "아름다운 호수와 함께 여유로운 산책 가능",
      "isDone": false,
      "puzzle": {},
      "AI": true,
      "feedback": "후쿠오카 도심 속 자연을 즐기기에 최고의 장소로 추천합니다."
    },
    {
      "id": 5,
      "time": "오후 4:00",
      "title": "후쿠오카 타워 방문",
      "place": "후쿠오카 타워",
      "mapPlace": "2 Chome-3-26 Momochihama, Sawara Ward, Fukuoka, 814-0001",
      "memo": "도심과 바다를 모두 조망할 수 있는 전망대",
      "isDone": false,
      "puzzle": {},
      "AI": true,
      "feedback": "일몰 시간에 방문하면 멋진 풍경을 볼 수 있어 추천합니다."
    },
    {
      "id": 6,
      "time": "오후 6:00",
      "title": "저녁 식사",
      "place": "하카타 멘타이코 덮밥 전문점",
      "mapPlace": "2 Chome-10-3 Hakata Ekimae, Hakata Ward, Fukuoka, 812-0011",
      "memo": "매콤한 명란젓 덮밥과 함께 후쿠오카만의 맛을 느낄 수 있음",
      "isDone": false,
      "puzzle": {},
      "AI": true,
      "feedback": "후쿠오카의 명물 요리를 경험할 수 있는 곳으로 특히 추천합니다."
    },
    {
      "id": 7,
      "time": "오후 8:00",
      "title": "야타이 거리 방문",
      "place": "나카스 야타이 거리",
      "mapPlace": "Nakasu, Hakata Ward, Fukuoka, 810-0801",
      "memo": "다양한 노점에서 간단한 안주와 술을 즐길 수 있음",
      "isDone": false,
      "puzzle": {},
      "AI": true,
      "feedback": "현지 분위기를 만끽하며 다양한 먹거리를 경험할 수 있는 야타이 거리는 밤에 방문하기 좋습니다."
    }
  ]
}

2. **`items`가 포함된 경우:**

{
  "place": "일본 후쿠오카",
   {
     "items": [
          {
            "id": 1,
            "time": "오후 1:00",
            "title": "공항 도착",
            "place": "후쿠오카 국제 공항",
            "mapPlace": "3 Chome-2-19 Tenjin, Chuo Ward, Fukuoka, 810-0001",
            "memo": "4번 출구로 나간 후 셔틀버스 탑승",
            "isDone": false,
            "puzzle": {},
            "AI": false,
            "feedback": ""
          },
          {
            "id": 2,
            "time": "오후 3:00",
            "title": "호텔 체크인",
            "place": "그랜드 호텔",
            "mapPlace": "4 Chome-2-1 Hakata, Fukuoka",
            "memo": "체크인 후 짐 정리",
            "isDone": false,
            "puzzle": {},
            "AI": false
          },
          {
            "id": 3,
            "time": "오후 3:30",
            "title": "점심 식사",
            "place": "이치란 라멘",
            "mapPlace": "Fukuoka, Hakata Ward, Hakata Ekimae, 2 Chome−2−1, Fukuoka Center Building, B2F",
            "memo": "24시간 영업!",
            "isDone": false,
            "puzzle": {},
            "AI": false
          },
          {
            "id": 4,
            "time": "오후 4:30",
            "title": "지하 쇼핑 센터 구경",
            "place": "텐진 지하가",
            "mapPlace": "〒810-0001 Fukuoka, Chuo Ward, Tenjin, 2 Chome, 地下1・2・3号",
            "memo": "모든 구간 와이파이 무료 이용 가능",
            "isDone": false,
            "puzzle": {},
            "AI": false
          },
          {
            "id": 5,
            "time": "오후 5:00",
            "title": "후쿠오카 기념품 구매",
            "place": "돈키호테 본점",
            "mapPlace": "1 Chome-20-17 Imaizumi, Chuo Ward, Fukuoka, 810-0021",
            "memo": "카드 결제 가능 / 카카오페이 가능",
            "isDone": false,
            "puzzle": {},
            "AI": false
          }
        ]   
    },
}

   - 이렇게 값이 요청이 나오면
   - 아래 응답과 동일하게 plans 배열에 Json값을 넘겨주세요

예시 응답:
{
  "plans": [
    {
        "id": 1,
        "time": "오후 1:00",
        "title": "공항 도착",
        "place": "후쿠오카 국제 공항",
        "mapPlace": "3 Chome-2-19 Tenjin, Chuo Ward, Fukuoka, 810-0001",
        "memo": "4번 출구로 나간 후 셔틀버스 탑승",
        "isDone": false,
        "puzzle": {},
        "AI": true,
        "feedback": "후쿠오카 공항은 입국 수속이 비교적 빠르지만, 셔틀버스를 놓치지 않도록 시간 확인을 권장합니다."
      },
      {
        "id": 2,
        "time": "오후 3:00",
        "title": "호텔 체크인",
        "place": "그랜드 호텔",
        "mapPlace": "4 Chome-2-1 Hakata, Fukuoka",
        "memo": "체크인 후 짐 정리",
        "isDone": false,
        "puzzle": {},
        "AI": false,
        "feedback": ""
      },
      {
        "id": 3,
        "time": "오후 3:30",
        "title": "점심 식사",
        "place": "신신라멘 본점",
        "mapPlace": "3-2-19 Tenjin, Chuo Ward, Fukuoka, 810-0001",
        "memo": "텐진 지하가 근처, 진한 돼지뼈 육수로 유명",
        "isDone": false,
        "puzzle": {},
        "AI": true,
        "feedback": "이치란 라멘은 혼잡할 가능성이 높아 텐진 근처의 신신라멘 본점을 추천합니다. 텐진 지하가와 가까워 이동이 편리합니다."
      },
      {
        "id": 4,
        "time": "오후 4:30",
        "title": "지하 쇼핑 센터 구경",
        "place": "텐진 지하가",
        "mapPlace": "〒810-0001 Fukuoka, Chuo Ward, Tenjin, 2 Chome, 地下1・2・3号",
        "memo": "모든 구간 와이파이 무료 이용 가능",
        "isDone": false,
        "puzzle": {},
        "AI": false,
        "feedback": ""
      },
      {
        "id": 5,
        "time": "오후 6:00",
        "title": "후쿠오카 기념품 구매",
        "place": "돈키호테 본점",
        "mapPlace": "1 Chome-20-17 Imaizumi, Chuo Ward, Fukuoka, 810-0021",
        "memo": "카드 결제 가능 / 카카오페이 가능",
        "isDone": false,
        "puzzle": {},
        "AI": true,
        "feedback": "돈키호테는 기념품부터 생필품까지 다양한 품목을 취급합니다. 시간을 조금 늦춰 여유롭게 쇼핑하는 것을 추천합니다."
      },
      {
        "id": 6,
        "time": "오후 8:00",
        "title": "저녁 맥주 한잔",
        "place": "야마카타야 맥주집",
        "mapPlace": "3-2-1 Tenjin, Chuo Ward, Fukuoka, 810-0001",
        "memo": "텐진 중심가 근처, 현지 맥주와 야키토리 추천",
        "isDone": false,
        "puzzle": {},
        "AI": true,
        "feedback": "현지 분위기를 즐기기에 좋은 장소로, 일본 특유의 다양한 맥주와 안주를 맛볼 수 있습니다. 텐진 쇼핑 후 들르기에 적합합니다."
      }
  ]
}
""";

//                    "1. 일정 데이터(`plans`)가 **비어 있는 경우**:\n" +
//                    "   - `place` 필드에 입력된 도시 정보를 바탕으로 해당 도시의 인기 여행지를 추천하세요.\n" +
//                    "   - 하루 기준으로 오전과 오후를 나눠 **최소 7개, 최대 8개 일정**을 작성하세요.\n" +
//                    "   - 각 일정은 다음 필드를 포함합니다:\n" +
//                    "     - `id`: id값 (숫자로 표시 중요) (시간순서).\n" +
//                    "     - `time`: 일정 시작 시간. **\"오전/오후 HH:MM\"** 형식을 사용하세요.\n" +
//                    "     - `title`: 활동 이름.\n" +
//                    "     - `place`: 장소 이름.\n" +
//                    "     - `mapPlace`: 장소의 상세 주소나 설명.\n" +
//                    "     - `memo`: 추천 이유나 팁.\n" +
//                    "     - `isDone`: 항상 `false`로 설정하세요.\n" +
//                    "     - `puzzles`: 빈 객체 `{}`로 설정하세요.\n" +
//                    "     - `AI`: 항상 `true`로 설정하세요.\n" +
//                    "     - `feedback`: 추천 이유를 명시하세요.\n" +
//                    "\n" +
//                    "2. 일정 데이터(`plans`)가 **이미 포함된 경우**:\n" +
//                    "   - 기존 일정과 새로 추천한 일정을 **시간 순서**로 통합 정렬하세요.\n" +
//                    "     - `time` 필드 기준으로 **오전/오후를 고려하여 오름차순**으로 정렬합니다.\n" +
//                    "     - 예: \"오전 09:00\" → \"오전 10:30\" → \"오후 01:00\" → \"오후 03:30\" → \"오후 06:30\".\n" +
//                    "   - 기존 일정은 **삭제하지 마세요**.\n" +
//                    "   - 추가 추천 일정은 하루 기준으로 오전과 오후를 나눠 **최소 7개, 최대 8개 일정**이 포함되도록 보장하세요.\n" +
//                    "   - 모든 일정에 대해 **필요시 피드백을 작성**하고, 기존 피드백은 유지하세요.\n" +
//                    "   - 사용자가 이미 포함한 일정의 데이터는 수정하지 말고 그대로 유지하세요. 추가 일정만 작성하세요.\n" +
//                    "\n" +
//                    "3. **응답 형식**:\n" +
//                    "   - 반드시 아래 JSON 형식으로 작성하세요. **JSON 외 다른 형식(텍스트, 주석 등)은 포함하지 마세요**.\n" +
//                    "{\n" +
//                    "  \"plans\": [\n" +
//                    "    {\n" +
//                    "      \"id\": \" 1부터 시작 (시간 순서대로) 1은 문자가 아닌 숫자\",\n" +
//                    "      \"time\": \"오전/오후 HH:MM\",\n" +
//                    "      \"title\": \"활동 이름\",\n" +
//                    "      \"place\": \"장소 이름\",\n" +
//                    "      \"mapPlace\": \"장소 주소\",\n" +
//                    "      \"memo\": \"추천 이유나 꿀팁\",\n" +
//                    "      \"isDone\": false,\n" +
//                    "      \"puzzles\": {},\n" +
//                    "      \"AI\": true,\n" +
//                    "      \"feedback\": \"이 일정을 추천 또는 개선하는 이유\"\n" +
//                    "    }\n" +
//                    "  ]\n" +
//                    "}\n" +
//                    "\n" +
//                    "4. **추가 요구사항**:\n" +
//                    "   - 모든 일정은 **시간 순서**로 제공해야 합니다.\n" +
//                    "   - 여행지뿐만 아니라 맛집도 포함할 수 있습니다.\n" +
//                    "   - 기존 일정은 **삭제하지 않고 유지**하세요.\n" +
//                    "   - 추천 일정이 **최소 7개, 최대 8개**가 되도록 보장하세요.\n" +
//                    "   - 기존 피드백과 새로 작성된 피드백 모두 포함해야 합니다.\n" +
//                    "   - 일정은 항상 **하루 기준 오전과 오후로 구분**되도록 작성하세요.\n" +
//                    "   - 사용자 입력 데이터의 모든 기존 요소를 그대로 유지하고, 추가한 항목은 명확히 `AI` 필드에서 구분할 수 있어야 합니다.\n" +
//                    "\n" +
//                    "5. **예외 처리**:\n" +
//                    "   - 기존 일정에서 시간(`time`)이 중복되더라도 수정하지 마세요.\n" +
//                    "   - 새로운 일정이 기존 시간과 겹치는 경우, 겹치지 않는 시간으로 자동 조정해 추가하세요.\n";



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
