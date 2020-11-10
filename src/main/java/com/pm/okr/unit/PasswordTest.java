package com.pm.okr.unit;

import org.jasypt.util.text.BasicTextEncryptor;

/**
 * @ClassName PasswordTest.java
 * @Author zlz
 * @CreateTime 2020年09月06日 18:43:00
 * @Details
 */
public class PasswordTest {
    public static void main(String[] args) {
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        //加密所需的salt(盐)
        textEncryptor.setPassword("SHSR201903");
        //要加密的数据（数据库的用户名或密码）
        String username = textEncryptor.encrypt("okr");
        String password = textEncryptor.encrypt("123456");
        System.out.println("username:"+username);
        System.out.println("password:"+password);
    }
}
