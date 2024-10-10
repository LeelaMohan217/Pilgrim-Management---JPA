package com.piligrim.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.piligrim.entity.City;
import com.piligrim.entity.Country;
import com.piligrim.entity.Pilgrim;
import com.piligrim.entity.Place;
import com.piligrim.entity.State;

@Named
@ViewScoped
public class PilgrimBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@PersistenceUnit(unitName = "jsf-jpa")
	private EntityManagerFactory emf;

	private EntityManager entityManager;

	private Pilgrim newPilgrim;
	private List<Pilgrim> pilgrims;
	private String searchPilgrimName;
	private Pilgrim selectedPilgrim;

	private ArrayList<String> typesOfDarshan;

	private List<Country> countries;
	private List<State> states;
	private List<City> cities;

	private Long selectedCountryId;
	private Long selectedStateId;
	private Long selectedCityId;
	private Long selectedPlaceId; // Added property for Place

	private List<Place> places;

	@PostConstruct
	public void init() {
		// Initialize EntityManager
		entityManager = emf.createEntityManager();

		// Load initial data
		pilgrims = loadPilgrims();
		newPilgrim = new Pilgrim();
		places = loadPlaces();
		typesOfDarshan = new ArrayList<>();
		typesOfDarshan.add("Free");
		typesOfDarshan.add("Paid");

		// Load countries on initialization
		countries = loadCountries();
	}

	// Method to load all countries
	private List<Country> loadCountries() {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Country> cq = cb.createQuery(Country.class);
		Root<Country> countryRoot = cq.from(Country.class);
		cq.select(countryRoot).orderBy(cb.asc(countryRoot.get("country_name")));
		return entityManager.createQuery(cq).getResultList();
	}

	// Method to load states based on selected country
	public void onCountryChange() {
		if (selectedCountryId != null && selectedCountryId != 0) {
			states = loadStates(selectedCountryId);
		} else {
			states = new ArrayList<>();
		}
		// Reset cities and place when country changes
		cities = new ArrayList<>();
		selectedStateId = null;
		selectedCityId = null;
		selectedPlaceId = null;
	}

	// Method to load cities based on selected state
	public void onStateChange() {
		if (selectedStateId != null && selectedStateId != 0) {
			cities = loadCities(selectedStateId);
		} else {
			cities = new ArrayList<>();
		}
		// Reset place when state changes
		selectedCityId = null;
		selectedPlaceId = null;
	}

	// Helper method to load states by country ID
	private List<State> loadStates(Long countryId) {
		TypedQuery<State> query = entityManager.createQuery(
				"SELECT s FROM State s WHERE s.country.country_id = :countryId ORDER BY s.state_name", State.class);
		query.setParameter("countryId", countryId);
		return query.getResultList();
	}

	// Helper method to load cities by state ID
	private List<City> loadCities(Long stateId) {
		TypedQuery<City> query = entityManager
				.createQuery("SELECT c FROM City c WHERE c.state.state_id = :stateId ORDER BY c.city_name", City.class);
		query.setParameter("stateId", stateId);
		return query.getResultList();
	}

	private List<Place> loadPlaces() {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Place> cq = cb.createQuery(Place.class);
		Root<Place> placeRoot = cq.from(Place.class);
		cq.select(placeRoot).orderBy(cb.asc(placeRoot.get("place_name")));
		return entityManager.createQuery(cq).getResultList();
	}

	public List<Pilgrim> loadPilgrims() {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Pilgrim> cq = cb.createQuery(Pilgrim.class);
		Root<Pilgrim> pilgrimRoot = cq.from(Pilgrim.class);
		cq.select(pilgrimRoot);
		TypedQuery<Pilgrim> query = entityManager.createQuery(cq);
		return query.getResultList();
	}

	// Getters and Setters
	public List<Country> getCountries() {
		return countries;
	}

	public List<State> getStates() {
		return states;
	}

	public List<City> getCities() {
		return cities;
	}

	public Long getSelectedCountryId() {
		return selectedCountryId;
	}

	public void setSelectedCountryId(Long selectedCountryId) {
		this.selectedCountryId = selectedCountryId;
	}

	public Long getSelectedStateId() {
		return selectedStateId;
	}

	public void setSelectedStateId(Long selectedStateId) {
		this.selectedStateId = selectedStateId;
	}

	public Long getSelectedCityId() {
		return selectedCityId;
	}

	public void setSelectedCityId(Long selectedCityId) {
		this.selectedCityId = selectedCityId;
	}

	public Long getSelectedPlaceId() {
		return selectedPlaceId;
	}

	public void setSelectedPlaceId(Long selectedPlaceId) {
		this.selectedPlaceId = selectedPlaceId;
	}

	public Pilgrim getNewPilgrim() {
		return newPilgrim;
	}

	public void setNewPilgrim(Pilgrim newPilgrim) {
		this.newPilgrim = newPilgrim;
	}

	public List<Pilgrim> getPilgrims() {
		return pilgrims;
	}

	public void setPilgrims(List<Pilgrim> pilgrims) {
		this.pilgrims = pilgrims;
	}

	public String getSearchPilgrimName() {
		return searchPilgrimName;
	}

	public void setSearchPilgrimName(String searchPilgrimName) {
		this.searchPilgrimName = searchPilgrimName;
	}

	public Pilgrim getSelectedPilgrim() {
		return selectedPilgrim;
	}

	public void setSelectedPilgrim(Pilgrim selectedPilgrim) {
		this.selectedPilgrim = selectedPilgrim;
	}

	public List<Place> getPlaces() {
		return places;
	}

	public void setPlaces(List<Place> places) {
		this.places = places;
	}

	public ArrayList<String> getTypesOfDarshan() {
		return typesOfDarshan;
	}

	public void setTypesOfDarshan(ArrayList<String> typesOfDarshan) {
		this.typesOfDarshan = typesOfDarshan;
	}

	// Save method with manual transaction management
	public void savePilgrim() {
		EntityManager em = null;
		EntityTransaction transaction = null;
		try {
			// Create a new EntityManager for this operation
			em = emf.createEntityManager();
			transaction = em.getTransaction();
			transaction.begin();

			// Set associations based on selected IDs
			if (selectedCountryId != null && selectedCountryId != 0) {
				Country country = em.find(Country.class, selectedCountryId);
				newPilgrim.setCountry(country);
			}
			if (selectedStateId != null && selectedStateId != 0) {
				State state = em.find(State.class, selectedStateId);
				newPilgrim.setState(state);
			}
			if (selectedCityId != null && selectedCityId != 0) {
				City city = em.find(City.class, selectedCityId);
				newPilgrim.setCity(city);
			}
			if (selectedPlaceId != null && selectedPlaceId != 0) {
				Place place = em.find(Place.class, selectedPlaceId);
				newPilgrim.setPlace(place);
			}

			// Persist the new Pilgrim
			em.persist(newPilgrim);

			// Commit the transaction
			transaction.commit();

			// Close the EntityManager
			em.close();

			// Refresh the pilgrim list
			pilgrims = loadPilgrims();

			// Reset the newPilgrim object
			newPilgrim = new Pilgrim();
			selectedCountryId = null;
			selectedStateId = null;
			selectedCityId = null;
			selectedPlaceId = null;

			// Add success message
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Pilgrim saved successfully!"));

		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			if (em != null && em.isOpen()) {
				em.close();
			}
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to save Pilgrim!"));
		}
	}

	public void searchPilgrim() {
		if (searchPilgrimName != null && !searchPilgrimName.trim().isEmpty()) {
			CriteriaBuilder cb = entityManager.getCriteriaBuilder();
			CriteriaQuery<Pilgrim> cq = cb.createQuery(Pilgrim.class);
			Root<Pilgrim> pilgrimRoot = cq.from(Pilgrim.class);
			cq.select(pilgrimRoot)
					.where(cb.like(cb.lower(pilgrimRoot.get("name")), "%" + searchPilgrimName.toLowerCase() + "%"));
			pilgrims = entityManager.createQuery(cq).getResultList();
		} else {
			pilgrims = loadPilgrims();
		}
	}

	public void resetSearch() {
		searchPilgrimName = null;
		pilgrims = loadPilgrims();
	}

	public void deletePilgrim(long pilgrimId) {
		EntityTransaction transaction = null;
		try {
			transaction = entityManager.getTransaction();
			transaction.begin();
			Pilgrim pilgrimToDelete = entityManager.find(Pilgrim.class, pilgrimId);
			if (pilgrimToDelete != null) {
				entityManager.remove(pilgrimToDelete);
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Pilgrim deleted successfully!"));
			} else {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Unable to delete!"));
			}
			transaction.commit();
			pilgrims = loadPilgrims();
		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to delete pilgrim."));
		}
	}
	
	public void loadPilgrimForEdit(long pilgrimId) {
        try {
            entityManager.getTransaction().begin();
            selectedPilgrim = entityManager.find(Pilgrim.class, pilgrimId);
            if (selectedPilgrim == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Pilgrim not found."));
            }
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to load pilgrim."));
        }
    }
	
	public void updatePilgrim() {
        try {
            entityManager.getTransaction().begin();
            entityManager.merge(selectedPilgrim);
            entityManager.getTransaction().commit();
            pilgrims = loadPilgrims();
            selectedPilgrim = null;
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Pilgrim updated successfully!"));
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to update pilgrim."));
        }
    }
}
