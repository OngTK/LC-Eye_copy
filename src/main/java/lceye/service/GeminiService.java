package lceye.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

@Service @RequiredArgsConstructor
public class GeminiService {
    // [*]
    private final ProcessInfoService processInfoService;
    private final ObjectMapper mapper;
    private final WebClient webClient;
    // [*] gemini 연동 설정
    final String MODEL_ENDPOINT ="/models/gemini-2.5-flash:generateContent";
    @Value("${gemini.api.key}")
    private String geminiApikey;

    // process 데이터
    List<Map<String,String>> processList = null;

    /**
     * 입력받은 리스트 문자열들의 유사도를 분석하거나 키워드를 추출하여 맵핑합니다.
     * @param inputList 분석할 문자열 리스트
     * @return CompletableFuture<Map<String, Set<String>>> (Key: 원본 문자열, Value: 연관된 키워드/유사어 Set)
     */
    public Mono<Map<String, Set<String>>> similarity(List<String> inputList) {
        System.out.println("GeminiService.similarity");
        if (inputList == null || inputList.isEmpty()) {
            return Mono.just(new HashMap<>());
        }// if end
        if (processList == null || processList.isEmpty()){
            processList = processInfoService.matchData().stream().map(dto ->{
                Map<String,String> map = new HashMap<>();
                map.put("pcname",dto.getPcname());
                map.put("pcdesc",dto.getPcdesc());
                return map;
            }).toList();
        }// if end

        // 1. 요청 프롬프트 작성
        String prompt = "당신은 LCI(전 과정목록 분석)의 번역 및 매칭프로그램입니다: " + toJsonString(inputList) +
                "각 문자열을 영어로 번역하여" + toJsonString(processList) + "해당 데이터의 pcdesc에 해당하면 해당pcname을 반환해야하며" +
                "오직 JSON 객체 형태로만 반환해야되고 키는 원본 문자열이고 값은 해당하는 pcname의 최대3개를 담은 배열이여야 합니다."+
                " 마크다운 형식(```json)을 포함하지 말고, 순수한 JSON만 반환하십시오.";

        // 2. 요청 바디 생성
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        Map<String, Object> part = new HashMap<>();

        part.put("text", prompt);
        content.put("parts", Collections.singletonList(part));
        requestBody.put("contents", Collections.singletonList(content));

        // 3. gemini 통신
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(MODEL_ENDPOINT)
                        .queryParam("key",geminiApikey)
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                // 에러처리
                .onStatus(HttpStatusCode::isError, clientResponse -> {
                    // 에러 발생 시
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(errorBody -> {
                                throw new RuntimeException("Gemini API Error: " + clientResponse.statusCode() + ", Body: " + errorBody);
                            });
                })
                .bodyToMono(String.class) // 응답 본문을 String으로 받음
                // 매핑작업
                .map(this::parseGeminiResponse); // Mono<String> -> Mono<Map<String,set<String>>> 변환
    }// func end

    // 헬퍼 메소드: 리스트를 JSON 문자열로 변환 (프롬프트 삽입용)
    private String toJsonString(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            return "[]";
        }// try end
    }// f end

    // 파싱 메소드: Gemini 응답에서 텍스트를 추출하고 JSON을 Map으로 변환
    private Map<String, Set<String>> parseGeminiResponse(String jsonResponse) {
        Map<String, Set<String>> resultMap = new HashMap<>();
        try {
            JsonNode root = mapper.readTree(jsonResponse);
            // candidates[0].content.parts[0].text 경로 탐색
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                String text = candidates.get(0).path("content").path("parts").get(0).path("text").asText();

                // 마크다운 코드 블록 제거 (혹시 포함될 경우)
                text = text.replace("```json", "").replace("```", "").trim();

                // 텍스트(JSON)를 Map<String, List<String>>으로 변환
                Map<String, List<String>> parsed = mapper.readValue(text, new TypeReference<Map<String, List<String>>>() {});

                // List를 Set으로 변환하여 결과 맵에 저장
                for (Map.Entry<String, List<String>> entry : parsed.entrySet()) {
                    resultMap.put(entry.getKey(), new HashSet<>(entry.getValue()));
                }// for end
            }// if end
        } catch (Exception e) {
            System.err.println("Gemini 응답 파싱 실패: " + e.getMessage());
        }// try end
        return resultMap;
    }// f end
}// class end
