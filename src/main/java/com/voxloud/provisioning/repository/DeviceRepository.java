package com.voxloud.provisioning.repository;

import com.voxloud.provisioning.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends JpaRepository<com.voxloud.provisioning.entity.Device, String> {
    Device findByMacAddress(String macAddress);
}
