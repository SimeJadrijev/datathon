package com.datathon.api.profile;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileMatchService profileMatchService;

    @PostMapping("/match")
    List<MatchResult> match(@Valid @RequestBody ProfileRequest profile) {
        return profileMatchService.match(profile);
    }
}
