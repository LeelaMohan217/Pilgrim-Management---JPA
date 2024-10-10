package com.piligrim.entity;

import javax.persistence.*;

@Entity
@Table(name = "places")
public class Place {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "place_id", nullable = false)
	private Long place_id;

	@Column(name = "place_name", nullable = false)
	private String place_name;

	public Long getPlace_id() {
		return place_id;
	}

	public void setPlace_id(Long place_id) {
		this.place_id = place_id;
	}

	public String getPlace_name() {
		return place_name;
	}

	public void setPlace_name(String place_name) {
		this.place_name = place_name;
	}

	public Place(Long place_id, String place_name) {
		super();
		this.place_id = place_id;
		this.place_name = place_name;
	}

	public Place() {
		super();
		// TODO Auto-generated constructor stub
	}

}
