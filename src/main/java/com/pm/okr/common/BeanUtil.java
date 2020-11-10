package com.pm.okr.common;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class BeanUtil {

    static String methodSubFix(String name){
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    static Object getValue(Object obj, Field field) {
        try {
            Method getMethod = obj.getClass().getMethod("get" + methodSubFix(field.getName()));
            return getMethod.invoke(obj);
        } catch (Exception e) {
            return null;
        }
    }

    static void setValue(Object obj, Field field, Object val) {
        try {
            Method setMethod = obj.getClass().getMethod("set" + methodSubFix(field.getName()), field.getType());
            setMethod.invoke(obj, val);
        } catch (Exception e) {
            //将整数 ID 映射为字符串 ID
            if (field.getName().equals("id")) {
                try {
                    Method setMethod = obj.getClass().getMethod("set" + methodSubFix(field.getName()), String.class);
                    if (obj != null) {
                        setMethod.invoke(obj, val.toString());
                    } else {
                        setMethod.invoke(obj, val);
                    }
                } catch (Exception e1) {
                }
            }
        }
    }

    public interface PostBeanProcessor<T1, T2>{
        void process(T1 src, T2 dest);
    }

    public static <T> T fill(Object from, Object to) {
        Object bean = to;
        if (from == null){
            return (T)to;
        }
        Field[] fileds = from.getClass().getDeclaredFields();
        for (int i = 0; i < fileds.length; ++i) {
            Field field = fileds[i];
            Object val = getValue(from, field);
            if (null != val) {
                setValue(to, field, val);
            }
        }
        return (T) bean;
    }

    public static <T> List<T> fillList(List from, List to) {
        for (int i = 0; i < from.size() && i < to.size(); ++i) {
            fill(from.get(i), to.get(i));
        }
        return (List<T>) to;
    }

    public static <T> List<T> fillList(List from, Class<?> cls) throws IllegalAccessException, InstantiationException {
        List<T> ret = new ArrayList<>();
        for (int i = 0; i < from.size(); ++i) {
            T t = (T) cls.newInstance();
            ret.add(t);
            fill(from.get(i), t);
        }
        return (List<T>) ret;
    }

    public static <T> List<T> fillList(List from, Class<?> cls, PostBeanProcessor processor) throws IllegalAccessException, InstantiationException {
        List<T> ret = new ArrayList<>();
        for (int i = 0; i < from.size(); ++i) {
            T t = (T) cls.newInstance();
            ret.add(t);
            fill(from.get(i), t);
            if (null != processor){
                processor.process(from.get(i), t);
            }
        }
        return (List<T>) ret;
    }

    public static <T> T clone(Object stub) {
        try {
            Object target = Object.class.newInstance();
            return fill(stub, target);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
