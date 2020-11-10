package com.pm.okr.model.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "objectives")
public class ObjectiveEntity {
    Integer id;
    String content;
    String color;
    Integer year;
    Integer season;
    Double progress = 0d;
    Double weight = 1d;
    Boolean archived = false;
    Integer assignFrom;
    Integer containerType;
    Integer containerId;
    Integer linkAbove;
    Timestamp createTime;
    Timestamp linkTime;
    List<KeyResultEntity> keyResults;
    List<ObjectiveEntity> linkBelows;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public Double getProgress() {
        return progress;
    }

    public void setProgress(Double progress) {
        this.progress = progress;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

    public Integer getAssignFrom() {
        return assignFrom;
    }

    public void setAssignFrom(Integer assignFrom) {
        this.assignFrom = assignFrom;
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

    public Integer getLinkAbove() {
        return linkAbove;
    }

    public void setLinkAbove(Integer linkAbove) {
        this.linkAbove = linkAbove;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    @OneToMany(cascade = {CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinColumn(name = "objId", referencedColumnName = "id")
    public List<KeyResultEntity> getKeyResults() {
        return keyResults;
    }

    public void setKeyResults(List<KeyResultEntity> keyResults) {
        this.keyResults = keyResults;
    }

    @OneToMany(mappedBy="linkAbove", cascade=CascadeType.REFRESH, fetch = FetchType.LAZY)
    public List<ObjectiveEntity> getLinkBelows() {
        return linkBelows;
    }

    public void setLinkBelows(List<ObjectiveEntity> linkBelows) {
        this.linkBelows = linkBelows;
    }

    public Timestamp getLinkTime() {
        return linkTime;
    }

    public void setLinkTime(Timestamp linkTime) {
        this.linkTime = linkTime;
    }
}
