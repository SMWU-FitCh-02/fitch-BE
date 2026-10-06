package com.vocal.app.song.controller;

import com.vocal.app.song.service.KoreanNameService;
import com.vocal.app.song.service.KoreanNameService.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

// 화면에 영문으로 뜨는 곡 제목/가수명을 한글 표기로 바꿔서 돌려준다 (표시용).
@RestController
@RequiredArgsConstructor
public class KoreanNameController {

    public record Request(List<Item> items) {}
    public record Response(List<Item> items) {}

    private final KoreanNameService service;

    @PostMapping("/recommend/ko-names")
    public Response koNames(@RequestBody Request request) {
        List<Item> in = request.items() == null ? new ArrayList<>() : request.items();
        if (in.size() > 30) in = in.subList(0, 30); // 한 번에 너무 많이 보내는 것 방지
        return new Response(service.toKorean(in));
    }
}