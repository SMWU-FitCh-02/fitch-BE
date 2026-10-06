package com.vocal.app.song.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// iTunes 같은 해외 음원 검색은 한국 곡도 "Good Day / IU"처럼 영문으로만 돌려준다.
// 같은 곡의 한글 제목/가수명을 차트·DB에 이미 있는 곡(known) 중에서 AI가 "골라" 돌려준다. (표시용 — 분석/검색 키는 원래 영문 그대로 쓴다)
// AI가 제목을 새로 쓰거나 번역하지 못하도록, AI는 참고 목록의 번호만 고르고 제목은 서버가 목록에서 꺼내 쓴다.
// 목록에 같은 곡이 없으면 원래 제목 그대로 둔다. 한 번 판단한 곡은 서버 메모리에 기억해 두고 다시 묻지 않는다.
@Slf4j
@Service
public class KoreanNameService {

    public record Item(String title, String artist) {}

    private static final String MODEL_ID =
            "arn:aws:bedrock:ap-southeast-2:054422645032:inference-profile/global.openai.gpt-5.6-luna";

    private final BedrockRuntimeClient client = BedrockRuntimeClient.builder()
            .region(Region.AP_SOUTHEAST_2)
            .build();

    private static final int CHUNK = 10;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentHashMap<String, Item> cache = new ConcurrentHashMap<>();

    // 힌트 목록이 달라지면 같은 곡도 다시 물어볼 수 있도록 캐시 키에 힌트 내용을 반영한다.
    private static String key(Item i, String hintKey) {
        return (i.title() + "::" + i.artist()).toLowerCase() + "|" + hintKey;
    }

    private static String hintKey(List<Item> known) {
        if (known == null || known.isEmpty()) return "0";
        return String.valueOf(known.stream().map(k -> k.title() + "::" + k.artist()).sorted().toList().hashCode());
    }

    // known: 이미 알고 있는 한국 곡(차트/DB의 한글 제목 + 가수). AI가 영문 제목이 이 중 어떤 곡의 번역인지 맞출 때 쓴다.
    public List<Item> toKorean(List<Item> input, List<Item> known) {
        final String hk = hintKey(known);
        List<Item> missing = new ArrayList<>();
        for (Item i : input) {
            if (i.title() != null && i.artist() != null && !cache.containsKey(key(i, hk))) missing.add(i);
        }

        if (!missing.isEmpty() && known != null && !known.isEmpty()) {
            // 한 번에 많이 물으면 AI가 곡을 빼먹거나 느려져서, 10곡씩 나눠 동시에 묻는다.
            List<List<Item>> chunks = new ArrayList<>();
            for (int i = 0; i < missing.size(); i += CHUNK) {
                chunks.add(missing.subList(i, Math.min(i + CHUNK, missing.size())));
            }
            chunks.parallelStream().forEach(chunk -> {
                try {
                    translate(chunk, known, hk);
                } catch (Exception e) {
                    log.error("한글 표기 변환 실패", e);
                }
            });
        }

        List<Item> out = new ArrayList<>();
        for (Item i : input) {
            out.add(cache.getOrDefault(key(i, hk), i));
        }
        return out;
    }

    private void translate(List<Item> items, List<Item> known, String hk) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            sb.append(i).append(". ").append(items.get(i).title()).append(" - ").append(items.get(i).artist()).append("\n");
        }

        StringBuilder hint = new StringBuilder();
        for (int k = 0; k < known.size(); k++) {
            hint.append(k).append(". ").append(known.get(k).title()).append(" - ").append(known.get(k).artist()).append("\n");
        }

        String systemPrompt = """
        너는 같은 노래를 찾아 짝지어 주는 도우미야.
        [곡 목록]은 해외 음원 사이트에 등록된 곡이고, [참고 목록]은 한국에서 쓰는 한글 제목의 곡들이야.
        [곡 목록]의 각 곡이 [참고 목록]의 어느 곡과 "같은 노래"인지 찾아줘.
        - 같은 가수이고, 제목이 같은 노래의 영문 표기/영어 제목이라고 확실할 때만 짝지어. (의미가 비슷한 다른 노래는 짝짓지 마)
        - 확실하지 않거나 참고 목록에 없으면 -1.
        곡마다 {"n":곡 목록 번호,"match":참고 목록 번호 또는 -1} 형태의 객체를 만들어 JSON 배열로만 출력해. 모든 곡을 포함하고, 다른 설명은 하지 마.
        예시 출력: [{"n":0,"match":3},{"n":1,"match":-1}]
        """;

        ConverseResponse response = client.converse(req -> req
                .modelId(MODEL_ID)
                .system(SystemContentBlock.builder().text(systemPrompt).build())
                .messages(Message.builder()
                        .role(ConversationRole.USER)
                        .content(ContentBlock.builder().text("[참고 목록]\n" + hint + "\n[곡 목록]\n" + sb).build())
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
        // 번호(n)로 짝을 맞추기 때문에, AI가 몇 곡을 빼먹어도 나머지는 그대로 쓴다.
        List<Map<String, Object>> arr = objectMapper.readValue(
                m.group(), new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});
        for (Map<String, Object> row : arr) {
            Object n = row.get("n");
            Object mt = row.get("match");
            if (!(n instanceof Number) || !(mt instanceof Number)) continue;
            int idx = ((Number) n).intValue();
            int match = ((Number) mt).intValue();
            if (idx < 0 || idx >= items.size()) continue;
            // 매칭된 곡만 한글 제목/가수로 바꾼다. 아니면 원래 그대로 (AI가 만든 제목은 절대 쓰지 않음)
            Item original = items.get(idx);
            Item result = (match >= 0 && match < known.size()) ? known.get(match) : original;
            cache.put(key(original, hk), result);
        }
    }
}