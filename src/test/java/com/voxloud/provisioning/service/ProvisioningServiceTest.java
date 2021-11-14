package com.voxloud.provisioning.service;

import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.exception.ResourceNotFoundException;
import com.voxloud.provisioning.repository.DeviceRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class ProvisioningServiceTest {


    @InjectMocks
    private ProvisioningServiceImpl provisioningService;

    @Mock
    private DeviceRepository deviceRepository;
    Device device;

    @Before
    public void init() {
        device = new Device();
        device.setUsername("john");
        device.setPassword("doe");
    }

    @Test
    public void testDeskPhoneWithOutOverrideFragment() throws Exception {
        String macAddress = "aa-bb-cc-dd-ee-ff";
        device.setMacAddress(macAddress);
        device.setModel(Device.DeviceModel.DESK);
        Mockito.when(deviceRepository.findByMacAddress(macAddress)).thenReturn(device);

        String provisioningFile = provisioningService.getProvisioningFile(macAddress);
        Assert.assertNotNull(provisioningFile);

        String verificationPart = "username=john";
        Assert.assertTrue(provisioningFile.contains(verificationPart));
    }

    @Test
    public void testConferencePhoneWithOutOverrideFragment() throws Exception {
        String macAddress = "f1-e2-d3-c4-b5-a6";
        device.setMacAddress(macAddress);
        device.setModel(Device.DeviceModel.CONFERENCE);
        Mockito.when(deviceRepository.findByMacAddress(macAddress)).thenReturn(device);

        String provisioningFile = provisioningService.getProvisioningFile(macAddress);
        Assert.assertNotNull(provisioningFile);
        System.out.println(provisioningFile);
        String verificationPart = "\"username\":\"john\"";
        Assert.assertTrue(provisioningFile.contains(verificationPart));
    }

    @Test
    public void testDeskPhoneWithOverrideFragment() throws Exception {
        String macAddress = "a1-b2-c3-d4-e5-f6";
        device.setMacAddress(macAddress);
        device.setModel(Device.DeviceModel.DESK);
        device.setOverrideFragment("domain=sip.anotherdomain.com\nport=5161\ntimeout=10");
        Mockito.when(deviceRepository.findByMacAddress(macAddress)).thenReturn(device);
        String provisioningFile = provisioningService.getProvisioningFile(macAddress);
        Assert.assertNotNull(provisioningFile);
        System.out.println(provisioningFile);
        String verificationPort = "port=5161";
        Assert.assertTrue(provisioningFile.contains(verificationPort));
        String verificationDomain = "domain=sip.anotherdomain.com";
        Assert.assertTrue(provisioningFile.contains(verificationDomain));
    }

    @Test
    public void testConferencePhoneWithOverrideFragment() throws Exception {
        String macAddress = "a1-b2-c3-d4-e5-f6";
        device.setMacAddress(macAddress);
        device.setModel(Device.DeviceModel.CONFERENCE);
        device.setOverrideFragment("{\"domain\":\"sip.anotherdomain.com\",\"port\":\"5161\",\"timeout\":10}");
        Mockito.when(deviceRepository.findByMacAddress(macAddress)).thenReturn(device);
        String provisioningFile = provisioningService.getProvisioningFile(macAddress);
        Assert.assertNotNull(provisioningFile);
        System.out.println(provisioningFile);
        String verificationPort = "\"port\":\"5161\"";
        Assert.assertTrue(provisioningFile.contains(verificationPort));
        String verificationDomain = "\"domain\":\"sip.anotherdomain.com\"";
        Assert.assertTrue(provisioningFile.contains(verificationDomain));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testNotExistingDevice() throws Exception {
        String macAddress = "a1-b2-c3-d4-e5-f9";
        device.setMacAddress(macAddress);
        Mockito.when(deviceRepository.findByMacAddress(macAddress)).thenReturn(null);
        provisioningService.getProvisioningFile(macAddress);
    }
}
