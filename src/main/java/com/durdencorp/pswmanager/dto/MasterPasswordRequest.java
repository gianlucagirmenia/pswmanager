
package com.durdencorp.pswmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Master password verification request")
public class MasterPasswordRequest {

	@Schema(description = "Master password to verify", example = "MySuperSecureMasterPassword123!", required = true)
	private String masterPassword;

	public String getMasterPassword() {
		return masterPassword;
	}

	public void setMasterPassword(String masterPassword) {
		this.masterPassword = masterPassword;
	}
}