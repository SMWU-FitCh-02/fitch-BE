package com.vocal.app.user.controller;

import com.vocal.app.user.service.VocalHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;


//auth/login 토큰 받아야됨!!
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final VocalHistoryService vocalHistoryService;

    @GetMapping("/{id}/vocal-history")
    public ResponseEntity<List<Map<String, Object>>> getHistory(@PathVariable("id") Long userId) {
        return ResponseEntity.ok(vocalHistoryService.getHistory(userId));
    }
}
