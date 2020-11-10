package com.pm.okr.model.entity;

import javax.persistence.*;

@Entity
@Table(name = "company")
public class CompanyEntity {
    Integer id;
    String name;
    Integer mu;

    public Integer getMu() {
        return mu;
    }

    public void setMu(Integer mu) {
        this.mu = mu;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
