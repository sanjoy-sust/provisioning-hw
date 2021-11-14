package com.voxloud.provisioning.factory;

import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.util.Constants;
import com.voxloud.provisioning.util.PropertyFileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("desk")
@Slf4j
public class DeskPropertyFileGenerator implements DynamicPropertyGeneratorFactory{
    @Override
    public String generateDynamicPropertyFile(Device device, Map<String, Object> propertyMap) {
        if (!StringUtils.isEmpty(device.getOverrideFragment())) {
            Map<String, Object> overrideFragmentMap = Arrays.stream(device.getOverrideFragment().split("\\n"))
                    .map(s -> s.split(Constants.KEY_VAL_SEPARATOR))
                    .collect(Collectors.toMap(s -> s[0], s -> s[1]));

            PropertyFileUtils.processOverrideFragment(overrideFragmentMap, propertyMap);
        }
        addPropsFromDB(device, propertyMap);
        return processMapToPropsFile(propertyMap);
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


}
