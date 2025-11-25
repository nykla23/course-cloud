package com.zjgsu.obl.catalog_service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Embedded;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class Instructor {
    private String id;
    private String name;
    private String email;


    public Instructor() {}
    public Instructor(String id, String name, String email){
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
}