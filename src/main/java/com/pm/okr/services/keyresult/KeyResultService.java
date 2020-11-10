package com.pm.okr.services.keyresult;

import com.pm.okr.controller.vo.*;
import com.pm.okr.model.entity.KeyResultEntity;

import java.util.List;


public interface KeyResultService {

    ResultKRChanged addKeyResult(Integer oId, String content, String resultType, Double startValue, Double targetValue, Integer decimals);

    boolean updateContent(Integer id, String content);

    KeyResultEntity updateTypeValue(Integer id, String resultType, Double startValue, Double targetValue, Integer decimals);

    List<ObjProgress> updateWeight(Integer id, Double weight);

    List<ObjProgress> updateProgress(Integer id, Double progress);

    List<KeyResultEntity> findAll(List<Integer> ids);

    List<ObjProgress> removeKeyResult(Integer valueOf);

    List<ObjProgress> assignResult(Integer kId, Integer container, Integer cId);
}
