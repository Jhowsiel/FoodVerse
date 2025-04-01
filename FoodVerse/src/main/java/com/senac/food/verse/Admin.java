package com.senac.food.verse;

public class Admin extends Usuario implements AdminInterface {
    String name;
    String AcessCode;

    public Admin(String name, String AcessCode, int userId, String userName, String email, String password, Boolean isLogin, String registrationDate) {
        super(userName, email, password, isLogin, registrationDate);
        this.name = name;
        this.AcessCode = AcessCode;
    }
    
    public String getAcessCode() {
        return AcessCode;
    }
    
    @Override
    public void login() {
        System.out.println("Logado Com Sucesso");
    }

    @Override
    public void cadastrarFuncionario() {
        System.out.println("Funcionário cadastrado pelo admin!");
    }

    @Override
    public void atualizarFuncionario() {
        System.out.println("Funcionário atualizado!");
    }

    @Override
    public void atualizarCliente() {
        System.out.println("Cliente atualizado!");  
    }

    @Override
    public void buscarFuncionario(int funcionarioId) {
        System.out.println("Aqui está o seu funcionario");
    }

    @Override
    public void listarFuncionario() {
        System.out.println("aqui está todos os funcionarios");
    }

    @Override
    public void excluirFuncionario() {
        System.out.println("Funcionario excluido");
    }
}
