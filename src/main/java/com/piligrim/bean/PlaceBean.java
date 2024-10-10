package com.piligrim.bean;

import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceUnit;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import com.piligrim.entity.Place;

@SessionScoped
@Named
public class PlaceBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@PersistenceUnit(unitName = "jsf-jpa")
	private EntityManagerFactory entityManagerFactory;

	private EntityManager entityManager;
	private Place newPlace;
	private List<Place> places;
	private String searchPlaceName;
	private Place selectedPlace;

	@PostConstruct
	public void init() {
		entityManager = entityManagerFactory.createEntityManager();
		places = loadPlaces();
		newPlace = new Place();
	}

	// Retrieve Data
	public List<Place> loadPlaces() {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Place> cq = cb.createQuery(Place.class);
		Root<Place> placeRoot = cq.from(Place.class);
		cq.select(placeRoot);
		TypedQuery<Place> query = entityManager.createQuery(cq);
		return query.getResultList();
	}

	public void savePlace() {
		try {
			if (entityManager == null) {
				System.out.println("EntityManager is null.");
				return;
			}
			if (newPlace.getPlace_name() == null || newPlace.getPlace_name().trim().isEmpty()) {
				System.out.println("Place name cannot be null or empty.");
				return;
			}
			entityManager.getTransaction().begin();
			System.out.println("Saving Place: " + newPlace);
			entityManager.persist(newPlace);
			entityManager.getTransaction().commit();
			places.add(newPlace);
			newPlace = new Place();
		} catch (Exception e) {
			if (entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
			e.printStackTrace();
		}
	}

	public void deletePlace(long placeId) {
		EntityTransaction transaction = null;
		try {
			transaction = entityManager.getTransaction();
			transaction.begin();

			Place placeToDelete = entityManager.find(Place.class, placeId);
			if (placeToDelete != null) {
				entityManager.remove(placeToDelete);
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("place deleted successfully!"));
			} else {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Place not found."));
			}

			transaction.commit();
			places = loadPlaces();
		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to delete Place."));
		}
	}

	public void searchPlacesByName() {
		System.out.println("Searching for: " + searchPlaceName); // Debugging line
		if (searchPlaceName == null || searchPlaceName.isEmpty()) {
			places = loadPlaces();
			return;
		}
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Place> cq = cb.createQuery(Place.class);
		Root<Place> placeRoot = cq.from(Place.class);
		cq.select(placeRoot)
				.where(cb.like(cb.lower(placeRoot.get("place_name")), "%" + searchPlaceName.toLowerCase() + "%"));
		TypedQuery<Place> query = entityManager.createQuery(cq);
		places = query.getResultList();
		System.out.println("Search results count: " + places.size());
	}

	public void resetSearch() {
		searchPlaceName = "";
		places = loadPlaces();
	}

	public void loadPlaceForEdit(long placeId) {
		try {
			selectedPlace = entityManager.find(Place.class, placeId);
			if (selectedPlace == null) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Place not found."));
			}
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to load place."));
		}
	}

	public void updatePlace() {
		try {
			entityManager.getTransaction().begin();
			entityManager.merge(selectedPlace);
			entityManager.getTransaction().commit();
			places = loadPlaces();
			selectedPlace = null;
		} catch (Exception e) {
			if (entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
			e.printStackTrace();
		}
	}

	public Place getNewPlace() {
		return newPlace;
	}

	public void setNewPlace(Place newPlace) {
		this.newPlace = newPlace;
	}

	public List<Place> getPlaces() {
		return places;
	}

	public void setPlaces(List<Place> places) {
		this.places = places;
	}

	public String getSearchPlaceName() {
		return searchPlaceName;
	}

	public void setSearchPlaceName(String searchPlaceName) {
		this.searchPlaceName = searchPlaceName;
	}

	public Place getSelectedPlace() {
		return selectedPlace;
	}

	public void setSelectedPlace(Place selectedPlace) {
		this.selectedPlace = selectedPlace;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
