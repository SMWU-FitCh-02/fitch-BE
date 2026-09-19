package com.vocal.app.song.controller;

import com.vocal.app.song.entity.ArtistGenderEntry;
import com.vocal.app.song.repository.ArtistGenderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/chart")
@RequiredArgsConstructor
public class ChartController {

    private final ArtistGenderRepository artistGenderRepository;

    @GetMapping("/artist-genders")
    public Map<String, String> getArtistGenders(@RequestParam List<String> artists) {
        List<ArtistGenderEntry> found = artistGenderRepository.findByArtistNameIn(artists);
        return found.stream()
                .collect(Collectors.toMap(
                        ArtistGenderEntry::getArtistName,
                        e -> e.getGender().name()
                ));
    }
}