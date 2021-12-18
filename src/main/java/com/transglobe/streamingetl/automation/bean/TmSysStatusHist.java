package com.transglobe.streamingetl.automation.bean;

import java.sql.Timestamp;

public class TmSysStatusHist {
	
	private Timestamp insertTime;
	private Timestamp lgmnrLstRcvdHeartbeatTime;
	private Timestamp lgmnrLstRcvd;
	private Timestamp cnsmrLstRcvdHeartbeatTime;
	private Timestamp cnsmrLstRcvd;
	private String cnsmrLstRcvdClient;
	private String kafkaStatus;
	private String logminerStatus;
	private String connectStatus;
	public Timestamp getInsertTime() {
		return insertTime;
	}
	public void setInsertTime(Timestamp insertTime) {
		this.insertTime = insertTime;
	}
	public Timestamp getLgmnrLstRcvdHeartbeatTime() {
		return lgmnrLstRcvdHeartbeatTime;
	}
	public void setLgmnrLstRcvdHeartbeatTime(Timestamp lgmnrLstRcvdHeartbeatTime) {
		this.lgmnrLstRcvdHeartbeatTime = lgmnrLstRcvdHeartbeatTime;
	}
	public Timestamp getLgmnrLstRcvd() {
		return lgmnrLstRcvd;
	}
	public void setLgmnrLstRcvd(Timestamp lgmnrLstRcvd) {
		this.lgmnrLstRcvd = lgmnrLstRcvd;
	}
	public Timestamp getCnsmrLstRcvdHeartbeatTime() {
		return cnsmrLstRcvdHeartbeatTime;
	}
	public void setCnsmrLstRcvdHeartbeatTime(Timestamp cnsmrLstRcvdHeartbeatTime) {
		this.cnsmrLstRcvdHeartbeatTime = cnsmrLstRcvdHeartbeatTime;
	}
	public Timestamp getCnsmrLstRcvd() {
		return cnsmrLstRcvd;
	}
	public void setCnsmrLstRcvd(Timestamp cnsmrLstRcvd) {
		this.cnsmrLstRcvd = cnsmrLstRcvd;
	}
	public String getCnsmrLstRcvdClient() {
		return cnsmrLstRcvdClient;
	}
	public void setCnsmrLstRcvdClient(String cnsmrLstRcvdClient) {
		this.cnsmrLstRcvdClient = cnsmrLstRcvdClient;
	}
	public String getKafkaStatus() {
		return kafkaStatus;
	}
	public void setKafkaStatus(String kafkaStatus) {
		this.kafkaStatus = kafkaStatus;
	}
	public String getLogminerStatus() {
		return logminerStatus;
	}
	public void setLogminerStatus(String logminerStatus) {
		this.logminerStatus = logminerStatus;
	}
	public String getConnectStatus() {
		return connectStatus;
	}
	public void setConnectStatus(String connectStatus) {
		this.connectStatus = connectStatus;
	}
	
	
	
}
