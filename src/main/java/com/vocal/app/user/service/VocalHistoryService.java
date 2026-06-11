package com.vocal.app.user.service;

import com.vocal.app.user.entity.VocalHistory;
import com.vocal.app.user.repository.VocalHistoryRepository;
import com.vocal.app.global.util.NoteUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @RequiredArgsConstructor
public class VocalHistoryService {
    private final VocalHistoryRepository vocalHistoryRepository;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getHistory(Long userId) {
        return vocalHistoryRepository.findByUserUserIdOrderByMeasuredAtDesc(userId)
                .stream().map(h -> Map.<String, Object>of(
                        "historyId",    h.getHistoryId(),
                        "minNote",      h.getMinNote(),
                        "maxNote",      h.getMaxNote(),
                        "minNoteLabel", NoteUtil.toLabel(h.getMinNote()),
                        "maxNoteLabel", NoteUtil.toLabel(h.getMaxNote()),
                        "stableScore",  h.getStableScore(),
                        "measuredAt",   h.getMeasuredAt()
                )).toList();
    }
}
