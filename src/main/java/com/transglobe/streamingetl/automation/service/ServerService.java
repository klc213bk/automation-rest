package com.transglobe.streamingetl.automation.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transglobe.streamingetl.automation.util.HttpUtils;


@Service
public class ServerService {
	static final Logger LOG = LoggerFactory.getLogger(ServerService.class);

	@Value("${tglminer.db.driver}")
	private String tglminerDbDriver;

	@Value("${tglminer.db.url}")
	private String tglminerDbUrl;
	
	@Value("${tglminer.db.username}")
	private String tglminerDbUsername;
	
	@Value("${tglminer.db.password}")
	private String tglminerDbPassword;
	
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

	@Value("${sever.restart.check.url}")
	private String serverRestartCheckUrl;

	@Value("${data.log.dirs}")
	private String dataLogDirs;

	@Value("${house.keeping.days}")
	private String houseKeepingDaysStr;
	
	private ScheduledExecutorService scheduledRestartingCheckExecutor;
	private ScheduledFuture<?> scheduledRestartingCheck;

	private ScheduledExecutorService scheduledHouseKeepingExecutor;
	private ScheduledFuture<?> scheduledHouseKeeping;
	
	private AtomicBoolean restarting = new AtomicBoolean(false);

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

		LOG.info(">>>>> startRestServer Done!!! ");
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


		LOG.info(">>>>> stopRestServer Done!!!");
	}
	public void startRestServer(String path, String script) throws Exception {
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

		}

		// kill process of kafka-rest
		listening = checkPortListening(logminerRestPortNum);
		LOG.info(">>>>>>>port {} is listening:{}", logminerRestPortNum, listening);
		if (listening) {
			killProcess(logminerRestPortNum);

		}

		// kill process of kafka-rest
		listening = checkPortListening(healthRestPortNum);
		LOG.info(">>>>>>>port {} is listening:{}", healthRestPortNum, listening);
		if (listening) {
			killProcess(healthRestPortNum);

		}

		// kill process of kafka-rest
		listening = checkPortListening(partycontactRestPortNum);
		LOG.info(">>>>>>>port {} is listening:{}", partycontactRestPortNum, listening);
		if (listening) {
			killProcess(partycontactRestPortNum);

		}
	}
	private void executeCommand(String directory, String script) throws Exception {
		LOG.info(">>> executeSCommand script={}", script);

		ProcessBuilder builder = new ProcessBuilder();

		builder.command(script);
		builder.directory(new File(directory));

		Process process = builder.start();

		int exitVal = process.waitFor();
		if (exitVal == 0) {
			LOG.info(">>>   Success!!! ");
		} else {
			LOG.error(">>>   Error!!!  exitcode={}",exitVal);

		}

		if (process.isAlive()) {
			process.destroy();
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
		while (checkPortListening(port)) {
			Thread.sleep(1000);
			LOG.info(">>>> Sleep for 1 second");;
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
	public void startRestartCheck() throws Exception {
		LOG.info(">>>>>>>>>>>> startRestartCheck...");

		scheduledRestartingCheckExecutor = Executors.newScheduledThreadPool(1);

		Runnable checkRestarting = () -> {
			Connection conn = null;
			CallableStatement cstmt = null;

			try {	
				restarting.set(true);
				
				String response = HttpUtils.restService(serverRestartCheckUrl, "GET");
				ObjectMapper objectMapper = new ObjectMapper();

				JsonNode jsonNode = objectMapper.readTree(response);
				boolean restart = jsonNode.get("restart").asBoolean();

				LOG.info("restart={}", restart);

				long t0, t1;
				String script = null;
				if (restart) {
					LOG.info("execute STOP scripts ...");

					t0 = System.currentTimeMillis();
					script = "./shutdown-partycontact.sh";
					executeCommand("bin", script);
					t1 = System.currentTimeMillis();
					LOG.info("executeCommand '{}' Done, take {} ms!!!" , script, (t1 - t0));

					Thread.sleep(5000);

					t0 = System.currentTimeMillis();
					script = "./shutdown-base.sh";
					executeCommand("bin", script);
					t1 = System.currentTimeMillis();
					LOG.info("executeCommand '{}' Done, take {} ms!!!" , script, (t1 - t0));

					Thread.sleep(15000);
					
					LOG.info("execute START scripts ...");

					t0 = System.currentTimeMillis();
					script = "./start-kafka.sh";
					executeCommand("bin", script);
					t1 = System.currentTimeMillis();
					LOG.info("executeCommand '{}' Done, take {} ms!!!" , script, (t1 - t0));

					Thread.sleep(15000);

					t0 = System.currentTimeMillis();
					script = "./startup-base.sh";
					executeCommand("bin", script);
					t1 = System.currentTimeMillis();
					LOG.info("executeCommand '{}' Done, take {} ms!!!" , script, (t1 - t0));

					Thread.sleep(5000);

					t0 = System.currentTimeMillis();
					script = "./cleanup-and-initialization-partycontact.sh";
					executeCommand("bin", script);
					t1 = System.currentTimeMillis();
					LOG.info("executeCommand '{}' Done, take {} ms!!!" , script, (t1 - t0));

					Thread.sleep(5000);

					t0 = System.currentTimeMillis();
					script = "./startup-partycontact-base.sh";
					executeCommand("bin", script);
					t1 = System.currentTimeMillis();
					LOG.info("executeCommand '{}' Done, take {} ms!!!" , script, (t1 - t0));

					Thread.sleep(5000);

					t0 = System.currentTimeMillis();
					script = "./startup-partycontact-loaddata.sh";
					executeCommand("bin", script);
					t1 = System.currentTimeMillis();
					LOG.info("executeCommand '{}' Done, take {} ms!!!" , script, (t1 - t0));

					Thread.sleep(5000);

					t0 = System.currentTimeMillis();
					script = "./startup-partycontact-consumer.sh";
					executeCommand("bin", script);
					t1 = System.currentTimeMillis();
					LOG.info("executeCommand '{}' Done, take {} ms!!!" , script, (t1 - t0));

					Thread.sleep(5000);


				}

			} catch (Exception e1) {
				String errMsg = ExceptionUtils.getMessage(e1);
				String stackTrace = ExceptionUtils.getStackTrace(e1);
				LOG.error(">>> err msg:{}, stacktrace={}", errMsg, stackTrace);
			} finally {
				if (cstmt != null) {
					try {
						cstmt.close();
					} catch (SQLException e1) {
						String errMsg = ExceptionUtils.getMessage(e1);
						String stackTrace = ExceptionUtils.getStackTrace(e1);
						LOG.error(">>> err msg:{}, stacktrace={}", errMsg, stackTrace);
					}
				}
				if (conn != null) {
					try {
						conn.close();
					} catch (SQLException e) {
						String errMsg = ExceptionUtils.getMessage(e);
						String stackTrace = ExceptionUtils.getStackTrace(e);
						LOG.error(">>> err msg:{}, stacktrace={}", errMsg, stackTrace);
					}
				}
				restarting.set(false);
			}	

		};
		// initial delay = 5, repeat the task every 60 seconds
		scheduledRestartingCheck = scheduledRestartingCheckExecutor.scheduleAtFixedRate(checkRestarting, 10, 60, TimeUnit.SECONDS);


		//		Runtime.getRuntime().addShutdownHook(new Thread() {
		//			@Override
		//			public void run() {
		//
		//				stopHeartbeat();
		//
		//			}
		//		});
		//
		//		return result;


	}
	public void stopRestartCheck() {
		LOG.info(">>>>>>>>>>>> stopRestartCheck ");

		scheduledRestartingCheck.cancel(true);

		scheduledRestartingCheckExecutor.shutdown();


		LOG.info(">>>>>>>>>>>> stopRestartCheck done !!!");

	}
	public void startHouseKeeping() throws Exception {
		LOG.info(">>>>>>>>>>>> startHouseKeeping...");

		scheduledHouseKeepingExecutor = Executors.newScheduledThreadPool(1);

		final int houseKeepingDays = Integer.valueOf(houseKeepingDaysStr);
		
		Runnable houseKeepingTask = () -> {

			Connection conn = null;
			PreparedStatement pstmt = null;
			String sql = null;
			try {	
				// log file house keeping
				String[] logDirArr = dataLogDirs.split(",");

				Pattern datePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
				Matcher matcher = null;
				for (String logDir : logDirArr) {
					File dir = new File(logDir);

					File[] files = dir.listFiles();
					if (files != null) {
//						System.out.println(logDir + ", length:" + files.length); 
						for (File file : files) {
							matcher = datePattern.matcher(file.getName());
							while (matcher.find()) {
//								System.out.println(file.getName() + ",macther.group:" + matcher.group());	
								LocalDate filedate = LocalDate.parse(matcher.group());
//								System.out.println("filedate:" + filedate);

								LocalDate now = LocalDate.now();

								LocalDate oneWeekBefore = now.minusDays(houseKeepingDays);
//								System.out.println("oneWeekBefore:" + oneWeekBefore);

								if (filedate.isBefore(oneWeekBefore)) {
									Files.delete(Paths.get(file.getAbsolutePath()));
									LOG.info("file:{} deleted" ,file.getName());
									
								}
							}

						}
					}

				}

				Class.forName(tglminerDbDriver);
				conn = DriverManager.getConnection(tglminerDbUrl, tglminerDbUsername, tglminerDbPassword);

				LocalDate now = LocalDate.now();
				LocalDate theDate = now.minusDays(houseKeepingDays);
				Timestamp theTimestamp = Timestamp.valueOf(theDate.atStartOfDay());
				
				sql = "delete from TM_HEARTBEAT where HEARTBEAT_TIME < ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, theTimestamp);
				pstmt.executeUpdate();
				pstmt.close();
				
				sql = "delete from TM_HEALTH where HEARTBEAT_TIME < ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, theTimestamp);
				pstmt.executeUpdate();
				pstmt.close();
				
				sql = "delete from TM_HEALTH where START_TIME < ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, theTimestamp);
				pstmt.executeUpdate();
				pstmt.close();
				
				sql = "delete from TM_PAYLOAD_LOG where INSERT_TIME < ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, theTimestamp);
				pstmt.executeUpdate();
				pstmt.close();
				
				conn.close();
				
			} catch (Exception e1) {
				LOG.error(">>> err msg:{}, stacktrace={}", ExceptionUtils.getMessage(e1), ExceptionUtils.getStackTrace(e1));
			} finally {
				if (pstmt != null) {
					try {
						pstmt.close();
					} catch (SQLException e) {
						LOG.error(">>> err msg:{}, stacktrace={}", ExceptionUtils.getMessage(e), ExceptionUtils.getStackTrace(e));

					}
				}
				if (conn != null) {
					try {
						conn.close();
					} catch (SQLException e) {
						LOG.error(">>> err msg:{}, stacktrace={}", ExceptionUtils.getMessage(e), ExceptionUtils.getStackTrace(e));

					}
				}
			}	

		};
		// initial delay = 30, repeat the task every day
		scheduledHouseKeeping = scheduledHouseKeepingExecutor.scheduleAtFixedRate(houseKeepingTask, 0, 1, TimeUnit.DAYS);


		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {

				LOG.info(">>>>>>>>>>>> ShutdownHook is calleddd, call stopHouseKeeping() !!!");
				stopHouseKeeping();

			}
		});


	}
	public void stopHouseKeeping() {
		LOG.info(">>>>>>>>>>>> stopHouseKeeping ");

		scheduledHouseKeeping.cancel(true);

		scheduledHouseKeepingExecutor.shutdown();


		LOG.info(">>>>>>>>>>>> stopHouseKeeping done !!!");

	}
}
