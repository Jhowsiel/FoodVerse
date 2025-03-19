
package com.senac.food.verse;

public class Usuario {
private  int userId;
private  String userName;
private String email;
private   String password;
private  Boolean isLogin;
 String registrationDate;

    public Usuario(int userId, String userName, String email, String password, Boolean isLogin, String registrationDate) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.isLogin = isLogin;
        this.registrationDate = registrationDate;
    }

    public boolean chechLogin() {
        return this.isLogin;
    }
    
    public void login() {
        System.out.println("Logado com Sucesso!");
    }
        
    public int getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Boolean getIsLogin() {
        return isLogin;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }
}
