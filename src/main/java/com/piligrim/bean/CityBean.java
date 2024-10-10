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

import com.piligrim.entity.City;
import com.piligrim.entity.Country;
import com.piligrim.entity.State;

@Named
@SessionScoped
public class CityBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceUnit(unitName = "jsf-jpa")
    private EntityManagerFactory entityManagerFactory;

    private EntityManager entityManager;

    private City newCity;
    private List<City> cities;
    private List<State> states;
    private List<Country> countries;

    private String selectedCountryCode;
    private String selectedCountryName;
    private Long selectedStateId;
    private String searchCityName;

    private City selectedCity; // For editing

    @PostConstruct
    public void init() {
        entityManager = entityManagerFactory.createEntityManager();
        newCity = new City();
        loadCountries();
        loadCities();
    }

    // 1. Load All Countries
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

    // 2. Load All Cities with Joined State and Country
    public void loadCities() {
        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<City> cq = cb.createQuery(City.class);
            Root<City> cityRoot = cq.from(City.class);
            // Join with State and Country
            cityRoot.fetch("state", JoinType.LEFT).fetch("country", JoinType.LEFT);
            cq.select(cityRoot);
            TypedQuery<City> query = entityManager.createQuery(cq);
            cities = query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to load cities."));
        }
    }

    // 3. Load States Based on Selected Country
    public void loadStatesForCountry(AjaxBehaviorEvent event) {
        if (selectedCountryCode != null && !selectedCountryCode.isEmpty()) {
            try {
                CriteriaBuilder cb = entityManager.getCriteriaBuilder();
                CriteriaQuery<State> cq = cb.createQuery(State.class);
                Root<State> stateRoot = cq.from(State.class);
                cq.select(stateRoot).where(cb.equal(stateRoot.get("country").get("country_code"), selectedCountryCode));
                TypedQuery<State> query = entityManager.createQuery(cq);
                states = query.getResultList();

                // Set selectedCountryName based on selectedCountryCode
                for (Country country : countries) {
                    if (country.getCountry_code().equals(selectedCountryCode)) {
                        selectedCountryName = country.getCountry_name();
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to load states."));
            }
        } else {
            states = null;
            selectedCountryName = null;
        }
    }

    // 4. Save New City
    public void saveCity() {
        EntityTransaction transaction = null;
        try {
            transaction = entityManager.getTransaction();
            transaction.begin();

            // Fetch the selected state entity
            State state = entityManager.find(State.class, selectedStateId);
            if (state != null) {
                newCity.setState(state);
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Selected state not found."));
                return;
            }

            entityManager.persist(newCity);
            transaction.commit();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "City saved successfully!"));

            // Reset fields and reload cities
            resetCityForm();
            loadCities();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to save city!"));
        }
    }

    // 5. Search Cities by Name
    public void searchCitiesByName() {
        if (searchCityName == null || searchCityName.trim().isEmpty()) {
            loadCities();
            return;
        }

        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<City> cq = cb.createQuery(City.class);
            Root<City> cityRoot = cq.from(City.class);
            cityRoot.fetch("state", JoinType.LEFT).fetch("country", JoinType.LEFT);
            cq.select(cityRoot)
                    .where(cb.like(cb.lower(cityRoot.get("city_name")), "%" + searchCityName.toLowerCase() + "%"));
            TypedQuery<City> query = entityManager.createQuery(cq);
            cities = query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to search cities."));
        }
    }

    // 6. Reset Search and Reload Cities
    public void resetSearch() {
        searchCityName = "";
        loadCities();
    }

    // 7. Delete City
    public void deleteCity(Long cityId) {
        EntityTransaction transaction = null;
        try {
            transaction = entityManager.getTransaction();
            transaction.begin();

            City cityToDelete = entityManager.find(City.class, cityId);
            if (cityToDelete != null) {
                entityManager.remove(cityToDelete);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "City deleted successfully!"));
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "City not found."));
            }

            transaction.commit();
            loadCities();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to delete city!"));
        }
    }

    // 8. Load City for Editing
    public void loadCityForEdit(Long cityId) {
        try {
            selectedCity = entityManager.find(City.class, cityId);
            if (selectedCity != null) {
                // Set selectedCountryCode and selectedStateId based on selectedCity
                selectedCountryCode = selectedCity.getState().getCountry().getCountry_code();
                selectedStateId = selectedCity.getState().getState_id();

                // Load states for the selected country
                loadStatesForCountry(null);
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "City not found."));
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to load city for editing."));
        }
    }

    // 9. Update Existing City
    public void updateCity() {
        EntityTransaction transaction = null;
        try {
            transaction = entityManager.getTransaction();
            transaction.begin();

            // Fetch the selected state entity
            State state = entityManager.find(State.class, selectedStateId);
            if (state != null) {
                selectedCity.setState(state);
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Selected state not found."));
                return;
            }

            entityManager.merge(selectedCity);
            transaction.commit();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "City updated successfully!"));

            // Reset fields and reload cities
            resetCityForm();
            loadCities();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to update city!"));
        }
    }

    // 10. Reset City Form
    public void resetCityForm() {
        newCity = new City();
        selectedCity = null;
        selectedCountryCode = null;
        selectedCountryName = null;
        selectedStateId = null;
        states = null;
    }

    // 11. Getters and Setters

    public City getNewCity() {
        return newCity;
    }

    public void setNewCity(City newCity) {
        this.newCity = newCity;
    }

    public List<City> getCities() {
        return cities;
    }

    public void setCities(List<City> cities) {
        this.cities = cities;
    }

    public List<State> getStates() {
        return states;
    }

    public void setStates(List<State> states) {
        this.states = states;
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

    public Long getSelectedStateId() {
        return selectedStateId;
    }

    public void setSelectedStateId(Long selectedStateId) {
        this.selectedStateId = selectedStateId;
    }

    public String getSearchCityName() {
        return searchCityName;
    }

    public void setSearchCityName(String searchCityName) {
        this.searchCityName = searchCityName;
    }

    public City getSelectedCity() {
        return selectedCity;
    }

    public void setSelectedCity(City selectedCity) {
        this.selectedCity = selectedCity;
    }
}
