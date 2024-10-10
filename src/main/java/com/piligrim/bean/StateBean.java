package com.piligrim.bean;

import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Named;
import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import com.piligrim.entity.Country;
import com.piligrim.entity.State;

@SessionScoped
@Named
public class StateBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@PersistenceUnit(unitName = "jsf-jpa")
	private EntityManagerFactory entityManagerFactory;

	private EntityManager entityManager;
	private State newState;
	private List<State> states;
	private String searchName;
	private List<Country> countries;
	private String selectedCountryCode;
	private String selectedCountryName;
	private String searchStateName;
	private State selectedState; // Newly added property

	@PostConstruct
	public void init() {
		entityManager = entityManagerFactory.createEntityManager();
		loadCountries();
		states = loadStates();
		newState = new State();
	}

	// Retrieve Data
	public List<State> loadStates() {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<State> cq = cb.createQuery(State.class);
		Root<State> stateRoot = cq.from(State.class);
		stateRoot.fetch("country", JoinType.LEFT); // Fetch country details if needed
		cq.select(stateRoot);
		TypedQuery<State> query = entityManager.createQuery(cq);
		return query.getResultList();
	}

	// Load Countries for Dropdown
	public void loadCountries() {
		try {
			CriteriaBuilder cb = entityManager.getCriteriaBuilder();
			CriteriaQuery<Country> cq = cb.createQuery(Country.class);
			Root<Country> countryRoot = cq.from(Country.class);
			cq.select(countryRoot);
			TypedQuery<Country> query = entityManager.createQuery(cq);
			countries = query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to load countries."));
		}
	}

	// Handle Country Code Change for Add and Edit Forms
	public void onCountryCodeChange(AjaxBehaviorEvent event) {
		try {
			TypedQuery<Country> query = entityManager
					.createQuery("SELECT c FROM Country c WHERE c.country_code = :country_code", Country.class);
			query.setParameter("country_code", selectedCountryCode);
			List<Country> result = query.getResultList();

			if (!result.isEmpty()) {
				Country country = result.get(0);
				selectedCountryName = country.getCountry_name();
				newState.setCountry(country);

				if (selectedState != null) {
					selectedState.setCountry(country);
				}
			} else {
				selectedCountryName = "";
				newState.setCountry(null);

				if (selectedState != null) {
					selectedState.setCountry(null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to change country."));
		}
	}

	// Save New State
	public void saveState() {
		try {
			entityManager.getTransaction().begin();
			entityManager.persist(newState);
			entityManager.getTransaction().commit();
			states = loadStates();
			newState = new State();
			selectedCountryCode = null;
			selectedCountryName = null;

			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "State saved successfully!"));
		} catch (Exception e) {
			if (entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to save state!"));
		}
	}

	public void searchStatesByName() {
		if (searchStateName == null || searchStateName.isEmpty()) {
			states = loadStates();
			return;
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<State> cq = cb.createQuery(State.class);
		Root<State> stateRoot = cq.from(State.class);
		cq.select(stateRoot)
				.where(cb.like(cb.lower(stateRoot.get("state_name")), "%" + searchStateName.toLowerCase() + "%"));
		TypedQuery<State> query = entityManager.createQuery(cq);
		states = query.getResultList();
	}

	// Reset Search
	public void resetSearch() {
		searchStateName = "";
		states = loadStates();
	}

	// Delete State
	public void deleteState(long state_id) {
		try {
			entityManager.getTransaction().begin();
			State stateToDelete = entityManager.find(State.class, state_id);
			if (stateToDelete != null) {
				// Assuming there is a City entity related to State
				entityManager.createQuery("DELETE FROM City c WHERE c.state.state_id = :state_id")
						.setParameter("state_id", state_id).executeUpdate();

				entityManager.remove(stateToDelete);
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Success", "State and its cities deleted successfully!"));
			} else {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "State not found."));
			}
			entityManager.getTransaction().commit();
			states = loadStates();
		} catch (Exception e) {
			if (entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error deleting state."));
		}
	}

	// Load State for Editing
	public void loadStateForEdit(long stateId) {
		try {
			entityManager.getTransaction().begin();
			selectedState = entityManager.find(State.class, stateId);

			if (selectedState == null) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "State not found."));
			} else {
				// Initialize country details if needed
				selectedState.getCountry().getCountry_name();
			}

			entityManager.getTransaction().commit();
		} catch (Exception e) {
			if (entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to load state."));
		}
	}

	// Update Existing State
	public void updateState() {
		try {
			entityManager.getTransaction().begin();
			entityManager.merge(selectedState);
			entityManager.getTransaction().commit();

			// Refresh the list of states to reflect the update
			states = loadStates();

			// Reset the selectedState
			selectedState = null;

			// Add a success message
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "State updated successfully!"));
		} catch (Exception e) {
			if (entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to update state."));
		}
	}

	// Getters and Setters

	public State getNewState() {
		return newState;
	}

	public void setNewState(State newState) {
		this.newState = newState;
	}

	public List<State> getStates() {
		return states;
	}

	public void setStates(List<State> states) {
		this.states = states;
	}

	public String getSearchName() {
		return searchName;
	}

	public void setSearchName(String searchName) {
		this.searchName = searchName;
	}

	public List<Country> getCountries() {
		return countries;
	}

	public void setCountries(List<Country> countries) {
		this.countries = countries;
	}

	public String getSelectedCountryCode() {
		return selectedCountryCode;
	}

	public void setSelectedCountryCode(String selectedCountryCode) {
		this.selectedCountryCode = selectedCountryCode;
	}

	public String getSelectedCountryName() {
		return selectedCountryName;
	}

	public void setSelectedCountryName(String selectedCountryName) {
		this.selectedCountryName = selectedCountryName;
	}

	public String getSearchStateName() {
		return searchStateName;
	}

	public void setSearchStateName(String searchStateName) {
		this.searchStateName = searchStateName;
	}

	public State getSelectedState() {
		return selectedState;
	}

	public void setSelectedState(State selectedState) {
		this.selectedState = selectedState;
	}
}
