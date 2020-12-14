package com.pingidentity.oidclogin.service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.pingidentity.oidclogin.data.AuthenticationResponse;

@Service
public class TransactionApprovalService {
	
	Logger logger = LoggerFactory.getLogger(TransactionApprovalService.class);
	
	@Value("${ping-config.az-server-az-url}")
	private String azUrl;

	@Value("${ping-config.issuer-uri}")
	private String issuerUri;

	@Value("${ping-config.oidc-client-id}")
	private String clientId;

	@Value("${ping-config.oidc-client-secret}")
	private String clientSecret;

	@Value("${ping-config.oidc-client-redirect-uri}")
	private String clientRedirectUri;

	@Value("${ping-config.trx-approval.policy}")
	private String trxApprovalPolicy;
	
	@Value("${ping-config.trx-approval.template}")
	private String templateName;
	
	@Value("${ping-config.auth-flow-url}")
	private String authFlowUrl;
		
	@Autowired
	RestTemplate clientRestTemplate;

	public AuthenticationResponse initiateTransaction(String userId, Map <String,String> attributes,Map <String,String> context) throws JoseException {
		String uriTemplate = azUrl
				+ "?response_type=code&response_mode=pi.flow&client_id={client_id}&acr_values={acr_values}&login_hint_token={login_hint_token}&request={request}";

		Map<String, String> urlParameters = new HashMap<>();
		urlParameters.put("client_id",clientId);
		urlParameters.put("acr_values",trxApprovalPolicy);
		urlParameters.put("login_hint_token",generateLoginHintToken(userId));
		urlParameters.put("request",generateRequestParamToken(attributes, context));

		ResponseEntity<AuthenticationResponse> response = clientRestTemplate.getForEntity(uriTemplate,
				AuthenticationResponse.class,urlParameters);
		
		AuthenticationResponse authenticationResponse = response.getBody();
		logger.debug("Authentication result " + authenticationResponse.getStatus() + " with return url " +  authenticationResponse.getResumeUrl());
		
		return authenticationResponse;
	}
	
	public AuthenticationResponse poll(AuthenticationResponse response) {
		logger.debug ("Starting poll for flow " + response.getId());
		
		String uri = authFlowUrl + "/" + response.getId();
		ResponseEntity<AuthenticationResponse> newResponse = clientRestTemplate.getForEntity(uri,AuthenticationResponse.class);
		AuthenticationResponse authenticationResponse = newResponse.getBody();
		logger.debug("Authentication result " + authenticationResponse.getStatus() + " with return url " +  authenticationResponse.getResumeUrl());
		
		return authenticationResponse;
	}
	
	private String generateLoginHintToken(String userId)
			throws JoseException {
		JwtClaims jwtClaims = new JwtClaims();
		jwtClaims.setIssuer(clientId);
		jwtClaims.setSubject(userId);
		jwtClaims.setAudience(issuerUri);
		jwtClaims.setIssuedAtToNow();
		jwtClaims.setExpirationTimeMinutesInTheFuture(5.0F);
		JsonWebSignature jws = new JsonWebSignature();
		jws.setPayload(jwtClaims.toJson());
		jws.setKey((Key) new HmacKey(clientSecret.getBytes(StandardCharsets.UTF_8)));
		jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
		
		logger.debug("Login hint JWT: " + jws.getCompactSerialization());
		return jws.getCompactSerialization();
	}

	private String generateRequestParamToken(Map<String, String> variables, Map<String, String> piClientContext)
			throws JoseException {
		JwtClaims jwtClaims = new JwtClaims();
		jwtClaims.setIssuer(clientId);

		Map<String, Object> piTemplate = new HashMap<String, Object>();
		piTemplate.put("name", templateName);
		piTemplate.put("variables", variables);

		jwtClaims.setClaim("pi.template", piTemplate);
		jwtClaims.setClaim("pi.clientContext", piClientContext);

		jwtClaims.setAudience(issuerUri);
		JsonWebSignature jws = new JsonWebSignature();
		jws.setPayload(jwtClaims.toJson());
		jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
		jws.setKey((Key) new HmacKey(clientSecret.getBytes(StandardCharsets.UTF_8)));
		
		logger.debug("Request JWT: " + jws.getCompactSerialization());
		
		return jws.getCompactSerialization();
	}

	
}
