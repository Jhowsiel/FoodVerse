package com.senac.food.verse;

public class Cliente extends Usuario implements ClienteInterface {
private   String name;
private  String phone;
private  String cpf;

    public Cliente(String name, String phone, String cpf, int userId, String userName, String email, String password, Boolean isLogin, String registrationDate) {
        super(userName, email, password, isLogin, registrationDate);
        this.name = name;
        this.phone = phone;
        this.cpf = cpf;
    } 

    @Override
    public void cadastro() {
        System.out.println("Cliente cadastrado com sucesso!");
    }

    @Override
    public void atualizarPerfil() {
        System.out.println("Perfil atualizado!");
    }  
   
    @Override
    public void login() {
        System.out.println("Perfil atualizado!");
    }  
}
