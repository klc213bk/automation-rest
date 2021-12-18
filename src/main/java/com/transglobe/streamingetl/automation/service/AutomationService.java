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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
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
import com.transglobe.streamingetl.automation.bean.TmSysStatusHist;
import com.transglobe.streamingetl.automation.util.HttpUtils;


@Service
public class AutomationService {
	static final Logger LOG = LoggerFactory.getLogger(AutomationService.class);

	private static String KAFKA_STATUS_RUNNING = "RUNNING";
	private static String LOGMINER_STATUS_RUNNING = "RUNNING";
	private static String CONNECT_STATUS_RUNNING = "RUNNING";

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

	@Value("${kafka.bootstrap.server.port}")
	private String kafkaBootstrapServerPort;

	@Value("${kafka.zookeeper.server.port}")
	private String kafkaZookeeperServerPort;

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

	@Value("${data.log.dirs}")
	private String dataLogDirs;

	@Value("${house.keeping.days}")
	private String houseKeepingDaysStr;

	@Value("${restart.allowance.period.bizhour}")
	private String restartAllowancePeriodBizhour;

	@Value("${restart.consumer.allowance.period.bizhour}")
	private String restartConsumerAllowancePeriodBizhour;

	private ScheduledExecutorService scheduledRestartingCheckExecutor;
	private ScheduledFuture<?> scheduledRestartingCheck;

	private ScheduledExecutorService scheduledHouseKeepingExecutor;
	private ScheduledFuture<?> scheduledHouseKeeping;

	private AtomicBoolean restarting = new AtomicBoolean(false);

	private int restartingPhase = 0; // 1, 2

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
			PreparedStatement pstmt = null;
			String sql = null;
			ResultSet rs = null;
			try {	

				if (restarting.get()) {
					return;
				}

				restarting.set(true);

				Class.forName(tglminerDbDriver);
				conn = DriverManager.getConnection(tglminerDbUrl, tglminerDbUsername, tglminerDbPassword);


				sql = "select INSERT_TIME,LGMNR_LST_RCVD_HEARTBEAT_TIME,\n" + 
						"	LGMNR_LST_RCVD,CNSMR_LST_RCVD_HEARTBEAT_TIME,\n" + 
						"	CNSMR_LST_RCVD,CNSMR_LST_RCVD_CLIENT,\n" + 
						"	KAFKA_STATUS,LOGMINER_STATUS,CONNECT_STATUS \n" +
						" from TM_SYS_STATUS_HIST \n" +
						" order by INSERT_TIME DESC fetch next 1 row only";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				TmSysStatusHist tmSysStatusHist = null;
				while (rs.next()) {
					tmSysStatusHist = new TmSysStatusHist();
					tmSysStatusHist.setInsertTime(rs.getTimestamp("INSERT_TIME"));
					tmSysStatusHist.setLgmnrLstRcvdHeartbeatTime(rs.getTimestamp("LGMNR_LST_RCVD_HEARTBEAT_TIME"));
					tmSysStatusHist.setLgmnrLstRcvd(rs.getTimestamp("LGMNR_LST_RCVD"));
					tmSysStatusHist.setCnsmrLstRcvdHeartbeatTime(rs.getTimestamp("CNSMR_LST_RCVD_HEARTBEAT_TIME"));
					tmSysStatusHist.setCnsmrLstRcvd(rs.getTimestamp("CNSMR_LST_RCVD"));
					tmSysStatusHist.setCnsmrLstRcvdClient(rs.getString("CNSMR_LST_RCVD_CLIENT"));
					tmSysStatusHist.setKafkaStatus(rs.getString("KAFKA_STATUS"));
					tmSysStatusHist.setLogminerStatus(rs.getString("LOGMINER_STATUS"));
					tmSysStatusHist.setConnectStatus(rs.getString("CONNECT_STATUS"));
				}
				rs.close();
				pstmt.close();

				LOG.info("tmSysStatusHist={}", ToStringBuilder.reflectionToString(tmSysStatusHist));

				boolean restart = checkRestart(tmSysStatusHist);

				LOG.info("need to restart={}", restart);

				int kafkaRestPortNum = Integer.valueOf(kafkaRestPort);
				int logminerRestPortNum = Integer.valueOf(logminerRestPort);
				int healthRestPortNum = Integer.valueOf(healthRestPort);
				int partycontactRestPortNum = Integer.valueOf(partycontactRestPort);
				int kafkaBootstrapServerPortNum = Integer.valueOf(kafkaBootstrapServerPort);
				int kafkaZookeeperServerPortNum = Integer.valueOf(kafkaZookeeperServerPort);

				
				long t0, t1;
				String script = null;
				String	response = null;
				if (restart) {
					
					LOG.info("restartingPhase={}", restartingPhase);
					if (restartingPhase == 0) {
						LOG.info("execute STOP scripts ...");

						if (checkPortListening(partycontactRestPortNum)) {
							t0 = System.currentTimeMillis();

							script = "./shutdown-partycontact.sh";
							executeCommand("bin", script);
							t1 = System.currentTimeMillis();
							LOG.info("executeCommand '{}' Done, take {} ms!!!" , script, (t1 - t0));

							Thread.sleep(5000);
						}
						if (checkPortListening(healthRestPortNum)) {
							t0 = System.currentTimeMillis();

							script = "./shutdown-health.sh";
							executeCommand("bin", script);
							t1 = System.currentTimeMillis();
							LOG.info("executeCommand '{}' Done, take {} ms!!!" , script, (t1 - t0));

							Thread.sleep(5000);
						}
						if (checkPortListening(logminerRestPortNum)) {
							t0 = System.currentTimeMillis();

							script = "./shutdown-logminer.sh";
							executeCommand("bin", script);
							t1 = System.currentTimeMillis();
							LOG.info("executeCommand '{}' Done, take {} ms!!!" , script, (t1 - t0));

							Thread.sleep(5000);
						}
						if (checkPortListening(kafkaRestPortNum)) {
							t0 = System.currentTimeMillis();
							script = "./shutdown-kafka.sh";
							executeCommand("bin", script);
							t1 = System.currentTimeMillis();
							LOG.info("executeCommand '{}' Done, take {} ms!!!" , script, (t1 - t0));

							Thread.sleep(15000);
						}
						LOG.info("execute START scripts ...");

						if (!checkPortListening(kafkaRestPortNum)) {
							t0 = System.currentTimeMillis();
							script = "./startup-kafka.sh";
							executeCommand("bin", script);
							t1 = System.currentTimeMillis();
							LOG.info("executeCommand '{}' Done, take {} ms!!!" , script, (t1 - t0));

							Thread.sleep(15000);
							
							if (!checkPortListening(kafkaZookeeperServerPortNum)
									|| !checkPortListening(kafkaBootstrapServerPortNum)) {
								// shutdown and restart kafka again
								t0 = System.currentTimeMillis();
								script = "./shutdown-kafka.sh";
								executeCommand("bin", script);
								t1 = System.currentTimeMillis();
								LOG.info("executeCommand '{}' Done, take {} ms!!!" , script, (t1 - t0));

								Thread.sleep(20000);
								
								t0 = System.currentTimeMillis();
								script = "./startup-kafka.sh";
								executeCommand("bin", script);
								t1 = System.currentTimeMillis();
								LOG.info("executeCommand '{}' Done, take {} ms!!!" , script, (t1 - t0));

								Thread.sleep(15000);
								
							}

						}
						if (!checkPortListening(logminerRestPortNum)) {
							t0 = System.currentTimeMillis();
							script = "./startup-logminer-with-reset.sh";
							executeCommand("bin", script);
							t1 = System.currentTimeMillis();
							LOG.info("executeCommand '{}' Done, take {} ms!!!" , script, (t1 - t0));

							Thread.sleep(15000);
						}
						if (!checkPortListening(healthRestPortNum)) {
							t0 = System.currentTimeMillis();
							script = "./startup-health.sh";
							executeCommand("bin", script);
							t1 = System.currentTimeMillis();
							LOG.info("executeCommand '{}' Done, take {} ms!!!" , script, (t1 - t0));

							Thread.sleep(15000);
						}

						restartingPhase = 1;
						LOG.info(">>> set restartingPhase={}", restartingPhase);
					} else if (restartingPhase == 1) {
						LOG.warn(">>> restart pahase 1 failed. reset restartingPhase = 0 and will restart at next cycle");
						restartingPhase = 0;
						LOG.info(">>> set restartingPhase={}", restartingPhase);
					} else if (restartingPhase == 2) {
						LOG.warn(">>> restart pahase 2 failed. reset restartingPhase = 0 and will restart at next cycle");
						restartingPhase = 0;
						LOG.info(">>> set restartingPhase={}", restartingPhase);
					}
				} else {
					if (restartingPhase == 0) {
						LOG.info("This is a normal case ");
					} else if (restartingPhase == 1) {
						LOG.warn(">>> restart pahase 1 successfully. go restart phase 2 ");

						//						t0 = System.currentTimeMillis();
						//						script = "./cleanup-and-initialization-partycontact.sh";
						//						executeCommand("bin", script);
						//						t1 = System.currentTimeMillis();
						//						LOG.info("executeCommand '{}' Done, take {} ms!!!" , script, (t1 - t0));
						//
						//						Thread.sleep(5000);
						//
						//						t0 = System.currentTimeMillis();
						//						script = "./startup-partycontact-loaddata.sh";
						//						executeCommand("bin", script);
						//						t1 = System.currentTimeMillis();
						//						LOG.info("executeCommand '{}' Done, take {} ms!!!" , script, (t1 - t0));
						//
						//						Thread.sleep(5000);

						if (!checkPortListening(partycontactRestPortNum)) {
							t0 = System.currentTimeMillis();
							response =  HttpUtils.restService("http://localhost:9100/server/start/partycontact-rest", "POST");
							t1 = System.currentTimeMillis();
							LOG.info(">>> cleanup Done !!! take {} ms!!!, response={}" , (t1 - t0), response);

							Thread.sleep(10000);
						}
						
						t0 = System.currentTimeMillis();
						response =  HttpUtils.restService("http://localhost:9201/partycontact/cleanup", "POST");
						t1 = System.currentTimeMillis();
						LOG.info(">>> cleanup Done !!! take {} ms!!!, response={}" , (t1 - t0), response);

						Thread.sleep(5000);

						t0 = System.currentTimeMillis();
						response =  HttpUtils.restService("http://localhost:9201/partycontact/initialize", "POST");
						t1 = System.currentTimeMillis();
						LOG.info(">>> initialize Done !!! take {} ms!!! , response={}" , (t1 - t0), response);

						Thread.sleep(5000);

						t0 = System.currentTimeMillis();
						response =  HttpUtils.restService("http://localhost:9201/partycontact/applyLogminerSync", "POST");
						t1 = System.currentTimeMillis();
						LOG.info(">>> applyLogminerSync Done !!! take {} ms!!!, response={}" , (t1 - t0), response);

						Thread.sleep(5000);

						t0 = System.currentTimeMillis();
						response =  HttpUtils.restService("http://localhost:9201/partycontact/loadAllData", "POST");
						t1 = System.currentTimeMillis();
						LOG.info(">>> loadAllData Done !!! take {} ms!!!, response={}" , (t1 - t0), response);

						Thread.sleep(5000);

						t0 = System.currentTimeMillis();
						response =  HttpUtils.restService("http://localhost:9201/partycontact/addPrimaryKey", "POST");
						t1 = System.currentTimeMillis();
						LOG.info(">>> addPrimaryKey Done !!! take {} ms!!!, response={}" , (t1 - t0), response);

						Thread.sleep(5000);

						t0 = System.currentTimeMillis();
						response =  HttpUtils.restService("http://localhost:9201/partycontact/createIndexes", "POST");
						t1 = System.currentTimeMillis();
						LOG.info(">>> createIndexes Done !!! take {} ms!!!, response={}" , (t1 - t0), response);

						Thread.sleep(5000);

						t0 = System.currentTimeMillis();
						response =  HttpUtils.restService("http://localhost:9201/consumer/startPartyContactConsumer", "POST");
						t1 = System.currentTimeMillis();
						LOG.info(">>> startPartyContactConsumer Done !!! take {} ms!!!, response={}" , (t1 - t0), response);



						//						t0 = System.currentTimeMillis();
						//						script = "./startup-partycontact-consumer.sh";
						//						executeCommand("bin", script);
						//						t1 = System.currentTimeMillis();
						//						LOG.info("executeCommand '{}' Done, take {} ms!!!" , script, (t1 - t0));

						Thread.sleep(10000);

						restartingPhase = 2;
					} else if (restartingPhase == 2) {
						LOG.info(">>> restart pahase 2 successfully. Whole restarting process succeeds. reset restartingPhase = 0 ");
						restartingPhase = 0;
					} else {
						LOG.error(">>> Cannot goes here, error!!! restartingPhase={}", restartingPhase);
					}

				}

			} catch (Exception e1) {
				String errMsg = ExceptionUtils.getMessage(e1);
				String stackTrace = ExceptionUtils.getStackTrace(e1);
				LOG.error(">>> err msg:{}, stacktrace={}", errMsg, stackTrace);
			} finally {
				if (pstmt != null) {
					try {
						pstmt.close();
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

				sql = "delete from TM_LOGMINER_OFFSET where START_TIME < ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, theTimestamp);
				pstmt.executeUpdate();
				pstmt.close();

				sql = "delete from TM_PAYLOAD_LOG where INSERT_TIME < ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, theTimestamp);
				pstmt.executeUpdate();
				pstmt.close();

				sql = "delete from TM_SYS_STATUS_HIST where INSERT_TIME < ?";
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
	private boolean checkRestart(TmSysStatusHist tmSysStatusHist) throws Exception {
		LOG.info(">>>>>>>>>>>> checkRestart ...");

		boolean restarting = false;
		if (tmSysStatusHist == null || tmSysStatusHist.getLgmnrLstRcvd() == null) {
			restarting = false;
		} else {
			Timestamp lgmnrLastReceived = tmSysStatusHist.getLgmnrLstRcvd();
			long lastReceivedMs = lgmnrLastReceived.getTime();
			long nowms = System.currentTimeMillis();
			LOG.info(">>>nowms - lastReceivedMs = {}", nowms - lastReceivedMs);

			if (KAFKA_STATUS_RUNNING.equals(tmSysStatusHist.getKafkaStatus())
					&& LOGMINER_STATUS_RUNNING.equals(tmSysStatusHist.getLogminerStatus())
					&& CONNECT_STATUS_RUNNING.equals(tmSysStatusHist.getConnectStatus())) {
				LOG.info(">>>servers are all RUNNING OK");
				int thresholdCosumer = Integer.valueOf(restartConsumerAllowancePeriodBizhour);


				LocalDateTime localDateTime = LocalDateTime.now();
				int hour = localDateTime.getHour();

				if (nowms - lastReceivedMs > thresholdCosumer * 1000 ) {
					LOG.info(">>>server ok, now - lastReceivedMs {} > {} seconds", nowms - lastReceivedMs, thresholdCosumer);

					if (hour >= 8 && hour < 20) {
						LOG.info(">>>server ok, now - lastReceivedMs {} > {} seconds, business hour, restarting=true", nowms - lastReceivedMs, thresholdCosumer);
						restarting = true;
					} else {
						LOG.info(">>>server ok, now - lastReceivedMs {} > {} seconds, non business hour", nowms - lastReceivedMs, thresholdCosumer);
						if (hour == 7 ) {
							LOG.info(">>> 7 oclock set restarting = true");
							restarting = true;
						} else {
							LOG.info(">>> non business hour set restarting = false");
							restarting = false;
						}
					}
				} else {
					restarting = false;
					LOG.info(">>>serverok, dnowms - lastReceivedMs {} < threshold, do nothing, keep waiting , threshold: {} seconds", nowms - lastReceivedMs, thresholdCosumer);
				}
			} else {
				LOG.info(">>>servers have Failure!!!");
				LocalDateTime localDateTime = LocalDateTime.now();
				int hour = localDateTime.getHour();

				if (hour >= 8 && hour < 20) {
					// business hour
					int thresholdServer = Integer.valueOf(restartAllowancePeriodBizhour);
					if (nowms - lastReceivedMs > thresholdServer * 1000 ) {
						LOG.info(">>>Although server fails, set restarting = true, > threshold:{}, keep waiting for {} seconds", nowms - lastReceivedMs, thresholdServer);
						restarting = true;
					} else {
						restarting = false;
						LOG.info(">>>Although server fails, do nothing, less than threshold:{}, keep waiting for {} seconds", nowms - lastReceivedMs, thresholdServer);
					}
				} else {
					if (hour == 7 ) {
						LOG.info(">>> hour={}, = 7 oclock hour oclock set restarting = true", hour);
						restarting = true;
					} else if (hour == 20 || hour == 23 || hour == 2 || hour == 5) {
						LOG.info(">>> hour={}, != 7 oclock, restart hour set restarting = true", hour);
						restarting = true;
					} else {
						LOG.info(">>> hour={}, != 7 oclock, not restart hour set restarting = false", hour);
						restarting = false;
					}

				}
			}
		}

		return restarting;
	}
}
