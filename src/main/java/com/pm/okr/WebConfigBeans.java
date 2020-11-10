package com.pm.okr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.text.SimpleDateFormat;

class CustomDateConverter implements Converter<String, java.util.Date> {

    @Override
    public java.util.Date convert(String source) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            java.util.Date date = simpleDateFormat.parse(source);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

}

class CustomSqlDateConverter implements Converter<String, java.sql.Date> {

    @Override
    public java.sql.Date convert(String source) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {
            java.sql.Date date = new java.sql.Date(simpleDateFormat.parse(source).getTime());
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

}

@Configuration
public class WebConfigBeans {

    @Autowired
    private RequestMappingHandlerAdapter handlerAdapter;

    /**
     * 增加字符串转日期的功能
     */
    @PostConstruct
    public void initEditableAvlidation() {

        ConfigurableWebBindingInitializer initializer = (ConfigurableWebBindingInitializer) handlerAdapter.getWebBindingInitializer();
        if (initializer.getConversionService() != null) {
            GenericConversionService genericConversionService = (GenericConversionService) initializer.getConversionService();
            genericConversionService.addConverter(new CustomDateConverter());
            genericConversionService.addConverter(new CustomSqlDateConverter());
        }

    }
}