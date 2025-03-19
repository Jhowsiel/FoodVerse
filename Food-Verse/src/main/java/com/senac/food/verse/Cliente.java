package com.senac.food.verse;

public class Cliente extends Usuario implements ClienteInterface {
private   String name;
private  String phone;
private  String cpf;

    public Cliente(int userId, String userName, String email, String password, Boolean isLogin, String registrationDate) {
        super(userId, userName, email, password, isLogin, registrationDate);
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
