package com.pingidentity.oidclogin.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.ui.Model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class BaseController {
		
	Logger logger = LoggerFactory.getLogger(BaseController.class);
	
	@Value("${ping-config.profile-page}")
	protected String profilePage;
	
	protected void setCommonAttributes(Model model, OidcUser principal) {
		if (principal != null) {
            model.addAttribute("profile", principal.getClaims());
            model.addAttribute("profileJson", toJson(principal.getClaims()));
            model.addAttribute("profilePage", profilePage);
        }
	}
    
	protected String toJson(Map<String, Object> claims) {
        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(claims);
        } catch (JsonProcessingException jpe) {
            logger.error("Error parsing claims to JSON", jpe);
        }
        return "Error parsing claims to JSON.";
    }
}