package com.piligrim.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "city")
public class City {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "city_id")
	private long city_id;

	@Column(name = "city_name")
	private String city_name;

	@ManyToOne
	@JoinColumn(name = "state_id")
	private State state;

	public long getCity_id() {
		return city_id;
	}

	public void setCity_id(long city_id) {
		this.city_id = city_id;
	}

	public String getCity_name() {
		return city_name;
	}

	public void setCity_name(String city_name) {
		this.city_name = city_name;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public City(long city_id, String city_name, State state) {
		super();
		this.city_id = city_id;
		this.city_name = city_name;
		this.state = state;
	}

	public City() {
		super();
		// TODO Auto-generated constructor stub
	}

}
