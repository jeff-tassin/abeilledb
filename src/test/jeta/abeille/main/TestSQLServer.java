package test.jeta.abeille.main;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Properties;

public class TestSQLServer {

	private void testConnection() {
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			Properties props = new Properties();
			props.put("user", "DSUser");
			props.put("password", "DSUs3r1");
			Connection conn = DriverManager.getConnection("jdbc:sqlserver://devsql1.vcphanna.net:1433", props);
			DatabaseMetaData metadata = conn.getMetaData();

			System.out.println("get catalogs: ...");
			ResultSet rset = metadata.getCatalogs();
			// TABLE_CATALOG
			while (rset.next()) {
				System.out.println("got table catalog: " + rset.getString("TABLE_CAT"));
			}

			System.out.println("get schemas: ...");
			rset = metadata.getSchemas();

			// TABLE_CATALOG TABLE_SCHEM
			while (rset.next()) {
				System.out.println("get schemas:  table catalog: " + rset.getString("TABLE_CATALOG") + "  schema: "
						+ rset.getString("TABLE_SCHEM"));
			}

			System.out.println("getting tables.");
			rset = metadata.getTables(null, null, null, null);
			while (rset.next()) {
				System.out.println("got table: " + rset.getString("TABLE_NAME") + "  schema: "
						+ rset.getString("TABLE_SCHEM") + "  catalog: " + rset.getString("TABLE_CAT"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("test connection finished.");
	}

	public static void main(String[] args) {
		new TestSQLServer().testConnection();
	}

}
