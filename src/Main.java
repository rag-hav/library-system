import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Integer.max;
import static java.lang.Integer.parseInt;
import static java.time.temporal.ChronoUnit.DAYS;

public class Main {

    public static void main(String[] args) {
        System.out.println("Welcome to the Library!\n");
        try {
            Database db = new Database();
            Account account;
            while (true) {
                try {
                    String username = Utils.prompt("Username");
                    String password = Utils.prompt("Password");
                    account = db.login(username, password);
                    break;
                } catch (LoginFailed e) {
                    System.out.println("Invalid username or password!");
                }
            }
            System.out.println("Login Successful! Welcome " + account.name);
            if (account.type == 0) {
                // User is student
                String[] choices = new String[]{"Check Status of Issued Books", "Search Book", "Borrow Book", "Return Book"};
                int choice;
                while ((choice = Utils.menu(choices, "Things to do")) != 0) {
                    if (choice == 1) {
                        db.printTableWhere("pending_history", String.format("`Borrower's ID`=%d", account.id));
                    } else if (choice == 2) {
                        // Search Book
                        String[] queries = new String[]{"name", "author_name"};
                        int qt = Utils.menu(queries, "Search Books Using");
                        if (qt != 0) {
                            String val = Utils.prompt("Search query");
                            db.printTableWhere("Books", String.format("%s='%s'", queries[qt - 1], val));
                        }
                    } else if (choice == 3) {
                        // Borrow Book

                        // count number of books currently issued
                        int cnt = Integer.parseInt(db.getVals("History", "status=1 and student_id=" + account.id, new String[]{"count(*)"}).get("count(*)"));
                        if (cnt >= 4) {
                            System.out.println("You can not borrow any more Books!");
                        } else {
                            String id = Utils.prompt("ID of Book");
                            // find book
                            HashMap<String, String> res = db.getVals("Books", "id=" + id, new String[]{"available", "name"});
                            if (res.isEmpty()) {
                                System.out.println("Error 404: No Such Book!");
                            } else {
                                Integer available = parseInt(res.get("available")) - 1;
                                if (available >= 0) {
                                    db.addRow("History", Map.of("student_id", account.id.toString(), "book_id", id));
                                    db.editRow("Books", "id=" + id, Map.of("available", available.toString()));
                                    System.out.println("Success! You have issued the book " + res.get("name"));
                                } else {
                                    System.out.println(res.get("name") + "is not available right now!.");
                                }
                            }
                        }
                    } else if (choice == 4) {
                        // Return Book

                        String id = Utils.prompt("ID of Book");
                        // condition to search History table
                        String condition = "book_id=" + id + " and student_id=" + account.id + " and status=1";
                        // check if the student has issued this book, get issue date
                        HashMap<String, String> check = db.getVals("History", condition, new String[]{"issued_on"});
                        if (check.isEmpty()) {
                            System.out.println("Error 404: No Such Book!");
                        } else {
                            // get name of the book, and number of copies in library
                            HashMap<String, String> res = db.getVals("Books", "id=" + id, new String[]{"available", "name"});
                            Integer available = parseInt(res.get("available")) + 1;
                            // increment the number of copies of book in library
                            db.editRow("Books", "id=" + id, Map.of("available", available.toString()));
                            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                            LocalDateTime issue_date = LocalDateTime.parse(check.get("issued_on"), dtf);
                            LocalDateTime return_date = LocalDateTime.now();
                            int diff = (int) (DAYS.between(issue_date, return_date) - 15);
                            Integer fine = max(0, diff * 5);
                            if (fine > 0) {
                                System.out.println("You have been charged a fine of Rupees " + fine);
                            }
                            // Mark the book as returned in History table
                            db.editRow("History", condition, Map.of("status", "0", "fine", fine.toString(), "returned_on", dtf.format(return_date)));

                            System.out.println("Success! You have Returned the book " + res.get("name"));
                        }
                    }
                }
            } else {
                String[] choices1 = new String[]{"Books", "Students", "Borrow Ledger"};
                int choice1;
                while ((choice1 = Utils.menu(choices1, "Things to do")) != 0) {
                    if (choice1 == 1) {
                        String[] choices2 = new String[]{"View Books", "Add a Book", "Delete a Book", "Edit a book"};
                        int choice2;
                        while ((choice2 = Utils.menu(choices2, "Things to do")) != 0) {
                            if (choice2 == 1) {
                                db.printTable("Books");
                            } else if (choice2 == 2) {
                                // add book
                                String name = Utils.prompt("Name of the Book");
                                String author_name = Utils.prompt("Author's Name");
                                String total = Utils.prompt("Quantity of Book");
                                db.addRow("Books", Map.of("name", name, "author_name", author_name, "total", total, "available", total));
                            } else if (choice2 == 3) {
                                // delete book
                                String id = Utils.prompt("ID of Book to delete");
                                db.deleteRow("Books", "id", id);
                            } else {
                                // edit book
                                String id = Utils.prompt("ID of Book to edit");
                                String name = Utils.prompt("Name of the Book");
                                String author_name = Utils.prompt("Author's Name");
                                String change = Utils.prompt("Change in Quantity of Book");
                                db.editRow("Books", "id=" + id, Map.of("name", name, "author_name", author_name, "total", "total + " + change, "available", "available + " + change));
                            }
                        }
                    } else if (choice1 == 2) {
                        String[] choices2 = new String[]{"View Students", "View Student's Borrowed", "Add Students", "Delete Students"};
                        int choice2;
                        while ((choice2 = Utils.menu(choices2, "Things to do")) != 0) {
                            if (choice2 == 1) {
                                db.printTable("all_students");
                            } else if (choice2 == 2) {
                                String id = Utils.prompt("id of Student Account");
                                db.printTableWhere("pending_history", String.format("`Borrower's ID`=%s", id));
                            } else if (choice2 == 3) {
                                String name = Utils.prompt("Name of Student");
                                String username = Utils.prompt("username");
                                String password = Utils.prompt("password for Student");
                                db.addRow("Users", Map.of("username", username, "name", name, "password", password, "type", "0"));
                            } else {
                                String id = Utils.prompt("id of Student Account to delete");
                                db.deleteRow("Users", "id", id);
                            }

                        }
                    } else if (choice1 == 3) {
                        String[] choices2 = new String[]{"Complete", "Active Only"};
                        int choice2;
                        while ((choice2 = Utils.menu(choices2, "Things to do")) != 0) {
                            if (choice2 == 1) {
                                db.printTable("full_history");
                            } else {
                                db.printTable("pending_history");
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Connection with database lost");
            System.exit(1);
        }
    }
}
