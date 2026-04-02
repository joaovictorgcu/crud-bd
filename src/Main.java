import dao.DepartamentoDAO;
import dao.FuncionarioDAO;
import modelo.Departamento;
import modelo.Funcionario;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        DepartamentoDAO deptDAO = new DepartamentoDAO();
        FuncionarioDAO funcDAO = new FuncionarioDAO();
        Scanner input = new Scanner(System.in);

        System.out.println("Sistema iniciado!");

        while (true) {
            System.out.println("\n=== MENU ===");
            System.out.println("1. Inserir departamento");
            System.out.println("2. Inserir funcionario");
            System.out.println("3. Atualizar funcionario (salario)");
            System.out.println("4. Atualizar departamento (nome)");
            System.out.println("5. Deletar funcionario");
            System.out.println("6. Deletar departamento");
            System.out.println("7. Listar funcionarios");
            System.out.println("8. Listar departamentos");
            System.out.println("9. Sair");
            System.out.print("Escolha: ");

            int opcao;
            try {
                opcao = Integer.parseInt(input.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Digite um numero valido.");
                continue;
            }

            try {
                switch (opcao) {
                    case 1 -> {
                        System.out.print("Nome do departamento: ");
                        String nome = input.nextLine();
                        System.out.print("Sigla (3 chars): ");
                        String sigla = input.nextLine();
                        Departamento dept = new Departamento(nome, sigla);
                        int rows = deptDAO.inserir(dept);
                        System.out.println(rows + " departamento(s) inserido(s)");
                    }
                    case 2 -> {
                        System.out.print("Nome do funcionario: ");
                        String nome = input.nextLine();
                        System.out.print("Email: ");
                        String email = input.nextLine();
                        System.out.print("Salario: ");
                        double salario = Double.parseDouble(input.nextLine());
                        System.out.print("ID do departamento (ou em branco para null): ");
                        String deptId = input.nextLine();
                        Integer idDept = deptId.isBlank() ? null : Integer.parseInt(deptId);
                        Funcionario func = new Funcionario(nome, email, salario, idDept);
                        int rows = funcDAO.inserir(func);
                        System.out.println(rows + " funcionario(s) inserido(s)");
                    }
                    case 3 -> {
                        System.out.print("ID do funcionario: ");
                        int id = Integer.parseInt(input.nextLine());
                        System.out.print("Novo salario: ");
                        double novoSalario = Double.parseDouble(input.nextLine());
                        int rows = funcDAO.atualizarSalario(id, novoSalario);
                        System.out.println(rows + " funcionario(s) atualizado(s)");
                    }
                    case 4 -> {
                        System.out.print("ID do departamento: ");
                        int id = Integer.parseInt(input.nextLine());
                        System.out.print("Novo nome: ");
                        String nome = input.nextLine();
                        int rows = deptDAO.atualizarNome(id, nome);
                        System.out.println(rows + " departamento(s) atualizado(s)");
                    }
                    case 5 -> {
                        System.out.print("ID do funcionario para deletar: ");
                        int id = Integer.parseInt(input.nextLine());
                        int rows = funcDAO.deletar(id);
                        System.out.println(rows + " funcionario(s) deletado(s)");
                    }
                    case 6 -> {
                        System.out.print("ID do departamento para deletar: ");
                        int id = Integer.parseInt(input.nextLine());
                        int rows = deptDAO.deletar(id);
                        System.out.println(rows + " departamento(s) deletado(s) (funcionarios com id_departamento SET NULL)");
                    }
                    case 7 -> {
                        List<Funcionario> funcionarios = funcDAO.listar();
                        System.out.println("ID | Nome | Email | Salario | Departamento");
                        for (Funcionario f : funcionarios) {
                            System.out.println(f);
                        }
                    }
                    case 8 -> {
                        List<Departamento> departamentos = deptDAO.listar();
                        System.out.println("ID | Nome | Sigla | Criacao");
                        for (Departamento d : departamentos) {
                            System.out.println(d);
                        }
                    }
                    case 9 -> {
                        System.out.println("Saindo...");
                        input.close();
                        return;
                    }
                    default -> System.out.println("Opcao invalida");
                }
            } catch (SQLException e) {
                System.err.println("Erro de banco: " + e.getMessage());
            }
        }
    }
}
