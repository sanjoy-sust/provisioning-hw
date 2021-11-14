package com.voxloud.provisioning.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.exception.ConversionException;
import com.voxloud.provisioning.exception.ResourceNotFoundException;
import com.voxloud.provisioning.repository.DeviceRepository;
import com.voxloud.provisioning.util.Constants;
import com.voxloud.provisioning.util.PropertyFileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProvisioningServiceImpl implements ProvisioningService {

    private final DeviceRepository deviceRepository;

    public ProvisioningServiceImpl(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public String getProvisioningFile(String macAddress) throws Exception {
        Device device = deviceRepository.findByMacAddress(macAddress);

        if (Objects.isNull(device)) {
            log.error("Device not found");
            throw new ResourceNotFoundException("GET_PROVISIONING_FILE", "RSC100", "Device not found");
        }

        Map<String, Object> propertyMap = readPropertiesFile();
        String response = Strings.EMPTY;
        if (Device.DeviceModel.CONFERENCE.equals(device.getModel())) {
            response = processConferenceProps(device, propertyMap);
        } else if (Device.DeviceModel.DESK.equals(device.getModel())) {
            response = processDeskProps(device, propertyMap);
        } else {
            log.error("Device model not found");
            throw new ResourceNotFoundException("GET_PROVISIONING_FILE", "RSC101", "Device model not found");
        }
        return Optional.ofNullable(response).filter(str -> str.length() != 0)
                .map(str -> str.substring(0, str.length() - 1)).orElse(response);
    }

    private String processDeskProps(Device device, Map<String, Object> propertyMap) {
        if (!StringUtils.isEmpty(device.getOverrideFragment())) {
            Map<String, Object> overrideFragmentMap = Arrays.stream(device.getOverrideFragment().split("\\n"))
                    .map(s -> s.split(Constants.KEY_VAL_SEPARATOR))
                    .collect(Collectors.toMap(s -> s[0], s -> s[1]));

            PropertyFileUtils.processOverrideFragment(overrideFragmentMap, propertyMap);
        }
        addPropsFromDB(device, propertyMap);
        return processMapToPropsFile(propertyMap);
    }

    private String processConferenceProps(Device device, Map<String, Object> propertyMap) throws ConversionException {
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

    private String processMapToPropsFile(Map<String, Object> propertyMap) {
        StringBuilder responseBuilder = new StringBuilder();
        for (Object key : propertyMap.keySet()) {
            String keyStr = key.toString();
            Object value = propertyMap.get(keyStr);

            if (propertyMap.containsKey(keyStr)) {
                if (value instanceof String && ((String) value).contains(Constants.LIST_DELIMETER_PROP_FILE)) {
                    List<String> convertedListType = Arrays.asList(((String) value).split(Constants.LIST_DELIMETER_PROP_FILE));
                    value = convertedListType;
                }
                responseBuilder.append(keyStr + Constants.KEY_VAL_SEPARATOR + value + Constants.PROP_FILE_LINE_SEPARATOR);
            }
        }
        return responseBuilder.toString();
    }

    private Map<String, Object> readPropertiesFile() throws ConversionException {
        Map<String, Object> propertyMap;
        try {
            Properties properties = PropertiesLoaderUtils.loadAllProperties("application.properties");
            propertyMap = PropertyFileUtils.processProperties(properties, Constants.PROVISIONING_PREFIX);
        } catch (IOException e) {
            log.error("Properties File processing error", e);
            throw new ConversionException("GET_PROVISIONING_FILE", "PROPS100", "Failed to process properties file");
        }
        return propertyMap;
    }
}
