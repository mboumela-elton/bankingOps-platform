package com.msel.app.controller;

import com.msel.app.dto.CreateUserRequest;
import com.msel.app.dto.UserDTO;
import com.msel.app.service.UserService;
import com.msel.app.util.StructuredLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final UserService userService;
    private final StructuredLogger structuredLogger;

    public UserController(UserService userService, StructuredLogger structuredLogger) {
        this.userService = userService;
        this.structuredLogger = structuredLogger;
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserRequest request, HttpServletRequest httpRequest) {
        String requestId = (String) httpRequest.getAttribute("X-Request-ID");
        Map<String, Object> context = new HashMap<>();
        context.put("action", "create_user");
        context.put("email", request.getEmail());
        structuredLogger.logEvent("user_creation_started", requestId, context);
        
        UserDTO userDTO = userService.createUser(request);
        
        context.put("user_id", userDTO.getId());
        structuredLogger.logEvent("user_created", requestId, context);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUser(@PathVariable UUID userId, HttpServletRequest httpRequest) {
        String requestId = (String) httpRequest.getAttribute("X-Request-ID");
        Map<String, Object> context = new HashMap<>();
        context.put("action", "get_user");
        context.put("user_id", userId);
        structuredLogger.logEvent("user_fetch_started", requestId, context);
        
        UserDTO userDTO = userService.getUserById(userId);
        
        structuredLogger.logEvent("user_fetched", requestId, context);
        return ResponseEntity.ok(userDTO);
    }
}
