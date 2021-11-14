package com.voxloud.provisioning.controller;

import com.voxloud.provisioning.service.ProvisioningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class ProvisioningController {

    @Autowired
    private ProvisioningService provisioningService;

    @GetMapping(value = "provisioning/{macAddress}")
    public String getProvisioningFile(@PathVariable("macAddress") String macAddress) throws Exception {
        log.info("Calling for provisioning file for mac address: {}", macAddress);
        return provisioningService.getProvisioningFile(macAddress);
    }
}