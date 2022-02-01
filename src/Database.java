import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class Database {
    private Connection conn;
    private PreparedStatement login_, view_book_by, add_book, edit_book;

    public Database() throws SQLException {

        conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "raghav", "sql");
        System.out.println("Connected to MySql database!");

        login_ = conn.prepareStatement("SELECT id, name, type FROM Users WHERE username = ? and password = ?");
//        view_book_by = conn.prepareStatement("SELECT id, name, author_name FROM Books WHERE ? = ?");
//        add_book = conn.prepareStatement("INSERT INTO Books (name, author_name, id, total) VALUES(?, ?, ?, ?)");
//        edit_book = conn.prepareStatement("UPDATE Books SET name=?, author_name=?, quantity=? where id=?");

    }

    protected void finalize() throws SQLException {
        // destructor
        conn.close();
    }

    public Account login(String username, String password) throws SQLException, LoginFailed {
        login_.clearParameters();
        login_.setString(1, username);
        login_.setString(2, password);
        ResultSet res = login_.executeQuery();
        if (res.next()) {
            String name = res.getString("name");
            int type = res.getInt("type");
            int id = res.getInt("id");
            return new Account(id, username, name, type);
        } else
            throw new LoginFailed();
    }

    public int editRow(String table, String condition, Map<String, String> vals) throws SQLException {
        String query = String.format("UPDATE %s SET ", table);

        boolean first = true;
        for (Map.Entry<String, String> elem : vals.entrySet()) {
            if (!first)
                query += ", ";
            first = false;
            query += "`"+elem.getKey() + "`='" + elem.getValue()+"'";
        }
        query += String.format(" WHERE %s;", condition);
//        System.out.println(query);
        Statement st = conn.createStatement();
        return st.executeUpdate(query);
    }

    public int addRow(String table, Map<String, String> vals) throws SQLException {
        String query = String.format("INSERT INTO %s (", table);
        boolean first = true;
        for (Map.Entry<String, String> elem : vals.entrySet()) {
            if (!first)
                query += ", ";
            first = false;
            query += "`"+elem.getKey()+"`";
        }
        first = true;
        query += ") VALUES(";
        for (Map.Entry<String, String> elem : vals.entrySet()) {
            if (!first)
                query += ", ";
            first = false;
            query += "'"+elem.getValue()+"'";
        }
        query += ");";
//        System.out.println(query);

        Statement st = conn.createStatement();
        return st.executeUpdate(query);
    }

    public void printTable(String table) {
        DBTablePrinter.printTable(conn, table);
    }

    public void printTableWhere(String table, String condition) throws SQLException {
        String query = String.format("SELECT * FROM %s WHERE %s;", table, condition);
        Statement st = conn.createStatement();
        DBTablePrinter.printResultSet(st.executeQuery(query));
    }

    public HashMap<String, String> getVals(String table, String condition, String[] columns) throws SQLException {
        String query = String.format("SELECT %s FROM %s WHERE %s;", String.join(", ", columns), table, condition);
        Statement st = conn.createStatement();
//        System.out.println(query);
        ResultSet rs = st.executeQuery(query);
        if (rs.next()) {
            HashMap<String, String> res = new HashMap<String, String>();
            for (String c : columns) {
                res.put(c, rs.getString(c));
            }
            return res;
        } else
            return new HashMap<>();
    }


    public void deleteRow(String table, String key, String id) throws SQLException {
        String query = String.format("DELETE FROM %s WHERE %s=%s;", table, key, id);
        Statement st = conn.createStatement();
        st.executeUpdate(query);
    }
}
