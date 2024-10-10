package com.piligrim.entity;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "state")
public class State {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "state_id")
	private long state_id;

	@Column(name = "state_name")
	private String state_name;

	@ManyToOne
	@JoinColumn(name = "country_id")
	private Country country;

	@OneToMany(mappedBy = "state", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<City> cities;

	public long getState_id() {
		return state_id;
	}

	public void setState_id(long state_id) {
		this.state_id = state_id;
	}

	public String getState_name() {
		return state_name;
	}

	public void setState_name(String state_name) {
		this.state_name = state_name;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public List<City> getCities() {
		return cities;
	}

	public void setCities(List<City> cities) {
		this.cities = cities;
	}

	public State(long state_id, String state_name, Country country) {
		super();
		this.state_id = state_id;
		this.state_name = state_name;
		this.country = country;
	}

	public State() {
		super();
		// TODO Auto-generated constructor stub
	}

}
