package com.pm.okr.common;

import com.pm.okr.controller.vo.*;
import com.pm.okr.model.entity.KeyResultEntity;
import com.pm.okr.model.entity.ObjectiveEntity;
import com.pm.okr.services.org.OrgService;
import com.pm.okr.services.user.UserService;
import org.mapstruct.BeforeMapping;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class KeyResultMapper {

    public interface BeforeMapping{
        /**
         * 通过 filter 检查 返回 true
         * 未通过 返回 false
         * */
        boolean filter(KeyResultEntity kr);
    }

    public static KeyResult.PartShort toPartShort(KeyResultEntity kre){
        KeyResult.PartShort kr = BeanUtil.fill(kre, new KeyResult.PartShort());
        return kr;
    }

    public static KeyResult from(KeyResultEntity kre){
        KeyResult kr = BeanUtil.fill(kre, new KeyResult());
        kr.setObjId(kre.getObjId().toString());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        kr.setCreateTime(sdf.format(kre.getCreateTime()));
        kr.setUpdateTime(sdf.format(kre.getUpdateTime()));
        return kr;
    }

    public static List<KeyResult> fromList(List<KeyResultEntity> kres, BeforeMapping beforeMapping){
        List<KeyResult> krs = new ArrayList<>();
        for (KeyResultEntity kre : kres){
            if (null == beforeMapping || beforeMapping.filter(kre)) {
                krs.add(from(kre));
            }
        }
        return krs;
    }
}
