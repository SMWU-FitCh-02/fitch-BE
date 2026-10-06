package com.vocal.app.song.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// iTunes 같은 해외 음원 검색은 한국 곡도 "Good Day / IU"처럼 영문으로만 돌려준다.
// 화면에 보여줄 한글 제목/가수명을 AI로 찾아서 돌려준다. (표시용 — 분석/검색 키는 원래 영문 그대로 쓴다)
// 한 번 변환한 곡은 서버 메모리에 기억해 두고 다시 묻지 않는다.
@Slf4j
@Service
public class KoreanNameService {

    public record Item(String title, String artist) {}

    private static final String MODEL_ID =
            "arn:aws:bedrock:ap-southeast-2:054422645032:inference-profile/global.openai.gpt-5.6-luna";

    private final BedrockRuntimeClient client = BedrockRuntimeClient.builder()
            .region(Region.AP_SOUTHEAST_2)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentHashMap<String, Item> cache = new ConcurrentHashMap<>();

    private static String key(Item i) {
        return (i.title() + "::" + i.artist()).toLowerCase();
    }

    public List<Item> toKorean(List<Item> input) {
        List<Item> missing = new ArrayList<>();
        for (Item i : input) {
            if (i.title() != null && i.artist() != null && !cache.containsKey(key(i))) missing.add(i);
        }

        if (!missing.isEmpty()) {
            try {
                translate(missing);
            } catch (Exception e) {
                log.error("한글 표기 변환 실패", e);
            }
        }

        List<Item> out = new ArrayList<>();
        for (Item i : input) {
            out.add(cache.getOrDefault(key(i), i));
        }
        return out;
    }

    private void translate(List<Item> items) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            sb.append(i).append(". ").append(items.get(i).title()).append(" - ").append(items.get(i).artist()).append("\n");
        }

        String systemPrompt = """
        너는 한국 음악 곡 정보를 한글 표기로 바꿔주는 도우미야.
        아래 목록은 해외 음원 사이트에 영문으로 등록된 곡이야. 각 곡에 대해:
        - 한국에서 발매된 곡이고 한국에서 쓰는 한글 제목이 있으면, 그 공식 한글 제목으로 바꿔.
        - 제목이 원래 영어인 곡이거나, 외국 곡이거나, 한글 제목이 확실하지 않으면 제목은 원래 그대로 둬. 절대 지어내거나 번역하지 마.
        - 가수는 한국 가수면 한국에서 쓰는 한글 이름으로 바꿔(예: IU → 아이유, HANRORO → 한로로). 외국 가수거나 확실하지 않으면 그대로 둬.
        번호 순서 그대로, 각 곡을 [제목, 가수] 쌍으로 담은 JSON 배열만 출력해. 다른 설명은 하지 마.
        예시 출력: [["좋은 날","아이유"],["Landing in Love","한로로"]]
        """;

        ConverseResponse response = client.converse(req -> req
                .modelId(MODEL_ID)
                .system(SystemContentBlock.builder().text(systemPrompt).build())
                .messages(Message.builder()
                        .role(ConversationRole.USER)
                        .content(ContentBlock.builder().text("곡 목록:\n" + sb).build())
                        .build())
                .inferenceConfig(cfg -> cfg.maxTokens(4000))
        );

        String output = null;
        for (ContentBlock block : response.output().message().content()) {
            if (block.text() != null && !block.text().isBlank()) {
                output = block.text();
                break;
            }
        }
        if (output == null) return;

        // 모델이 앞뒤에 설명을 붙이는 경우 대비: 가장 바깥 [ ... ] 만 파싱
        Matcher m = Pattern.compile("\\[.*\\]", Pattern.DOTALL).matcher(output);
        if (!m.find()) return;
        String[][] arr = objectMapper.readValue(m.group(), String[][].class);
        if (arr.length != items.size()) {
            log.warn("한글 표기 개수 불일치: 요청 {} / 응답 {}", items.size(), arr.length);
            return;
        }
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == null || arr[i].length < 2 || arr[i][0] == null || arr[i][1] == null) continue;
            cache.put(key(items.get(i)), new Item(arr[i][0].trim(), arr[i][1].trim()));
        }
    }
}