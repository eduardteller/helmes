package com.example.helmes.dto;

import java.util.ArrayList;
import java.util.List;

// sector dto class, entry has children array instead of parent id as in the Sector model
public class SectorDTO {
    private Long id;
    private String name;
    private List<SectorDTO> children = new ArrayList<>();

    public SectorDTO() {
    }

    public SectorDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

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

    public List<SectorDTO> getChildren() {
        return children;
    }

    public void setChildren(List<SectorDTO> children) {
        this.children = children;
    }

    public void addChild(SectorDTO child) {
        this.children.add(child);
    }
}
