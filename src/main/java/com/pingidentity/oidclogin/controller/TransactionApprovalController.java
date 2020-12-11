package com.pingidentity.oidclogin.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pingidentity.oidclogin.Consts;
import com.pingidentity.oidclogin.data.AuthenticationResponse;
import com.pingidentity.oidclogin.data.MoneyTransfer;
import com.pingidentity.oidclogin.service.TransactionApprovalService;

@Controller
public class TransactionApprovalController extends BaseController {

	Logger logger = LoggerFactory.getLogger(TransactionApprovalController.class);

	private final String APPROVAL_RESPONSE = "APPROVAL_RESPONSE";

	@Autowired
	TransactionApprovalService trxApprovalService;

	@GetMapping("/transaction-approval")
	public String showInputPage(Model model, @AuthenticationPrincipal OidcUser principal) {
		logger.debug("Starting transaction with user " + principal);
		setCommonAttributes(model, principal);
		model.addAttribute("transfer", new MoneyTransfer());
		return "transaction-start";
	}

	@PostMapping("/transaction-approval")
	public String initiateTransaction(@ModelAttribute @Valid MoneyTransfer transfer, Model model,
			@AuthenticationPrincipal OidcUser principal, HttpSession session) {
		logger.debug("Starting initiateTransaction " + transfer.getId());
		setCommonAttributes(model, principal);

		model.addAttribute("transfer", transfer);
		Map<String, String> variables = new ObjectMapper().convertValue(transfer, Map.class);

		try {
			AuthenticationResponse response = trxApprovalService.initiateTransaction(principal.getSubject(), variables,
					new HashMap<String, String>());

			if (Consts.PUSH_CONFIRMATION_REQUIRED.equals(response.getStatus())) {
				logger.debug("Push confirmation required");
				session.setAttribute(APPROVAL_RESPONSE, response);
				return "transaction-approval-wait";
			}
			
			// Other status might be returned here. Ignore for now and assume that a user only has one device paired
			// and return to the transaction start page if the status is not PUSH_CONFIRMATION_REQUIRED
		} catch (JoseException e) {
			logger.error("Error generating the JWT objects", e);
		}

		return "transaction-start";
	}

	@GetMapping("/transaction-approval-wait")
	public ResponseEntity<AuthenticationResponse> wait(@AuthenticationPrincipal OidcUser principal, Model model,
			HttpSession session) {
		logger.debug("Wait");
		setCommonAttributes(model, principal);

		AuthenticationResponse response = (AuthenticationResponse) session.getAttribute(APPROVAL_RESPONSE);
		logger.debug("Authentication response from session: " + response);

		if (response != null) {
			response = trxApprovalService.poll(response);
			if (Consts.COMPLETED.equals(response.getStatus())) {
				logger.debug("Transaction completed: " + response.getId());
				session.setAttribute(APPROVAL_RESPONSE, null);
				
				// do some business logic here
			} 
			if (Consts.FAILED.equals(response.getStatus()) || Consts.PUSH_CONFIRMATION_TIMED_OUT.equals(response.getStatus())) {
				logger.debug("Transaction cancelled: " + response.getId());
				session.setAttribute(APPROVAL_RESPONSE, null);
				
				// do not complete business logic
			}
		}

		return new ResponseEntity<AuthenticationResponse>(response, HttpStatus.OK);
	}

}