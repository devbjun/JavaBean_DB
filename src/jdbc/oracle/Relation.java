package jdbc.oracle;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import org.json.simple.JSONObject;

import jdbc.JDBCManager;

public class Relation {
	
	/**
	 * Customers Relation JSON Example
	 * <{
	 *		"id": "a",
	 *		"account": "b",
	 *		"password": "c",
	 *		"name": "d",
	 *		"point": 0,
	 *		"type": "f"
	 * },
	 * 	...
	 * > Vector<JSONObject> 
	 */
	private Vector<JSONObject> intension;
	
	// JDBC 변수 선언
	private JDBCManager jdbc;
	private String sql;
	
	/**
	 * 빈 Relation 정보를 지니는 클래스로 초기화
	 */
	public Relation() {
		// 기본 값 설정
		try {
			jdbc = new JDBCManager();
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 입력받은 정보에 해당하는 Relation 정보를 지니는 클래스로 초기화
	 * @param String _sql
	 * @throws Exception
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public Relation(String _sql) throws Exception, ClassNotFoundException, SQLException {
		// 기본 값 설정
		jdbc = new JDBCManager();
		sql = _sql;
		
		// 최신 릴레이션 정보를 가져옴
		getLatest();
	}
	
	/**
	 * JDBCManager 반환
	 * @return JDBCManager
	 */
	public JDBCManager getJDBCManager() {
		return jdbc;
	}
	
	/**
	 * 입력받은 _sql에 해당하는 릴레이션 정보를 DB로부터 받아와 설정한다.
	 * @param String _sql
	 * @throws Excetpion
	 * @throws SQLException
	 */
	public void setSQL(String _sql) throws Exception, SQLException {
		sql = _sql;
		
		// 최신 정보로 업데이트
		getLatest();
	}
	
	/**
	 * 입력받은 _sql에 해당하는 릴레이션 정보를 DB로부터 받아와 설정한다.
	 * @param String _sql
	 * @throws Excetpion
	 * @throws SQLException
	 */
	public int updateSQL(String _sql) throws Exception, SQLException {
		return jdbc.executeUpdate(_sql);
	}
	
	/**
	 * intension값을 반환
	 * @return Vector<JSONObject>
	 */
	public Vector<JSONObject> getIntension() throws Exception {
		if (intension == null) { throw new Exception("릴레이션 정보를 가져올 수 없습니다."); }
		return intension;
	}
	
	/**
	 * _i값에 해당하는 특정 튜플 반환
	 * @param _i
	 * @return JSONObject
	 * @throws Exception
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public JSONObject getTuple(int _i) throws Exception, ArrayIndexOutOfBoundsException {
		if (intension == null) { throw new Exception("릴레이션 정보를 가져올 수 없습니다."); }
		if (_i >= intension.size()) { throw new ArrayIndexOutOfBoundsException(); }
		return intension.get(_i);
	}
	
	/**
	 * _in
	 * @param _instance
	 * @return JSONObject
	 * @throws Exception
	 */
	public JSONObject getTuple(String _instance) throws Exception {
		if (intension == null) { throw new Exception("릴레이션 정보를 가져올 수 없습니다."); }
		
		Iterator<?> keys;
		for (JSONObject json : intension) {
				
			keys = json.keySet().iterator();
			while (keys.hasNext()) {
					
				if (json.get(keys.next().toString()).equals(_instance)) {
					return json;
				}
			}
		}
		
		throw new Exception("입력한 인스턴스 값이 잘못되었습니다.");
	}
	
	/**
	 * _attribute에 해당하는 Column을 반환 
	 * @param _attribute
	 * @return String[]
	 * @throws Exception
	 */
	public String[] getColumn(String _attribute) throws Exception {
		if (intension == null) { throw new Exception("릴레이션 정보를 가져올 수 없습니다."); }
		
		Vector<String> result = new Vector<String>();
		Iterator<?> keys;
		String key;
			
		for (JSONObject json : intension) {
				
			keys = json.keySet().iterator();
			while (keys.hasNext()) {
					
				key = keys.next().toString();
				if (key.equals(_attribute.toUpperCase())) {
					result.add(json.get(key).toString());
				}
			}
		}
			
		return result.toArray(new String[result.size()]);
	}
	
	/**
	 * 업데이트된 최신 릴레이션 정보를 가져오는 함수
	 * @throws Exception
	 * @throws SQLException
	 */
	@SuppressWarnings("unchecked")
	public void getLatest() throws Exception, SQLException {
		if (sql == null) { throw new Exception("쿼리 정보를 가져올 수 없습니다."); }
		
		ResultSet resultSet = jdbc.executeQuery(
				((sql.contains("SELECT")) ? (sql) : ("SELECT * FROM " + sql)).toUpperCase()
		);
		ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
			
		intension = new Vector<JSONObject>();
		while (resultSet.next()) {
			JSONObject tuple = new JSONObject();
			
			String[] order = new String[resultSetMetaData.getColumnCount()];
			for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
				order[i] = resultSetMetaData.getColumnName(i + 1).toString();
			}
			
			tuple.put("order", order);
			for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
				tuple.put(
					resultSetMetaData.getColumnName(i).toString(), 
					resultSet.getString(resultSetMetaData.getColumnName(i))
				);
			};
			
			intension.add(tuple);
		}
	}
}
