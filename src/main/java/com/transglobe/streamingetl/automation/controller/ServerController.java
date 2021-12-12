package com.transglobe.streamingetl.automation.controller;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.transglobe.streamingetl.automation.service.ServerService;

@RestController
@RequestMapping("/server")
public class ServerController {
	static final Logger LOG = LoggerFactory.getLogger(ServerController.class);

	@Autowired
	private ServerService serverService;
	
	@Autowired
	private ObjectMapper mapper;

	@PostMapping(path="/cleanup", produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<Object> cleanup() {
		LOG.info(">>>>controller cleanup is called");
		
		ObjectNode objectNode = mapper.createObjectNode();
		
		try {
			serverService.cleanup();
			objectNode.put("returnCode", "0000");
		} catch (Exception e) {
			String errMsg = ExceptionUtils.getMessage(e);
			String stackTrace = ExceptionUtils.getStackTrace(e);
			objectNode.put("returnCode", "-9999");
			objectNode.put("errMsg", errMsg);
			objectNode.put("returnCode", stackTrace);
			LOG.error(">>> errMsg={}, stacktrace={}",errMsg,stackTrace);
		}
		
		LOG.info(">>>>controller cleanup finished ");
		
		return new ResponseEntity<Object>(objectNode, HttpStatus.OK);
	}
	
	@PostMapping(path="/start/{restServer}", produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<Object> startRestServer(@PathVariable("restServer") String restServer) {
		LOG.info(">>>>controller startRestServer is called");
		
		ObjectNode objectNode = mapper.createObjectNode();
		
		try {
			serverService.startRestServer(restServer);
			objectNode.put("returnCode", "0000");
		} catch (Exception e) {
			String errMsg = ExceptionUtils.getMessage(e);
			String stackTrace = ExceptionUtils.getStackTrace(e);
			objectNode.put("returnCode", "-9999");
			objectNode.put("errMsg", errMsg);
			objectNode.put("returnCode", stackTrace);
			LOG.error(">>> errMsg={}, stacktrace={}",errMsg,stackTrace);
		}
		
		LOG.info(">>>>controller startRestServer finished ");
		
		return new ResponseEntity<Object>(objectNode, HttpStatus.OK);
	}
	@PostMapping(path="/stop/{restServer}", produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<Object> stopRestServer(@PathVariable("restServer") String restServer) {
		LOG.info(">>>>controller stopRestServer is called");
		
		ObjectNode objectNode = mapper.createObjectNode();
		
		try {
			serverService.stopRestServer(restServer);
			objectNode.put("returnCode", "0000");
		} catch (Exception e) {
			String errMsg = ExceptionUtils.getMessage(e);
			String stackTrace = ExceptionUtils.getStackTrace(e);
			objectNode.put("returnCode", "-9999");
			objectNode.put("errMsg", errMsg);
			objectNode.put("returnCode", stackTrace);
			LOG.error(">>> errMsg={}, stacktrace={}",errMsg,stackTrace);
		}
		
		LOG.info(">>>>controller stopRestServer finished ");
		
		return new ResponseEntity<Object>(objectNode, HttpStatus.OK);
	}
}