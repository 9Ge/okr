package com.pm.okr.common;

import com.pm.okr.controller.vo.*;
import com.pm.okr.model.entity.CompanyEntity;
import com.pm.okr.model.entity.ObjectiveEntity;
import com.pm.okr.model.entity.TeamEntity;
import com.pm.okr.model.entity.UserEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ObjectiveMapper {


    public interface EntityLoader {
        CompanyEntity getCompany(Integer cid);

        TeamEntity getTeam(Integer tid);

        UserEntity getUser(Integer uid);

        ObjectiveEntity getObjective(Integer oId);
    }

    public interface BeforeMapping {
        boolean filter(ObjectiveEntity oe);
    }

    public static Objective.Owner getOwner(Integer type, Integer id, EntityLoader loader) {
        Objective.Owner owner = new Objective.Owner();
        switch (type) {
            case ObjectiveContainer.COMPANY:
                CompanyEntity ce = loader.getCompany(id);
                if (ce == null) {
                    return null;
                }
                owner.setCompany(BeanUtil.fill(ce, new Company.Part()));
                break;
            case ObjectiveContainer.TEAM:
                TeamEntity te = loader.getTeam(id);
                if (te == null) {
                    return null;
                }
                owner.setTeam(BeanUtil.fill(te, new Team.Part()));
                break;
            case ObjectiveContainer.USER:
                UserEntity ue = loader.getUser(id);
                if (ue == null) {
                    return null;
                }
                owner.setUser(BeanUtil.fill(ue, new User.PartShort()));
                break;
            default:
                return null;
        }
        return owner;
    }

    public static Objective.PartToLink toPartToLink(ObjectiveEntity oe, EntityLoader loader) {
        Objective.PartToLink ob = BeanUtil.fill(oe, new Objective.PartToLink());
        ob.setOwner(getOwner(oe.getContainerType(), oe.getContainerId(), loader));
        return ob;
    }

    public static List<Objective.PartToLink> toPartToLinks(List<ObjectiveEntity> objs, EntityLoader loader) {
        List<Objective.PartToLink> ret = new ArrayList<>();
        for (ObjectiveEntity obj : objs) {
            Objective.PartToLink pl = toPartToLink(obj, loader);
            if (pl.getOwner() != null) {
                ret.add(pl);
            }
        }
        return ret;
    }

    static Objective.Owner getOwner(ObjectiveEntity oe, EntityLoader loader){
        ObjectiveEntity oeAssign = null;
        //由 KR 分配生成的 Objective， Owner 需要显示为分配者
        if (oe.getAssignFrom() != null){
            oeAssign = loader.getObjective(oe.getAssignFrom());
        }
        if (oeAssign != null){
            return getOwner(oeAssign.getContainerType(), oeAssign.getContainerId(), loader);
        }else{
            return getOwner(oe.getContainerType(), oe.getContainerId(), loader);
        }
    }

    public static Objective.LinkPart toLinkPart(ObjectiveEntity oe, EntityLoader loader) {
        Objective.LinkPart ob = BeanUtil.fill(oe, new Objective.LinkPart());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ob.setCreateTime(sdf.format(oe.getCreateTime()));
        ob.setProgress(Math.round(ob.getProgress()) * 1.0);
        if (oe.getLinkAbove() != null) {
            ob.setLinkTime(sdf.format(oe.getLinkTime()));
        }
        ob.setOwner(getOwner(oe.getContainerType(), oe.getContainerId(), loader));
        return ob;
    }

    public static Objective.PartAdd toPartAdd(ObjectiveEntity oe, EntityLoader loader) {
        Objective.PartAdd ob = BeanUtil.fill(oe, new Objective.PartAdd());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ob.setCreateTime(sdf.format(oe.getCreateTime()));
        ob.setProgress(Math.round(ob.getProgress()) * 1.0);
        ob.setOwner(getOwner(oe, loader));
        return ob;
    }

    public static List<Objective.PartAdd> toPartAdds(List<ObjectiveEntity> objs, EntityLoader loader) {
        List<Objective.PartAdd> ret = new ArrayList<>();
        for (ObjectiveEntity obj : objs) {
            Objective.PartAdd partAdd = toPartAdd(obj, loader);
            if (partAdd.getOwner() != null) {
                ret.add(partAdd);
            }
        }
        return ret;
    }

    public static Objective from(ObjectiveEntity oe, EntityLoader loader, KeyResultMapper.BeforeMapping kFilter) {
        Objective ob = BeanUtil.fill(oe, new Objective());
        ob.setProgress(Math.round(ob.getProgress()) * 1.0);
        ob.setOwner(getOwner(oe, loader));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ob.setCreateTime(sdf.format(oe.getCreateTime()));
        ob.setKeyResults(KeyResultMapper.fromList(oe.getKeyResults(), kFilter));
        if (oe.getLinkAbove() != null) {
            ob.setLinkTime(sdf.format(oe.getLinkTime()));
            ObjectiveEntity aboveOE = loader.getObjective(oe.getLinkAbove());
            ob.setLinkedAbove(toLinkPart(aboveOE, loader));
        }
        List<ObjectiveEntity> belows = oe.getLinkBelows();
        ob.setLinkedBelow(new ArrayList<>());
        for (ObjectiveEntity below : belows) {
            ob.getLinkedBelow().add(toLinkPart(below, loader));
        }
        return ob;
    }

    public static List<Objective> fromList(List<ObjectiveEntity> objs, EntityLoader loader, BeforeMapping befroeMapping,
                                           KeyResultMapper.BeforeMapping kFilter) {
        List<Objective> ret = new ArrayList<>();
        for (ObjectiveEntity obj : objs) {
            if (null == befroeMapping || befroeMapping.filter(obj)) {
                ret.add(from(obj, loader, kFilter));
            }
        }
        return ret;
    }
}
