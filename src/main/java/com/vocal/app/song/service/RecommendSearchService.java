package com.vocal.app.song.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocal.app.song.dto.RecommendSearchRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class RecommendSearchService {

    // Bedrock 추론 프로파일 ARN (콘솔 > 모델 카탈로그 > GPT-5.6 Luna > 교차 리전 추론에서 확인)
    private static final String MODEL_ID =
            "arn:aws:bedrock:ap-southeast-2:054422645032:inference-profile/global.openai.gpt-5.6-luna";

    // 자격 증명은 기본 체인(환경변수 AWS_ACCESS_KEY_ID/AWS_SECRET_ACCESS_KEY,
    // 또는 `aws configure`로 저장된 ~/.aws/credentials)에서 자동으로 찾는다.
    private final BedrockRuntimeClient client = BedrockRuntimeClient.builder()
            .region(Region.AP_SOUTHEAST_2)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Integer> searchSongs(RecommendSearchRequest request) {
        List<RecommendSearchRequest.CandidateDto> candidates = request.getCandidates();
        if (candidates == null || candidates.isEmpty()) {
            return new ArrayList<>();
        }

        StringBuilder listBuilder = new StringBuilder();
        for (int i = 0; i < candidates.size(); i++) {
            var c = candidates.get(i);
            listBuilder.append(i).append(". ").append(c.getTitle())
                    .append(" - ").append(c.getArtist()).append("\n");
        }

        String systemPrompt = """
                너는 노래방 인기차트 곡 목록에서 사용자의 취향/분위기 설명에 맞는 곡을 골라주는 도우미야.
                아래 곡 목록에서 사용자 요청과 어울리는 곡을 최대 10개까지 골라서,
                그 곡들의 번호만 JSON 배열로 반환해. 다른 설명은 절대 하지 말고 JSON 배열만 출력해.
                예시 출력: [3, 17, 42]
                """;

        String userPrompt = "곡 목록:\n" + listBuilder + "\n사용자 요청: \"" + request.getQuery() + "\"";

        try {
            ConverseResponse response = client.converse(req -> req
                    .modelId(MODEL_ID)
                    .system(SystemContentBlock.builder().text(systemPrompt).build())
                    .messages(Message.builder()
                            .role(ConversationRole.USER)
                            .content(ContentBlock.builder().text(userPrompt).build())
                            .build())
                    .inferenceConfig(cfg -> cfg.maxTokens(300))
            );

            // reasoning 지원 모델은 응답이 여러 콘텐츠 블록으로 나뉘어 오기도 한다
            // (예: 추론 블록 + 실제 답변 블록). 텍스트가 있는 첫 블록을 찾는다.
            String output = null;
            for (ContentBlock block : response.output().message().content()) {
                if (block.text() != null && !block.text().isBlank()) {
                    output = block.text();
                    break;
                }
            }
            log.info("Bedrock 응답: {}", output);
            if (output == null) return new ArrayList<>();
            return parseIndices(output, candidates.size());
        } catch (Exception e) {
            log.error("Bedrock 호출 실패", e);
            return new ArrayList<>();
        }
    }

    // 모델이 JSON 배열 앞뒤에 설명을 덧붙이는 경우를 대비해, 첫 [ ... ] 블록만 뽑아서 파싱
    private List<Integer> parseIndices(String output, int candidateCount) {
        Pattern pattern = Pattern.compile("\\[[^\\]]*\\]");
        Matcher matcher = pattern.matcher(output);
        if (!matcher.find()) return new ArrayList<>();
        try {
            Integer[] arr = objectMapper.readValue(matcher.group(), Integer[].class);
            List<Integer> result = new ArrayList<>();
            for (Integer i : arr) {
                if (i != null && i >= 0 && i < candidateCount) result.add(i);
            }
            return result;
        } catch (Exception e) {
            log.warn("추천 결과 파싱 실패: {}", output, e);
            return new ArrayList<>();
        }
    }
}