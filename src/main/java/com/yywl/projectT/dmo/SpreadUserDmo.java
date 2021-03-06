package com.yywl.projectT.dmo;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="spread_user")
public class SpreadUserDmo implements Serializable{

	private static final long serialVersionUID = 3059469511922590679L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	private Long userId;
	
	private Double latitude1=0.0;
	
	private Double latitude2=0.0;
	
	private Double Longitude1=0.0;
	
	private Double Longitude2=0.0;

	public SpreadUserDmo() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Double getLatitude1() {
		return latitude1;
	}

	public void setLatitude1(Double latitude1) {
		this.latitude1 = latitude1;
	}

	public Double getLatitude2() {
		return latitude2;
	}

	public void setLatitude2(Double latitude2) {
		this.latitude2 = latitude2;
	}

	public Double getLongitude1() {
		return Longitude1;
	}

	public void setLongitude1(Double longitude1) {
		Longitude1 = longitude1;
	}

	public Double getLongitude2() {
		return Longitude2;
	}

	public void setLongitude2(Double longitude2) {
		Longitude2 = longitude2;
	}

	
}
