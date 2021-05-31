package com.lakindu.bangerandcobackend.controller;

import com.lakindu.bangerandcobackend.serviceinterface.AdditionalEquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RestController;

@RestController //handle rest interactions (JSON Response)
public class AdditionalEquipmentController {
    private final AdditionalEquipmentService additionalEquipmentService;

    @Autowired //inject
    public AdditionalEquipmentController(
            @Qualifier("additionalEquipmentServiceImpl") AdditionalEquipmentService additionalEquipmentService) {
        this.additionalEquipmentService = additionalEquipmentService;
    }
}
