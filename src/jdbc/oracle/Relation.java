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
	 * 릴레이션의 정보를 저장하기 위한 특수 배열 객체
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
	public Relation() throws ClassNotFoundException, SQLException {
		// 기본 값 설정
		jdbc = JDBCManager.getJDBCManager();
	}
	
	/**
	 * 입력받은 정보에 해당하는 Relation 정보를 지니는 클래스로 초기화
	 * _sql은 쿼리문 혹은 릴레이션의 이름이어야 한다.
	 * @param String _sql
	 * @throws Exception
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public Relation(String _sql) throws Exception, ClassNotFoundException, SQLException {
		// 기본 값 설정
		jdbc = JDBCManager.getJDBCManager();
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
		
		// 연결 종료 전, 결과값 저장
		int result = jdbc.executeUpdate(_sql);
		
		// PreparedStatement 연결 종료
		jdbc.setClosePreStatement();
		
		// 결과값 반환
		return result;
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
	 * _instance 값을 지니고 있는 해당 튜플에 대한 정보를 반환한다.
	 * @param _instance
	 * @return JSONObject
	 * @throws Exception
	 */
	public JSONObject getTuple(String _instance) throws Exception {
		if (intension == null) { throw new Exception("릴레이션 정보를 가져올 수 없습니다."); }
		
		// 처리를 위한 변수 선언
		Iterator<?> keys;
		
		// 튜플을 돌며 인스턴스 값을 가지고 있는 검사한다.
		for (JSONObject json : intension) {
				
			keys = json.keySet().iterator();
			while (keys.hasNext()) {
				
				// 특정 인스턴스 값을 가지고 있는 튜플 발견시, 처리를 종료하고 값을 반환한다.
				if (json.get(keys.next().toString()).equals(_instance)) {
					return json;
				}
			}
		}
		
		throw new Exception("입력한 인스턴스 값이 잘못되었습니다.");
	}
	
	/**
	 * _attribute에 해당하는 열 전체 정보를 반환 
	 * @param _attribute
	 * @return String[]
	 * @throws Exception
	 */
	public String[] getColumn(String _attribute) throws Exception {
		if (intension == null) { throw new Exception("릴레이션 정보를 가져올 수 없습니다."); }
		
		// 처리를 위한 필요 변수 선언
		Vector<String> result = new Vector<String>();
		Iterator<?> keys;
		String key;
		
		// 튜플을 돌며 해당 애트리뷰트 값을 result 벡터에 저장한다.
		for (JSONObject json : intension) {
				
			keys = json.keySet().iterator();
			while (keys.hasNext()) {
				
				// _attribute에 해당하는 값을 result Vector에 저장한다.
				key = keys.next().toString();
				if (key.equals(_attribute.toUpperCase())) {
					result.add(json.get(key).toString());
				}
			}
		}
		
		// 결과값 반환
		return result.toArray(new String[result.size()]);
	}
	
	/**
	 * 최신 릴레이션 정보를 가져오는 함수
	 * @throws Exception
	 * @throws SQLException
	 */
	@SuppressWarnings("unchecked")
	public void getLatest() throws Exception, SQLException {
		
		// 클래스 생성 후, SQL 초기화를 하지 않은 경우 오류 발생
		if (sql == null) { throw new Exception("쿼리 정보를 가져올 수 없습니다."); }
		
		/*
		 * SQL문이 SELECT로 시작하는지 검사한다.
		 * 만일 SELECT문으로 시작하면 SQL문을 executeQuery로 날리며
		 * 그렇지 않으면 SQL문은 Table Name이 되므로, 정상적인 조회를 할 수 있도록 쿼리문을 조합해 날리도록 한다.
		 */
		ResultSet resultSet = jdbc.executeQuery(
				((sql.contains("SELECT")) ? (sql) : ("SELECT * FROM " + sql)).toUpperCase()
		);
		
		// 결과에 대한 메타 정보를 받아온다.
		ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
		
		// 릴레이션에 대한 내포 & 외연 저장을 위한 Vector<JSONObject> 형태의 객체를 할당한다.
		intension = new Vector<JSONObject>();
		
		// DB로부터 받아온 정보를 intension 변수에 저장한다.
		while (resultSet.next()) {
			
			// 개별 정보를 하나의 튜플로 엮기 위한 JSONObject 객체를 할당한다.
			JSONObject tuple = new JSONObject();
			
			// JSONObject는 Key의 순서를 보장하지 않는다.
			// 따라서 DB로부터 받아온 애트리뷰트의 순서를 보장하기 위하여 순서를 저장한 별도의 배열을 추가로 작성한다.
			String[] order = new String[resultSetMetaData.getColumnCount()];
			for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
				order[i] = resultSetMetaData.getColumnName(i + 1).toString();
			}
			
			// 애트리뷰트 순서에 대한 정보를 "order": order로 키 - 값 할당한다.
			tuple.put("order", order);
			
			// 나머지 애트리뷰트에 해당하는 인스턴스 값들을 튜플에 키-값 형태로 저장한다.
			for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
				tuple.put(
					resultSetMetaData.getColumnName(i).toString(), 
					resultSet.getString(resultSetMetaData.getColumnName(i))
				);
			};
			
			// 릴레이션 내포에 튜플 정보를 저장한다.
			intension.add(tuple);
		}
		
		// ResultSet 연결 종료
		resultSet.close();
		
		// PreparedStatement 연결 종료
		jdbc.setClosePreStatement();
	}
}
