import java.util.Scanner;
import java.sql.*;
import java.io.*;
import java.util.*;
import java.lang.*;
import java.util.List;
import java.text.*;

public class CSCI3170 {
    public static Scanner sc = new Scanner(System.in);
    public int identity_choice = 0;
    public int choice = 0;
    private String dbAddress = "jdbc:mysql://projgw.cse.cuhk.edu.hk:2633/db30";
    private String dbUserName = "Group30";
    private String dbPassword = "CSCI3170";
    private Connection con = null;

    public static void main(String[] args) {
        CSCI3170 cs = new CSCI3170();
        cs.connect_db();
        cs.welcome_page();
        sc.close();
    }

    void welcome_page() {
        System.out.println("Welcome to Library INquiry System!");
        System.out.println("  ");
        System.out.println("---Main Menu---");
        System.out.println("What kinds of operation would you like to perform?");
        System.out.println("1. Admin");
        System.out.println("2. Library User");
        System.out.println("3. Librarian");
        System.out.println("4. Exit");
        System.out.print("Enter your choice: ");
        identity_choice = sc.nextInt();
        main_menu_action(identity_choice);
    }

    void admin_menu() {
        System.out.println("---Admin Menu---");
        System.out.println("What kinds of operation would you like to perform?");
        System.out.println("1. Create all tables");
        System.out.println("2. Delete all tables");
        System.out.println("3. Load from datafile");
        System.out.println("4. Show number of recrods in each table");
        System.out.println("5. Return to main menu");
        System.out.print("Enter your choice: ");
        choice = sc.nextInt();
        admin_action(choice);
    }

    void user_menu() {
        System.out.println("---Library User Menu---");
        System.out.println("What kinds of operation would you like to perform?");
        System.out.println("1. Search for books");
        System.out.println("2. Show loan record of a user");
        System.out.println("3. Return to main menu");
        System.out.print("Enter your choice: ");
        choice = sc.nextInt();
        user_action(choice);
    }

    void librarian_menu() {
        System.out.println("---Librarian Menu---");
        System.out.println("What kinds of operation would you like to perform?");
        System.out.println("1. Book Borrowing");
        System.out.println("2. Book Returning");
        System.out.println("3. List all un-returned book copies which are checked-out within a period");
        System.out.println("4. Return to main menu");
        System.out.print("Enter your choice: ");
        choice = sc.nextInt();
        librarian_action(choice);
    }

    void main_menu_action(int identity_choice) {
        if (identity_choice == 1) {
            admin_menu();
        }
        if (identity_choice == 2) {
            user_menu();
        }
        if (identity_choice == 3) {
            librarian_menu();
        }
        if (identity_choice == 4) {
            System.exit(0);
        }
        // welcome_page();
    }

    void admin_action(int choice) {
        if (choice == 1) {
            create_table();
        } else if (choice == 2) {
            drop_table();
        } else if (choice == 3) {
            load_file_data();
        } else if (choice == 4) {
            count_records();
        } else if (choice == 5) {
            return_menu();
            return;
        }
        admin_menu();
    }

    void user_action(int choice) {
        if (choice == 1) {
            Choose_Search_Action();
        } else if (choice == 2) {
            Show_Loan_Record();
        } else if (choice == 3) {
            return_menu();
            return;
        }
    }

    void Show_Loan_Record() {
        System.out.print("Enter the User ID: ");
        sc.nextLine();
        String ID = sc.nextLine();
        System.out.println("Loan Record: ");
        String Record = "SELECT c.Call_number, c.Copy_number, b.title, b.author, c.Check_out_date, c.Return_date FROM Checked_Out_Records c, Books b WHERE c.Call_number = b.call_number AND User_ID = ? ORDER BY Check_Out_date DESC;";
        try {
            PreparedStatement sql_Record = con.prepareStatement(Record);
            sql_Record.setString(1, ID);
            con.setAutoCommit(false);
            try {
                sql_Record.execute();
                ResultSet resultSet = sql_Record.executeQuery();
                if (!resultSet.isBeforeFirst())
                    System.out.println("No records found.");
                else {
                    while (resultSet.next()) {
                        System.out.println("|CallNum|CopyNum|Title|Author|Check-out|Returned?|");
                        System.out.print("|" + resultSet.getString(1) + "|");
                        System.out.print(resultSet.getString(2) + "|");
                        System.out.print(resultSet.getString(3) + "|");
                        System.out.print(resultSet.getString(4) + "|");
                        System.out.print(resultSet.getString(5) + "|");
                        if (resultSet.getString(6) != null) {
                            System.out.print("Yes|");
                        } else {
                            System.out.print("No|");
                        }
                        System.out.println();
                    }
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                System.out.println(ex.getSQLState());
                System.out.println(ex.getMessage());
                System.out.println("[ERROR] Failed.");
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Failed.");
        }
        System.out.println("End of Query ");
        return_menu();
        return;
    }

    void Choose_Search_Action() {
        System.out.println("Choose the Search criterion: ");
        System.out.println("1. call number");
        System.out.println("2. title");
        System.out.println("3. author");
        System.out.print("Choose the Search criterion: ");
        choice = sc.nextInt();
        Search_Action(choice);
    }

    void Search_Action(int choice) {
        if (choice == 1) {
            Search_By_CN();
        } else if (choice == 2) {
            Search_By_title();
        } else if (choice == 3) {
            Search_By_Author();
        }
    }

    void Search_By_CN() {
        System.out.print("Type in the search Keyword: ");
        sc.nextLine();
        String keyword = sc.nextLine();
        String Search_CN = "SELECT c.call_number, c.title, b.bc_name, c.author, c.rating, c.no_of_copies FROM Books c, Book_Categories b where c.call_number = ? AND c.bcid = b.bcid;";
        // String Search_CN = "SELECT * FROM Books WHERE call_number = ?;";
        // String Search_CN_BC = "SELECT * FROM Books_Categories WHERE bcid = ?;";
        try {
            PreparedStatement sql_Search_CN = con.prepareStatement(Search_CN);
            sql_Search_CN.setString(1, keyword);
            // PreparedStatement sql_Search_CN_BC = con.prepareStatement(Search_CN_BC);
            // sql_Search_CN_BC.setString(1, keyword);
            con.setAutoCommit(false);
            try {
                sql_Search_CN.execute();
                ResultSet resultSet = sql_Search_CN.executeQuery();
                // sql_Search_CN_BC.setString(1, resultSet.getString(8));
                // sql_Search_CN_BC.execute();
                // ResultSet resultSet2 = sql_Search_CN_BC.executeQuery();
                if (!resultSet.isBeforeFirst())
                    System.out.println("No records found.");
                else {
                    while (resultSet.next()) {
                        System.out.println("|Call Num|Title|Book Category|Author|Rating|Available No. of Copy|");
                        System.out.print("|" + resultSet.getString(1) + "|");
                        System.out.print(resultSet.getString(2) + "|");
                        System.out.print(resultSet.getString(3) + "|");
                        System.out.print(resultSet.getString(4) + "|");
                        System.out.print(resultSet.getString(5) + "|");
                        System.out.print(resultSet.getString(6) + "|");
                        System.out.println();
                    }
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                System.out.println(ex.getSQLState());
                System.out.println(ex.getMessage());
                System.out.println("[ERROR] Failed.");
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Failed.");
        }
        System.out.println("End of Query ");
        user_menu();
        return;
    }

    void Search_By_title() {
        System.out.print("Type in the search Keyword: ");
        sc.nextLine();
        String keyword = sc.nextLine();
        String Search_Title = "SELECT c.call_number, c.title, b.bc_name, c.author, c.rating, c.no_of_copies FROM Books c, Book_Categories b WHERE title LIKE ? AND c.bcid = b.bcid ORDER BY call_number ASC;";

        try {
            PreparedStatement sql_Search_Title = con.prepareStatement(Search_Title);
            sql_Search_Title.setString(1, "%" + keyword + "%");
            con.setAutoCommit(false);
            try {
                sql_Search_Title.execute();
                ResultSet resultSet = sql_Search_Title.executeQuery();
                if (!resultSet.isBeforeFirst())
                    System.out.println("No records found.");
                else {
                    while (resultSet.next()) {
                        System.out.println("|Call Num|Title|Book Category|Author|Rating|Available No. of Copy|");
                        System.out.print("|" + resultSet.getString(1) + "|");
                        System.out.print(resultSet.getString(2) + "|");
                        System.out.print(resultSet.getString(3) + "|");
                        System.out.print(resultSet.getString(4) + "|");
                        System.out.print(resultSet.getString(5) + "|");
                        System.out.print(resultSet.getString(6) + "|");
                        System.out.println();
                    }
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                System.out.println(ex.getSQLState());
                System.out.println(ex.getMessage());
                System.out.println("[ERROR] Failed.");
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Failed.");
        }
        System.out.println("End of Query ");
        user_menu();
        return;
    }

    void Search_By_Author() {
        System.out.print("Type in the search Keyword: ");
        sc.nextLine();
        String keyword = sc.nextLine();
        String Search_Au = "SELECT c.call_number, c.title, b.bc_name, c.author, c.rating, c.no_of_copies FROM Books c, Book_Categories b WHERE author LIKE ? AND c.bcid = b.bcid ORDER BY call_number ASC;";

        try {
            PreparedStatement sql_Search_Au = con.prepareStatement(Search_Au);
            sql_Search_Au.setString(1, "%" + keyword + "%");
            con.setAutoCommit(false);
            try {
                sql_Search_Au.execute();
                ResultSet resultSet = sql_Search_Au.executeQuery();
                if (!resultSet.isBeforeFirst())
                    System.out.println("No records found.");
                else {
                    while (resultSet.next()) {
                        System.out.println("|Call Num|Title|Book Category|Author|Rating|Available No. of Copy|");
                        System.out.print("|" + resultSet.getString(1) + "|");
                        System.out.print(resultSet.getString(2) + "|");
                        System.out.print(resultSet.getString(3) + "|");
                        System.out.print(resultSet.getString(4) + "|");
                        System.out.print(resultSet.getString(5) + "|");
                        System.out.print(resultSet.getString(6) + "|");
                        System.out.println();
                    }
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                System.out.println(ex.getSQLState());
                System.out.println(ex.getMessage());
                System.out.println("[ERROR] Failed.");
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Failed.");
        }
        System.out.println("End of Query ");
        user_menu();
        return;
    }

    void librarian_action(int choice) {
        if (choice == 1) {
            librarian_book_borrowing();
        } else if (choice == 2) {
            librarian_book_return();

        } else if (choice == 3) {
            librarian_book_history();
        } else if (choice == 4) {
            return_menu();
            return;
        }
        librarian_menu();
    }

    void librarian_book_history() {
        System.out.print("Type in the starting date [dd/mm/yyyy]: ");
        sc.nextLine();
        String starting_date = sc.nextLine();
        System.out.print("Type in the ending date [dd/mm/yyyy]: ");
        String ending_date = sc.nextLine();
        String query = "SELECT * FROM  Checked_Out_Records WHERE Check_out_date >= ? AND Check_out_date <= ? AND (Return_date is NULL or Return_date > ?);";

        DateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");

        int count = 0;
        try {
            PreparedStatement checkout_query = con.prepareStatement(query);

            java.util.Date starting_date_format = inputFormat.parse(starting_date);
            String start_date = outputFormat.format(starting_date_format);

            java.util.Date end_date_format = inputFormat.parse(ending_date);
            String end_date = outputFormat.format(end_date_format);

            checkout_query.setString(1, start_date);
            checkout_query.setString(2, end_date);
            checkout_query.setString(3, end_date);

            ResultSet rs = checkout_query.executeQuery();

            while (rs.next()) {
                count += 1;
                if (count == 1) {
                    System.out.println("List of UnReturned Book:");
                    System.out.println("|LibUID|CallNum|CopyNum|Checkout|");

                }
                System.out.println("|" + rs.getString("User_ID") + "|" + rs.getString("Call_number") + "|"
                        + rs.getInt("Copy_number") + "|" + rs.getDate("Check_out_date") + "|");
            }
            if (count == 0) {
                System.out.println("No UnReturned Book!");
            }
            System.out.println("End of Query");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("[ERROR] FAILED!");
        }

    }

    void librarian_book_return() {
        System.out.print("Enter The User ID: ");
        sc.nextLine();// ??????
        String userID = sc.nextLine();
        System.out.print("Enter The Call Number: ");
        String call_number = sc.nextLine();
        System.out.print("Enter The Copy Number: ");
        int copy_number = sc.nextInt();
        System.out.print("Enter Your Rating of the Book: ");
        int rating = sc.nextInt();
        try {
            String query = "SELECT * FROM  Checked_Out_Records c WHERE c.call_number = ? AND c.Copy_number = ? AND c.User_ID = ? AND c.Return_date is NULL;";
            PreparedStatement checkout_record = con.prepareStatement(query);
            checkout_record.setString(1, call_number);
            checkout_record.setInt(2, copy_number);
            checkout_record.setString(3, userID);
            ResultSet rs = checkout_record.executeQuery();
            if (rs.next()) {
                try {
                    String update = "UPDATE Checked_Out_Records c SET Return_date = ? WHERE c.call_number = ? AND c.Copy_number = ? AND c.User_ID = ?";
                    PreparedStatement checkout_update = con.prepareStatement(update);
                    java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis());

                    checkout_update.setDate(1, sqlDate);
                    checkout_update.setString(2, call_number);
                    checkout_update.setInt(3, copy_number);
                    checkout_update.setString(4, userID);
                    checkout_update.executeUpdate();
                    // update rating point
                    String books_query = "SELECT rating, no_of_times_borrowed FROM  Books WHERE call_number = ?;";
                    PreparedStatement books_query_statement = con.prepareStatement(books_query);
                    books_query_statement.setString(1, call_number);
                    ResultSet books_rs = books_query_statement.executeQuery();
                    books_rs.next();
                    int new_borrowed_times = books_rs.getInt("no_of_times_borrowed") + 1;
                    float new_rating = (books_rs.getFloat("rating") * (new_borrowed_times - 1) + rating)
                            / new_borrowed_times;

                    String books_update_statement = "UPDATE Books SET rating = ?, no_of_times_borrowed = ? WHERE call_number = ?";
                    PreparedStatement books_update = con.prepareStatement(books_update_statement);
                    books_update.setFloat(1, new_rating);
                    books_update.setInt(2, new_borrowed_times);
                    books_update.setString(3, call_number);
                    books_update.executeUpdate();

                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("[ERROR] Failed.");

                }

                System.out.println("Book returning performed succesfully");

            } else {
                System.out.println("Empty set!");
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("[ERROR] Failed.");
        }

    }

    void librarian_book_borrowing() {

        System.out.print("Enter The User ID: ");
        sc.nextLine();// ??????
        String userID = sc.nextLine();
        System.out.print("Enter The Call Number: ");
        String call_number = sc.nextLine();
        System.out.print("Enter The Copy Number: ");
        int copy_number = sc.nextInt();
        try {
            String query = "SELECT * FROM  Checked_Out_Records c WHERE c.call_number = ? AND c.Copy_number = ? AND c.Return_date is NULL;";
            PreparedStatement checkout_record = con.prepareStatement(query);
            checkout_record.setString(1, call_number);
            checkout_record.setInt(2, copy_number);
            ResultSet rs = checkout_record.executeQuery();
            if (!rs.next()){
                try {
                    String sql_insert = "INSERT INTO Checked_Out_Records VALUES (?, ?, ?, ?, NULL);";

                    java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis());

                    PreparedStatement checkout_insert = con.prepareStatement(sql_insert);
                    checkout_insert.setString(1, call_number);
                    checkout_insert.setInt(2, copy_number);
                    checkout_insert.setString(3, userID);
                    checkout_insert.setDate(4, sqlDate);
                    checkout_insert.executeUpdate();
                    System.out.println("Book borrowing performed successfully.");

                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("[ERROR] Failed.");
                }
            }
            else {
                System.out.println("Current book have been borrowed!");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("[ERROR] Failed.");
        }

    }

    void return_menu() {
        welcome_page();
        this.identity_choice = 0;
    }

    void create_table() {
        print_processing();
        String create_UC = "CREATE TABLE User_Categories ( ucid INT(1) NOT NULL, max_book INT(2) NOT NULL, loan_period INT(2) NOT NULL, PRIMARY KEY (ucid) );";
        String create_BC = "CREATE TABLE Book_Categories ( bcid INT(1) NOT NULL,bc_name VARCHAR(30) NOT NULL,PRIMARY KEY (bcid));";
        String create_Book = "CREATE TABLE Books (call_number VARCHAR(8) NOT NULL,no_of_copies INT(1) NOT NULL,title VARCHAR(30) NOT NULL,author VARCHAR(100) NOT NULL, dop DATE NOT NULL, rating FLOAT(1) unsigned, no_of_times_borrowed INT(2) unsigned NOT NULL, bcid INT(1) NOT NULL, PRIMARY KEY (call_number),FOREIGN KEY (bcid) REFERENCES Book_Categories(bcid)  );";
        String create_library_user = "CREATE TABLE Library_Users ( User_ID VARCHAR(10) NOT NULL,  Name VARCHAR(25) NOT NULL, Age INT(3) unsigned NOT NULL,  Address VARCHAR(100) NOT NULL,ucid INT(1) NOT NULL,  PRIMARY KEY (User_ID), FOREIGN KEY (ucid) REFERENCES User_Categories(ucid));";
        String create_CheckOut = "CREATE TABLE Checked_Out_Records ( Call_number VARCHAR(8) NOT NULL, Copy_number INT(1) unsigned NOT NULL, User_ID VARCHAR(10) NOT NULL,Check_out_date DATE NOT NULL, Return_date DATE, PRIMARY KEY (Call_number, Check_out_date), FOREIGN KEY(Call_number) REFERENCES Books(call_number), FOREIGN KEY(User_ID) REFERENCES Library_Users(User_ID));";

        try {
            PreparedStatement sql_create_UC = con.prepareStatement(create_UC);
            PreparedStatement sql_create_BC = con.prepareStatement(create_BC);
            PreparedStatement sql_create_Book = con.prepareStatement(create_Book);
            PreparedStatement sql_create_CheckOut = con.prepareStatement(create_CheckOut);
            PreparedStatement sql_create_library_user = con.prepareStatement(create_library_user);
            con.setAutoCommit(false);
            try {
                sql_create_UC.execute();
                sql_create_BC.execute();
                sql_create_Book.execute();
                sql_create_library_user.execute();
                sql_create_CheckOut.execute();
                con.commit();
                System.out.println("All tables are created.");
            } catch (SQLException ex) {
                con.rollback();
                System.out.println(ex.getSQLState());
                System.out.println(ex.getMessage());
                System.out.println("[ERROR] Failed to create tables.");
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to create tables.");
        }
    }

    void drop_table() {
        print_processing();
        // String alter = "ALTER TABLE
        // User_Categories,Book_Categories,Books,Checked_Out_Records,Library_Users
        // NOCHECK CONSTRAINT all";
        String disable = "SET FOREIGN_KEY_CHECKS=0";
        // String drop_child = "DROP TABLE IF EXISTS ;";
        String drop_table = "DROP TABLE IF EXISTS Book_Categories,User_Categories,Books,Library_Users,Checked_Out_Records;";
        String enable = "SET FOREIGN_KEY_CHECKS=1";

        try {
            PreparedStatement sql_drop_tabel = con.prepareStatement(drop_table);
            PreparedStatement sql_disable = con.prepareStatement(disable);
            PreparedStatement sql_enable = con.prepareStatement(enable);
            con.setAutoCommit(false);
            // PreparedStatement sql_alter = con.prepareStatement(alter);
            try {
                sql_disable.execute();
                sql_drop_tabel.execute();
                sql_enable.execute();
                // sql_alter.execute();
                con.commit();
                System.out.println("All tables are deleted.");
            } catch (SQLException ex) {
                con.rollback();
                System.out.println(ex.getSQLState());
                System.out.println(ex.getMessage());
                System.out.println("[ERROR] Failed to delete tables.");
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to delete tables.");
        }
    }

    void print_processing() {
        System.out.println("Processing......");
    }

    void load_file_data() {
        System.out.print("Enter folder path:");
        sc.nextLine();
        String folder_path = sc.nextLine();
        File dir = new File("./" + folder_path);
        File[] directoryListing = dir.listFiles();

        if (directoryListing != null) {
            String add_book = "";
            String add_bc = "";
            String add_user = "";
            String add_uc = "";
            String add_checkout = "";
            for (File child : directoryListing) {
                try {
                    BufferedReader br = new BufferedReader(new FileReader(child));
                    // StringBuilder sb = new StringBuilder();
                    String line = br.readLine();
                    ArrayList<ArrayList<String>> tmp_list = new ArrayList<ArrayList<String>>();

                    while (line != null) {
                        ArrayList<String> lines = new ArrayList<String>();
                        for (String retval : line.split("\\t")) {
                            // System.out.println(retval);
                            if (isNumeric(retval) == true) {
                                lines.add(retval);
                            } else {
                                lines.add('"' + retval + '"');
                            }
                        }
                        tmp_list.add(lines);
                        line = br.readLine();
                    }
                    System.out.println(child.getName());
                    ArrayList<String> tmp_string = new ArrayList<String>();
                    for (ArrayList<String> li : tmp_list) {
                        // ArrayList<String> cop = li;
                        if (child.getName().equals("check_out.txt")) {
                            for (int col = 1; col < 3; col++) {
                                String date = li.get(li.size() - col).substring(1,
                                        li.get(li.size() - col).length() - 1);
                                if (date.equals("null")) {
                                    continue;
                                }
                                ArrayList<String> sqldate = new ArrayList<String>();
                                String[] tmp = date.split("/", -col);
                                for (int counter = 2; counter >= 0; counter--) {
                                    // System.out.print(tmp[counter]);
                                    sqldate.add(tmp[counter]);
                                }
                                li.set(li.size() - col, '"' + join("/", sqldate) + '"');
                                // System.out.println(join("/", sqldate));
                            }

                        } else if (child.getName().equals("book.txt")) {
                            String date = li.get(4).substring(1, li.get(4).length() - 1);
                            // System.out.println(date);
                            ArrayList<String> sqldate = new ArrayList<String>();
                            String[] tmp = date.split("/", -1);
                            // System.out.print(tmp);

                            for (int counter = 2; counter >= 0; counter--) {
                                // System.out.print(tmp[counter]);
                                sqldate.add(tmp[counter]);
                            }
                            li.set(4, '"' + join("/", sqldate) + '"');
                            // System.out.println(join("/", sqldate));
                        }
                        for (int j = 0; j < li.size(); j++) {
                            if (li.get(j).equals("\"null\"")) {
                                // System.out.println("\"null\"");
                                li.set(j, "NULL");
                            }
                        }
                        // System.out.print(li);
                        String values = join(",", li);
                        tmp_string.add("(" + values + ")");
                    }
                    if (child.getName().equals("book_category.txt")) {
                        add_bc = "INSERT INTO Book_Categories VALUES " + join(",", tmp_string) + ";";
                    } else if (child.getName().equals("book.txt")) {
                        add_book = "INSERT INTO Books VALUES " + join(",", tmp_string) + ";";
                    } else if (child.getName().equals("user_category.txt")) {
                        add_uc = "INSERT INTO User_Categories VALUES " + join(",", tmp_string) + ";";
                    } else if (child.getName().equals("user.txt")) {
                        add_user = "INSERT INTO Library_Users VALUES " + join(",", tmp_string) + ";";
                    } else if (child.getName().equals("check_out.txt")) {
                        add_checkout = "INSERT INTO Checked_Out_Records VALUES " + join(",", tmp_string) + ";";
                    }
                    // System.out.println(add_checkout);
                    br.close();
                } catch (Exception e) {
                    System.out.println(e.toString());
                    System.out.println("[ERROR]");
                }
            }
            try {
                PreparedStatement sql_add_bc = con.prepareStatement(add_bc);
                PreparedStatement sql_add_book = con.prepareStatement(add_book);
                PreparedStatement sql_add_uc = con.prepareStatement(add_uc);
                PreparedStatement sql_add_user = con.prepareStatement(add_user);
                PreparedStatement sql_add_checkout = con.prepareStatement(add_checkout);
                con.setAutoCommit(false);
                try {
                    sql_add_uc.execute();
                    sql_add_bc.execute();
                    sql_add_book.execute();
                    sql_add_user.execute();
                    sql_add_checkout.execute();
                    con.commit();
                    System.out.println("Data added!");
                } catch (SQLException ex) {
                    con.rollback();
                    System.out.println(ex.getSQLState());
                    System.out.println(ex.getMessage());
                    System.out.println("[ERROR] Failed to add data.");
                }
            } catch (Exception e) {
                System.out.println(e.toString());
                System.out.println("[ERROR] Failed to add data.");
            }
        }
    }

    void count_records() {
        String count_records = "SELECT( SELECT COUNT(*) FROM User_Categories ) AS User_Categories, (SELECT COUNT(*) FROM Book_Categories) AS Book_Categories, (SELECT COUNT(*) FROM Books ) AS Books, (SELECT COUNT(*)FROM Library_Users ) AS Library_Users, (SELECT COUNT(*) FROM Checked_Out_Records ) AS Checked_Out_Records FROM dual";
        try {
            PreparedStatement sql_count_records = con.prepareStatement(count_records);
            try {
                ResultSet result = sql_count_records.executeQuery();
                if (!result.isBeforeFirst()) {
                    System.out.println("[ERROR] No records found.");
                } else {
                    ResultSetMetaData rsmd = result.getMetaData();
                    int columnsNumber = rsmd.getColumnCount();
                    System.out.println("Number of records in each table: ");
                    while (result.next()) {
                        for (int i = 1; i <= columnsNumber; i++) {
                            String columnValue = result.getString(i);
                            System.out.println(rsmd.getColumnName(i) + ": " + columnValue);
                        }
                    }
                }

            } catch (SQLException ex) {
                System.out.println(ex.getSQLState());
                System.out.println(ex.getMessage());
                System.out.println("[ERROR] Failed to count data.");
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            System.out.println("[ERROR] Failed to count data.");
        }
    }

    void connect_db() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(dbAddress, dbUserName, dbPassword);
            System.out.println(con);
        } catch (ClassNotFoundException e) {
            System.out.println("[Error]: JAVA DB not found");
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    private static String join(String separator, List<String> input) {
        if (input == null || input.size() <= 0)
            return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.size(); i++) {
            sb.append(input.get(i));
            // if not the last item
            if (i != input.size() - 1) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }

    private static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
