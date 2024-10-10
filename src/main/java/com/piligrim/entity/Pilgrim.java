package com.piligrim.entity;

import javax.persistence.*;

@Entity
@Table(name = "pilgrim")
public class Pilgrim {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "age", nullable = false)
	private int age;

	@Column(name = "gender", nullable = false)
	private String gender;

	@Column(name = "aadhar", nullable = false)
	private String aadhar;

	@Column(name = "mobile", nullable = false)
	private Long mobile;

	@Column(name = "darshan", nullable = false)
	private String darshan;

	@ManyToOne
	@JoinColumn(name = "country_id")
	private Country country;

	@ManyToOne
	@JoinColumn(name = "state_id")
	private State state;

	@ManyToOne
	@JoinColumn(name = "city_id")
	private City city;

	@ManyToOne
	@JoinColumn(name = "place_id")
	private Place place;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getAadhar() {
		return aadhar;
	}

	public void setAadhar(String aadhar) {
		this.aadhar = aadhar;
	}

	public Long getMobile() {
		return mobile;
	}

	public void setMobile(Long mobile) {
		this.mobile = mobile;
	}

	public String getDarshan() {
		return darshan;
	}

	public void setDarshan(String darshan) {
		this.darshan = darshan;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public City getCity() {
		return city;
	}

	public void setCity(City city) {
		this.city = city;
	}

	public Place getPlace() {
		return place;
	}

	public void setPlace(Place place) {
		this.place = place;
	}

	public Pilgrim(Long id, String name, int age, String gender, String aadhar, Long mobile, String darshan,
			Country country, State state, City city, Place place) {
		super();
		this.id = id;
		this.name = name;
		this.age = age;
		this.gender = gender;
		this.aadhar = aadhar;
		this.mobile = mobile;
		this.darshan = darshan;
		this.country = country;
		this.state = state;
		this.city = city;
		this.place = place;
	}

	public Pilgrim() {
		super();
		// TODO Auto-generated constructor stub
	}

}
