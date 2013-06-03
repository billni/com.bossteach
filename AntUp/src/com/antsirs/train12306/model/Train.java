package com.antsirs.train12306.model;

import java.util.Date;

import com.google.appengine.api.datastore.Key;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@NamedQueries({
		@NamedQuery(name = "listTrain", query = "SELECT m FROM Train m"),
		@NamedQuery(name = "findTrainById", query = "SELECT m FROM Train m WHERE m.trainId = :trainId") })
public class Train {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Key trainId;
	private String trainNo;
	private String fromStation;
	private String toStation;
	private String departureTime;
	private String arrvialTime;
	private String during;
	private Date insertTime;

	public Train() {
	}

	public Key getTrainId() {
		return trainId;
	}

	public void setTrainId(Key trainId) {
		this.trainId = trainId;
	}

	public String getFromStation() {
		return fromStation;
	}

	public void setFromStation(String fromStation) {
		this.fromStation = fromStation;
	}

	public String getToStation() {
		return toStation;
	}

	public void setToStation(String toStation) {
		this.toStation = toStation;
	}

	public String getDepartureTime() {
		return departureTime;
	}

	public void setDepartureTime(String departureTime) {
		this.departureTime = departureTime;
	}

	public String getArrvialTime() {
		return arrvialTime;
	}

	public void setArrvialTime(String arrvialTime) {
		this.arrvialTime = arrvialTime;
	}

	public String getDuring() {
		return during;
	}

	public void setDuring(String during) {
		this.during = during;
	}

	public String getTrainNo() {
		return trainNo;
	}

	public void setTrainNo(String trainNo) {
		this.trainNo = trainNo;
	}

	@Temporal(TemporalType.DATE)
	public Date getInsertTime() {
		return insertTime;
	}

	public void setInsertTime(Date insertTime) {
		this.insertTime = insertTime;
	}

}