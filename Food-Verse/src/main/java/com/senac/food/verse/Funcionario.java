package com.senac.food.verse;

public class Funcionario extends Usuario implements funcionarioInterface {
    String name;
    String role;
    String acessCode;

    public Funcionario(int userId, String userName, String email, String password, Boolean isLogin, String registrationDate) {
        super(userId, userName, email, password, isLogin, registrationDate);
    }

    @Override
    public void permissaoFunc() {
        System.out.println("Permissões definidas para o funcionário!");
    }

    @Override
    public void login() {
        System.out.println("Funcionário logado!");
    };
}
