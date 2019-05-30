package jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JDBCManager {
	
	private Connection conn;
	
	private String driverName = "oracle.jdbc.driver.OracleDriver";
	private String url = "jdbc:oracle:thin:@13.124.160.185:1521:XE";
	private String user = "jcp";
	private String password = "jcp_1234#";
	
	/**
	 * DB 연결 및 관리를 위한 클래스
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public JDBCManager() throws ClassNotFoundException, SQLException {
		Class.forName(driverName);
		conn = DriverManager.getConnection(
				url,
				user,
				password
		);
	}
	
	/**
	 * SELECT SQL문을 실행하고 결과를 반환하는 함수
	 * @param _sql
	 * @return ResultSet
	 * @throws SQLException
	 */
	public ResultSet executeQuery(String _sql) throws SQLException {
		if (!_sql.contains("SELECT")) {
			System.out.println("executeQeury 함수 SELECT문만 사용할 수 있습니다.");
			return null;
		}
		return conn.createStatement().executeQuery(_sql);
	}
	
	/**
	 * SELECT를 제한 나머지 SQL문을 실행하고 결과를 반환하는 함수
	 * @param _sql
	 * @return Integer
	 * @throws SQLException
	 */
	public int executeUpdate(String _sql) throws SQLException {
		if (_sql.contains("SELECT")) {
			System.out.println("executeUpdate 함수에 SELECT문을 사용할 수 없습니다.");
			return -1; 
		}
		return conn.createStatement().executeUpdate(_sql);
	}
	
	/**
	 * Commit 실행
	 * @return Integer
	 * @throws SQLException
	 */
	public int commit() throws SQLException {
		return conn.createStatement().executeUpdate("COMMIT");
	}
	
	/**
	 * RollBack 실행
	 * @return Integer
	 * @throws SQLException
	 */
	public int rollback() throws SQLException {
		return conn.createStatement().executeUpdate("ROLLBACK");
	}
	
	/**
	 * JDBC Connection 반환
	 * @return Connection
	 */
	public Connection getConnection() {
		return conn;
	}
}
