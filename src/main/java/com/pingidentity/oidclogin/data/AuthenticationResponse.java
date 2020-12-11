package com.pingidentity.oidclogin.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthenticationResponse {

	@JsonProperty("id")
	private String id;

	@JsonProperty("resumeUrl")
	private String resumeUrl;

	@JsonProperty("status")
	private String status;

	@JsonProperty("bypassAllowed")
	private Boolean bypassAllowed;

	@JsonProperty("id")
	public String getId() {
		return id;
	}

	@JsonProperty("id")
	public void setId(String id) {
		this.id = id;
	}

	@JsonProperty("resumeUrl")
	public String getResumeUrl() {
		return resumeUrl;
	}

	@JsonProperty("resumeUrl")
	public void setResumeUrl(String resumeUrl) {
		this.resumeUrl = resumeUrl;
	}

	@JsonProperty("status")
	public String getStatus() {
		return status;
	}

	@JsonProperty("status")
	public void setStatus(String status) {
		this.status = status;
	}

	@JsonProperty("bypassAllowed")
	public Boolean getBypassAllowed() {
		return bypassAllowed;
	}

	@JsonProperty("bypassAllowed")
	public void setBypassAllowed(Boolean bypassAllowed) {
		this.bypassAllowed = bypassAllowed;
	}

}