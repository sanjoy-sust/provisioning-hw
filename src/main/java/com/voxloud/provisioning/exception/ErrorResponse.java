package com.voxloud.provisioning.exception;

import lombok.Data;

@Data
public class ErrorResponse {
	String feature;
	String code;
	String message;
}