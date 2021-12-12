package com.transglobe.streamingetl.automation.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ServerService {
	static final Logger LOG = LoggerFactory.getLogger(ServerService.class);

	
	@Value("${kafka.rest.home}")
	private String kafkaRestHome;
	
	@Value("${kafka.rest.start.script}")
	private String kafkaRestStartScript;
	
	@Value("${kafka.rest.port}")
	private String kafkaRestPort;
	
	@Value("${logminer.rest.home}")
	private String logminerRestHome;
	
	@Value("${logminer.rest.start.script}")
	private String logminerRestStartScript;
	
	@Value("${logminer.rest.port}")
	private String logminerRestPort;
	
	@Value("${health.rest.home}")
	private String healthRestHome;
	
	@Value("${health.rest.start.script}")
	private String healthRestStartScript;
	
	@Value("${health.rest.port}")
	private String healthRestPort;
	
	@Value("${partycontact.rest.home}")
	private String partycontactRestHome;
	
	@Value("${partycontact.rest.start.script}")
	private String partycontactRestStartScript;
	
	@Value("${partycontact.rest.port}")
	private String partycontactRestPort;
	
	public void startRestServer(String restServer) throws Exception {
		int kafkaRestPortNum = Integer.valueOf(kafkaRestPort);
		int logminerRestPortNum = Integer.valueOf(logminerRestPort);
		int healthRestPortNum = Integer.valueOf(healthRestPort);
		int partycontactRestPortNum = Integer.valueOf(partycontactRestPort);
		
		int port = 0;
		if (StringUtils.equalsIgnoreCase("kafka-rest", restServer)) {
			startRestServer(kafkaRestHome, kafkaRestStartScript);
			port = kafkaRestPortNum;
		} else if (StringUtils.equalsIgnoreCase("logminer-rest", restServer)) {
			startRestServer(logminerRestHome, logminerRestStartScript);
			port = logminerRestPortNum;
		} else if (StringUtils.equalsIgnoreCase("health-rest", restServer)) {
			startRestServer(healthRestHome, healthRestStartScript);
			port = healthRestPortNum;
		} else if (StringUtils.equalsIgnoreCase("partycontact-rest", restServer)) {
			startRestServer(partycontactRestHome, partycontactRestStartScript);
			port = partycontactRestPortNum;
		} else {
			throw new Exception("restServer=" + restServer + " not recognixed");
		}
		
		
		while (!checkPortListening(port)) {
			Thread.sleep(1000);
			LOG.info(">>>> Sleep for 1 second");;
		}
		LOG.info(">>>>> startRestServer port={} Done!!!, ", port);
	}
	public void stopRestServer(String restServer) throws Exception {
		int kafkaRestPortNum = Integer.valueOf(kafkaRestPort);
		int logminerRestPortNum = Integer.valueOf(logminerRestPort);
		int healthRestPortNum = Integer.valueOf(healthRestPort);
		int partycontactRestPortNum = Integer.valueOf(partycontactRestPort);
		
		int port = 0;
		if (StringUtils.equalsIgnoreCase("kafka-rest", restServer)) {
			port = kafkaRestPortNum;
		} else if (StringUtils.equalsIgnoreCase("logminer-rest", restServer)) {
			port = logminerRestPortNum;
		} else if (StringUtils.equalsIgnoreCase("health-rest", restServer)) {
			port = healthRestPortNum;
		} else if (StringUtils.equalsIgnoreCase("partycontact-rest", restServer)) {
			port = partycontactRestPortNum;
		}else {
			throw new Exception("restServer=" + restServer + " not recognixed");
		}
		
		killProcess(port);
		
		while (checkPortListening(port)) {
			Thread.sleep(1000);
			LOG.info(">>>> Sleep for 1 second");;
		}
		LOG.info(">>>>> stopRestServer Done!!!");
	}
	public boolean startRestServer(String path, String script) throws Exception {
		LOG.info(">>>>>>>>>>>> checkstartRestServer, {}, {}", path, script);
		BufferedReader reader = null;
		try {
			ProcessBuilder builder = new ProcessBuilder();
			builder.command("bash", "-c", script + " &");
			//				builder.command(kafkaTopicsScript + " --list --bootstrap-server " + kafkaBootstrapServer);

			//				builder.command(kafkaTopicsScript, "--list", "--bootstrap-server", kafkaBootstrapServer);

			builder.directory(new File(path));
			Process startRestServerProcess = builder.start();

			AtomicBoolean running = new AtomicBoolean(false);
			
//			ExecutorService kafkaStartExecutor = Executors.newSingleThreadExecutor();
//			kafkaStartExecutor.submit(new Runnable() {
//
//				@Override
//				public void run() {
//					BufferedReader reader = new BufferedReader(new InputStreamReader(startRestServerProcess.getInputStream()));
//					reader.lines().forEach(line -> {
//						LOG.info(line);
//						if (StringUtils.contains(line, "Started Application")) {
//							running.set(true);
//							return;
//						}  
//					});
//				}
//
//			});
			
			
			int exitVal = startRestServerProcess.waitFor();
			if (exitVal == 0) {
				LOG.info(">>> startRestServer exitcode={}", exitVal);
			} else {
				LOG.error(">>> startRestServer Error!!!  exitcode={}", exitVal);

			}
			
			
//			while (!running.get()) {
//				LOG.info(">>>>>>WAITING 1 sec FOR FINISH");
//				Thread.sleep(10000);
//			}
			return running.get();

		} finally {
			if (reader != null) reader.close();
		}

	}
	public void killServers() throws Exception {
		int kafkaRestPortNum = Integer.valueOf(kafkaRestPort);
		int logminerRestPortNum = Integer.valueOf(logminerRestPort);
		int healthRestPortNum = Integer.valueOf(healthRestPort);
		int partycontactRestPortNum = Integer.valueOf(partycontactRestPort);
		
		boolean listening = false;
		// kill process of kafka-rest
		listening = checkPortListening(kafkaRestPortNum);
		LOG.info(">>>>>>>port {} is listening:{}", kafkaRestPortNum,listening);
		if (listening) {
			killProcess(kafkaRestPortNum);

			// check 
			listening = checkPortListening(kafkaRestPortNum);
			if (listening) {
				throw new Exception("Port:" + kafkaRestPortNum + " cannot be killed.!!!");
			}
		}

		// kill process of kafka-rest
		listening = checkPortListening(logminerRestPortNum);
		LOG.info(">>>>>>>port {} is listening:{}", logminerRestPortNum, listening);
		if (listening) {
			killProcess(logminerRestPortNum);
			// check 
			listening = checkPortListening(logminerRestPortNum);
			if (listening) {
				throw new Exception("Port:" + logminerRestPortNum + " cannot be killed.!!!");
			}
		}

		// kill process of kafka-rest
		listening = checkPortListening(healthRestPortNum);
		LOG.info(">>>>>>>port {} is listening:{}", healthRestPortNum, listening);
		if (listening) {
			killProcess(healthRestPortNum);
			// check 
			listening = checkPortListening(healthRestPortNum);
			if (listening) {
				throw new Exception("Port:" + healthRestPortNum + " cannot be killed.!!!");
			}
		}

		// kill process of kafka-rest
		listening = checkPortListening(partycontactRestPortNum);
		LOG.info(">>>>>>>port {} is listening:{}", partycontactRestPortNum, listening);
		if (listening) {
			killProcess(partycontactRestPortNum);
			// check 
			listening = checkPortListening(partycontactRestPortNum);
			if (listening) {
				throw new Exception("Port:" + partycontactRestPortNum + " cannot be killed.!!!");
			}
		}
	}
	private void killProcess(int port) throws Exception {
		ProcessBuilder builder = new ProcessBuilder();
		String script = String.format("kill -9 $(lsof -t -i:%d -sTCP:LISTEN)", port);
		LOG.info(">>> stop script={}", script);

		builder.command("bash", "-c", script);
		builder.directory(new File("."));

		Process killProcess = builder.start();

		int exitVal = killProcess.waitFor();
		if (exitVal == 0) {
			LOG.info(">>> Kill Port:{} Success!!! ", port);
		} else {
			LOG.error(">>> Kill Port:{}  Error!!!  exitcode={}", port, exitVal);

		}
		if (killProcess.isAlive()) {
			killProcess.destroy();
		}

	}
	private boolean checkPortListening(int port) throws Exception {
		LOG.info(">>>>>>>>>>>> checkPortListening:{} ", port);

		BufferedReader reader = null;
		try {
			ProcessBuilder builder = new ProcessBuilder();
			String script = "netstat -tnlp | grep :" + port;
			builder.command("bash", "-c", script);
			//				builder.command(kafkaTopicsScript + " --list --bootstrap-server " + kafkaBootstrapServer);

			//				builder.command(kafkaTopicsScript, "--list", "--bootstrap-server", kafkaBootstrapServer);

			builder.directory(new File("."));
			Process checkPortProcess = builder.start();

			AtomicBoolean portRunning = new AtomicBoolean(false);


			int exitVal = checkPortProcess.waitFor();
			if (exitVal == 0) {
				reader = new BufferedReader(new InputStreamReader(checkPortProcess.getInputStream()));
				reader.lines().forEach(line -> {
					if (StringUtils.contains(line, "LISTEN")) {
						portRunning.set(true);
						LOG.info(">>> Success!!! portRunning.set(true)");
					}
				});
				reader.close();

				LOG.info(">>> Success!!! portRunning={}", portRunning.get());
			} else {
				LOG.error(">>> Error!!!  exitcode={}", exitVal);


			}
			if (checkPortProcess.isAlive()) {
				checkPortProcess.destroy();
			}

			return portRunning.get();
		} finally {
			if (reader != null) reader.close();
		}



	}
}
