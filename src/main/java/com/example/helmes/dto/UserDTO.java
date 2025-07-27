package com.example.helmes.dto;

import java.util.List;

public class UserDTO {
    private Long id;
    private String name;
    private List<Integer> sectors;
    private Boolean agreeToTerms;

    // Getters and setters
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

    public List<Integer> getSectors() {
        return sectors;
    }

    public void setSectors(List<Integer> sectors) {
        this.sectors = sectors;
    }

    public Boolean isAgreeToTerms() {
        return agreeToTerms;
    }

    public void setAgreeToTerms(Boolean agreeToTerms) {
        this.agreeToTerms = agreeToTerms;
    }
}
