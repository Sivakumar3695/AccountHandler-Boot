package com.application.acchandler.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage")
public class StorageProperties {

	/**
	 * Folder location for storing files
	 */

	@Value("${media.storage.service}")
	private String ip;

	public String getLocation() {
		return ip;
	}
}
