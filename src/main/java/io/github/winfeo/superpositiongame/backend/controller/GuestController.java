package io.github.winfeo.superpositiongame.backend.controller;

import io.github.winfeo.superpositiongame.backend.dto.toApp.GuestResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/guest")
public class GuestController {
    @PostMapping("/create")
    public ResponseEntity<GuestResponse> createGuest() {
        String guestId = "guest-" + UUID.randomUUID();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new GuestResponse(guestId));
    }
}
