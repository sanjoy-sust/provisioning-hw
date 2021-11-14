package com.voxloud.provisioning.factory;

import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.exception.ConversionException;

import java.util.Map;

public interface DynamicPropertyGeneratorFactory {
    String generateDynamicPropertyFile(Device device, Map<String, Object> propertyMap) throws ConversionException;
}
