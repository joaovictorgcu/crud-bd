import java.sql.*;
import java.util.Scanner;

public class Main {
    private static final String URL = "jdbc:postgresql://localhost:5432/entrega_bd";
    private static final String USER = "seu_usuario";
    private static final String PASSWORD = "sua_senha";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD); Scanner input = new Scanner(System.in)) {
            System.out.println("Conectado ao banco de dados com sucesso!");

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

                int opcao = Integer.parseInt(input.nextLine());

                switch (opcao) {
                    case 1 -> inserirDepartamento(conn, input);
                    case 2 -> inserirFuncionario(conn, input);
                    case 3 -> atualizarSalarioFuncionario(conn, input);
                    case 4 -> atualizarNomeDepartamento(conn, input);
                    case 5 -> deletarFuncionario(conn, input);
                    case 6 -> deletarDepartamento(conn, input);
                    case 7 -> listarFuncionarios(conn);
                    case 8 -> listarDepartamentos(conn);
                    case 9 -> { System.out.println("Saindo..."); return; }
                    default -> System.out.println("Opcao invalida");
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro de banco: " + e.getMessage());
        }
    }

    private static void inserirDepartamento(Connection conn, Scanner input) throws SQLException {
        System.out.print("Nome do departamento: ");
        String nome = input.nextLine();
        System.out.print("Sigla (3 chars): ");
        String sigla = input.nextLine();

        String sql = "INSERT INTO departamento (nome, sigla) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nome);
            stmt.setString(2, sigla);
            int rows = stmt.executeUpdate();
            System.out.println(rows + " departamento(s) inserido(s)");
        }
    }

    private static void inserirFuncionario(Connection conn, Scanner input) throws SQLException {
        System.out.print("Nome do funcionario: ");
        String nome = input.nextLine();
        System.out.print("Email: ");
        String email = input.nextLine();
        System.out.print("Salario: ");
        double salario = Double.parseDouble(input.nextLine());
        System.out.print("ID do departamento (ou em branco para null): ");
        String dept = input.nextLine();

        String sql = "INSERT INTO funcionario (nome, email, salario, id_departamento) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nome);
            stmt.setString(2, email);
            stmt.setDouble(3, salario);
            if (dept.isBlank()) {
                stmt.setNull(4, Types.INTEGER);
            } else {
                stmt.setInt(4, Integer.parseInt(dept));
            }
            int rows = stmt.executeUpdate();
            System.out.println(rows + " funcionario(s) inserido(s)");
        }
    }

    private static void atualizarSalarioFuncionario(Connection conn, Scanner input) throws SQLException {
        System.out.print("ID do funcionario: ");
        int id = Integer.parseInt(input.nextLine());
        System.out.print("Novo salario: ");
        double novoSalario = Double.parseDouble(input.nextLine());

        String sql = "UPDATE funcionario SET salario = ? WHERE id_funcionario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, novoSalario);
            stmt.setInt(2, id);
            int rows = stmt.executeUpdate();
            System.out.println(rows + " funcionario(s) atualizado(s)");
        }
    }

    private static void atualizarNomeDepartamento(Connection conn, Scanner input) throws SQLException {
        System.out.print("ID do departamento: ");
        int id = Integer.parseInt(input.nextLine());
        System.out.print("Novo nome: ");
        String nome = input.nextLine();

        String sql = "UPDATE departamento SET nome = ? WHERE id_departamento = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nome);
            stmt.setInt(2, id);
            int rows = stmt.executeUpdate();
            System.out.println(rows + " departamento(s) atualizado(s)");
        }
    }

    private static void deletarFuncionario(Connection conn, Scanner input) throws SQLException {
        System.out.print("ID do funcionario para deletar: ");
        int id = Integer.parseInt(input.nextLine());

        String sql = "DELETE FROM funcionario WHERE id_funcionario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            System.out.println(rows + " funcionario(s) deletado(s)");
        }
    }

    private static void deletarDepartamento(Connection conn, Scanner input) throws SQLException {
        System.out.print("ID do departamento para deletar: ");
        int id = Integer.parseInt(input.nextLine());

        String sql = "DELETE FROM departamento WHERE id_departamento = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            System.out.println(rows + " departamento(s) deletado(s) (funcionarios com id_departamento SET NULL)");
        }
    }

    private static void listarFuncionarios(Connection conn) throws SQLException {
        String sql = "SELECT id_funcionario, nome, email, salario, id_departamento FROM funcionario ORDER BY id_funcionario LIMIT 100";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("ID | Nome | Email | Salario | Departamento");
            while (rs.next()) {
                System.out.printf("%d | %s | %s | %.2f | %s%n",
                    rs.getInt("id_funcionario"),
                    rs.getString("nome"),
                    rs.getString("email"),
                    rs.getDouble("salario"),
                    rs.getObject("id_departamento", Integer.class));
            }
        }
    }

    private static void listarDepartamentos(Connection conn) throws SQLException {
        String sql = "SELECT id_departamento, nome, sigla, data_criacao FROM departamento ORDER BY id_departamento";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("ID | Nome | Sigla | Criacao");
            while (rs.next()) {
                System.out.printf("%d | %s | %s | %s%n",
                    rs.getInt("id_departamento"),
                    rs.getString("nome"),
                    rs.getString("sigla"),
                    rs.getDate("data_criacao"));
            }
        }
    }
}
