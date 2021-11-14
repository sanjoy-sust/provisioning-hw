package com.voxloud.provisioning.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.exception.ConversionException;
import com.voxloud.provisioning.util.PropertyFileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Slf4j
@Component("conference")
public class ConferencePropertyFileGenerator implements DynamicPropertyGeneratorFactory{
    @Override
    public String generateDynamicPropertyFile(Device device, Map<String, Object> propertyMap) throws ConversionException {
        try {
            if (!StringUtils.isEmpty(device.getOverrideFragment())) {
                String overrideFragment = device.getOverrideFragment();
                ObjectReader reader = new ObjectMapper().readerFor(Map.class);
                Map<String, Object> overrideFragmentMap = reader.readValue(overrideFragment);
                PropertyFileUtils.processOverrideFragment(overrideFragmentMap, propertyMap);
            }
            addPropsFromDB(device, propertyMap);
            return new ObjectMapper().writeValueAsString(propertyMap);
        } catch (JsonProcessingException e) {
            log.error("JSON File processing error", e);
            throw new ConversionException("GET_PROVISIONING_FILE", "CONV100", "JSON Processing Failed");
        }
    }

    private void addPropsFromDB(Device device, Map<String, Object> propertyMap) {
        propertyMap.put("username", device.getUsername());
        propertyMap.put("password", device.getPassword());
    }
}
