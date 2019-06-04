package jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * LazyHolder 방식을 따르는 SingleTone Class 선언
 */
public class JDBCManager {
	
	// DB 연결 관련 변수
	private Connection conn;
	
	// DB 연결 정보 설정
	private String driverName = "oracle.jdbc.driver.OracleDriver";
	private String url = "jdbc:oracle:thin:@13.124.160.185:1521:XE";
	private String user = "jcp";
	private String password = "jcp_1234#";
	
	// DB SQL 처리 관련 변수
	private PreparedStatement pstmt;
	
	/**
	 * 싱글톤 클래스 형태로 JDBCManager를 생성한다.
	 * @return JDBCManager
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static JDBCManager getJDBCManager() throws ClassNotFoundException, SQLException {
		return LazyHolder.INSTANCE;
	}
	
	
	/**
	 * Thread-Safe한 싱글톤 JDBCManager Class를 선언하도록한다.
	 * 아래의 방식은 LazyHolder를 따른다.
	 */
	private static class LazyHolder {
		private static final JDBCManager INSTANCE;
		
		// static 형태로 오류 처리
		static {
			try {
				
				INSTANCE = new JDBCManager();
			
			// 에러 처리
			} catch (ClassNotFoundException | SQLException e) {
				
				e.printStackTrace();
				throw new ExceptionInInitializerError(e);
				
			}
		}
	}
	
	
	/**
	 * DB 연결 및 관리를 위한 클래스
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	private JDBCManager() throws ClassNotFoundException, SQLException {
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
		
		pstmt = conn.prepareStatement(_sql);
		return pstmt.executeQuery();
	}
	
	/**
	 * SELECT를 제한 나머지 SQL문을 실행하고 결과를 반환하는 함수
	 * @param _sql
	 * @return Integer
	 * @throws SQLException
	 */
	public int executeUpdate(String _sql) throws SQLException {
		if (!_sql.contains("UPDATE") && !_sql.contains("DELETE") && !_sql.contains("INSERT") && !_sql.contains("COMMIT") && !_sql.contains("ROLLBACK")) {
			System.out.println("executeUpdate 함수에 SELECT문을 사용할 수 없습니다.");
			return -1; 
		}
		
		pstmt = conn.prepareStatement(_sql);
		return pstmt.executeUpdate();
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
	
	/**
	 * JDBC Connection 연결 종료
	 * @throws SQLException
	 */
	public void setClose() throws SQLException {
		conn.close();
	}
	
	/**
	 * PreparedStatement 연결 종료
	 * @throws SQLException
	 */
	public void setClosePreStatement() throws SQLException {
		pstmt.close();
	}
}
