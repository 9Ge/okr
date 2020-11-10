package com.pm.okr.model.entity;


import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "objective_link_count")
public class ObjectiveLinkCountEntity {

    Integer id;
    Integer year;
    Integer season;
    Integer containerType;
    Integer containerId;
    Integer linkupCount;
    Integer NotLinkupCount;
    Timestamp createTime;
    Timestamp updateTime;
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

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getSeason() {
        return season;
    }

    public void setSeason(Integer season) {
        this.season = season;
    }

    public Integer getContainerType() {
        return containerType;
    }

    public void setContainerType(Integer containerType) {
        this.containerType = containerType;
    }

    public Integer getContainerId() {
        return containerId;
    }

    public void setContainerId(Integer containerId) {
        this.containerId = containerId;
    }

    public Integer getLinkupCount() {
        return linkupCount;
    }

    public void setLinkupCount(Integer linkupCount) {
        this.linkupCount = linkupCount;
    }

    public Integer getNotLinkupCount() {
        return NotLinkupCount;
    }

    public void setNotLinkupCount(Integer notLinkupCount) {
        NotLinkupCount = notLinkupCount;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}
