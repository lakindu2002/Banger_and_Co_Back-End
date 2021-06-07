package com.lakindu.bangerandcobackend.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/rental")
@PreAuthorize("isAuthenticated()")
public class RentalController {
}
