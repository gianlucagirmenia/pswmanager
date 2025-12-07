package com.durdencorp.pswmanager.model;

import jakarta.persistence.*;

@Entity
@Table(name = "app_config")
public class AppConfig {

	@Id
	private String configKey;

	@Column(nullable = false, length = 500)
	private String configValue;

	public AppConfig() {
	}

	public AppConfig(String configKey, String configValue) {
		this.configKey = configKey;
		this.configValue = configValue;
	}

	public String getConfigKey() {
		return configKey;
	}

	public void setConfigKey(String configKey) {
		this.configKey = configKey;
	}

	public String getConfigValue() {
		return configValue;
	}

	public void setConfigValue(String configValue) {
		this.configValue = configValue;
	}
}