import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class WikiConnect {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;

        try {
        	Scanner scanner = new Scanner(System.in);
            con = DriverManager.getConnection(
            		"jdbc:mysql://ec2-54-245-163-76.us-west-2.compute.amazonaws.com:3306/wikidb",
            		"god", "jesus");
            
            while (true) {
            	System.out.print("Enter a query: ");
            	String query = scanner.nextLine();
            	query = query.toLowerCase().replace(' ', '_');
                
                // Statement
                st = con.createStatement();
                rs = st.executeQuery("SELECT * FROM page WHERE LOWER(page_title) = '" + query + "';");//MATCH(page_title) AGAINST('University_of_[Ww]ashington');");

                int numCols = rs.getMetaData().getColumnCount();
                
                while (rs.next()) {
                	System.out.print("[");
                	for (int i = 1; i <= numCols; ++i) {
                		if (i != 1) {
                			System.out.print(" | ");
                		}
                		System.out.print("'" + rs.getString(i) + "'");
                	}
                	System.out.println("]");
                }
            }


        } catch (SQLException ex) {
        	System.out.println("Exception: " + ex);

        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (st != null) {
                    st.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
            	System.out.println("Exception: " + ex);
            }
        }
    }
}
