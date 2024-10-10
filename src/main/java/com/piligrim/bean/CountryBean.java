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

import com.piligrim.entity.Country;

@SessionScoped
@Named
public class CountryBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@PersistenceUnit(unitName = "jsf-jpa")
	private EntityManagerFactory entityManagerFactory;

	private EntityManager entityManager;
	private Country newCountry;
	private List<Country> countries;
	private String searchCountryName;
	private Country selectedCountry;

	@PostConstruct
	public void init() {
		entityManager = entityManagerFactory.createEntityManager();
		countries = loadCountries();
		newCountry = new Country();
	}

	// Retrieve Data
	public List<Country> loadCountries() {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Country> cq = cb.createQuery(Country.class);
		Root<Country> countryRoot = cq.from(Country.class);
		cq.select(countryRoot);
		TypedQuery<Country> query = entityManager.createQuery(cq);
		return query.getResultList();
	}

	public void saveCountry() {
		try {
			entityManager.getTransaction().begin();
			entityManager.persist(newCountry);
			entityManager.getTransaction().commit();
			countries = loadCountries();
			newCountry = new Country();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Country saved successfully!"));
		} catch (Exception e) {
			if (entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to save country!"));
		}
	}

	public void searchCountriesByName() {
		if (searchCountryName == null || searchCountryName.isEmpty()) {
			countries = loadCountries();
			return;
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Country> cq = cb.createQuery(Country.class);
		Root<Country> countryRoot = cq.from(Country.class);
		cq.select(countryRoot)
				.where(cb.like(cb.lower(countryRoot.get("country_name")), "%" + searchCountryName.toLowerCase() + "%"));
		TypedQuery<Country> query = entityManager.createQuery(cq);
		countries = query.getResultList();
	}

	public void resetSearch() {
		searchCountryName = "";
		countries = loadCountries();
	}

	public void deleteCountry(long countryId) {
		EntityTransaction transaction = null;
		try {
			transaction = entityManager.getTransaction();
			transaction.begin();

			Country countryToDelete = entityManager.find(Country.class, countryId);
			if (countryToDelete != null) {
				entityManager.remove(countryToDelete);
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage("Country and its related states and cities deleted successfully!"));
			} else {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Country not found."));
			}

			transaction.commit();
			countries = loadCountries();
		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to delete country."));
		}
	}

	// Load country for editing
	public void loadCountryForEdit(long countryId) {
		try {
			selectedCountry = entityManager.find(Country.class, countryId);
			if (selectedCountry == null) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Country not found."));
			}
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to load country."));
		}
	}

	// Update the selected country
	public void updateCountry() {
		EntityTransaction transaction = null;

		try {
			transaction = entityManager.getTransaction();
			transaction.begin();

			entityManager.merge(selectedCountry);
			transaction.commit();
			countries = loadCountries(); // Refresh the country list
			selectedCountry = null; // Clear selected country after update
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Country updated successfully!"));
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to update country."));
		}
	}

	// Getters and Setters
	public Country getNewCountry() {
		return newCountry;
	}

	public void setNewCountry(Country newCountry) {
		this.newCountry = newCountry;
	}

	public List<Country> getCountries() {
		return countries;
	}

	public void setCountries(List<Country> countries) {
		this.countries = countries;
	}

	public String getSearchCountryName() {
		return searchCountryName;
	}

	public void setSearchCountryName(String searchCountryName) {
		this.searchCountryName = searchCountryName;
	}

	public Country getSelectedCountry() {
		return selectedCountry;
	}

	public void setSelectedCountry(Country selectedCountry) {
		this.selectedCountry = selectedCountry;
	}

}
