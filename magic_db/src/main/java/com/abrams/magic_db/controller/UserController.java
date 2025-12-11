package com.abrams.magic_db.controller;

import com.abrams.magic_db.model.User;
import com.abrams.magic_db.service.UserService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public User getUserDetails(@PathVariable Long userId) {
        return userService.getUserById(userId);
    }
}