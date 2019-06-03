package jdbc.oracle.customer;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Vector;

import org.json.simple.JSONObject;

import jdbc.oracle.Relation;

public class Customers {
	
	// date 형식을 지정한다.
	private static SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	// Static 형태로 relation을 초기화한다.
	private static Relation relation;
	static {
		try {
			relation = new Relation();
		} catch (Exception e) {
			e.printStackTrace();
			throw new ExceptionInInitializerError(e);
		}
	}
	
	/**
	 * 품명, 수량, 물품 상세 정보를 가지고 주문 정보를 DB에 넣는 함수
	 * @param _list
	 * @return String 접수된 주문에 해당하는 대기 번호
	 * @throws NumberFormatException 
	 * @throws SQLException
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static String setOrders(JSONObject _list) throws NumberFormatException, SQLException, Exception {
		
		// 승인번호 및 주문 날짜를 위한 변수 선언
		long _millis = System.currentTimeMillis() * 1000;
		
		
		// JSONObject의 값들을 별도의 변수에 저장
		Vector<String> _nItem = (Vector<String>) _list.get("name");
		Vector<String> _dItem = (Vector<String>) _list.get("detail");
		Vector<String> _qItem = (Vector<String>) _list.get("quantity");
		
		
		// 작업 시작 전 필수 조건 검사
		if (!((_nItem.size() == _qItem.size()) && (_dItem.size() == _qItem.size()))) { throw new Exception("인자값이 잘못되었습니다."); }
		if ((_nItem.size() <= 0) || (_dItem.size() <= 0) || (_qItem.size() <= 0)) { throw new Exception("주문 수량이 잘못되었습니다."); }
		
		
		// 품명 및 옵션을 가지고 품번으로 변경
		for (int i = 0; i < _nItem.size(); i++) {
			
			// 품명 + 옵션 => 품번
			_nItem.set(i, Items.getItemNumber(_nItem.get(i), _dItem.get(i)));
			
			// 물품 번호 및 물품 상세 번호에 대한 오류 검사
			if (_nItem.get(i).equals("")) { throw new Exception("품명 혹은 옵션이 잘못되었습니다."); }
		}
		
		// SQL 처리를 위한 변수 선언
		String SQL;
		
		try {
			
			// Auto Commit 해제
			relation.getJDBCManager().getConnection().setAutoCommit(false);
			
			// 주문 테이블에 정보 입력
			SQL = "INSERT INTO " +
					"ORDERS_TB(ORDER_SQ, ORDER_STATUS_SQ, ORDER_DT) " +
					"VALUES ('" + _millis +  "', 0, TO_DATE('" + date.format(System.currentTimeMillis()) + "', 'YYYY-MM-DD HH24:MI:SS'))";
			
			relation.updateSQL(SQL);
			
			// 고객 테이블에 정보 입력
			SQL = "INSERT INTO " +
					"CUSTOMERS_TB(ORDER_SQ) " +
					"VALUES ('" + _millis + "')";
			
			relation.updateSQL(SQL);
			
			// 주문 상세 테이블에 주문 상세 정보 입력
			for (int i = 0; i < _nItem.size(); i++) {
				
				SQL = "INSERT INTO " +
						"ORDERS_DETAILS_TB(ORDER_SQ, ITEM_SQ, ITEM_QUANTITY_NO, ORDER_STATUS_SQ) " +
						"VALUES ('" + _millis + "', '" + _nItem.get(i) + "', " + _qItem.get(i) + ", 0)";
				
				relation.updateSQL(SQL);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			// 롤백 및 오토커밋 실행
			setRollBackAndAutoCommit();
			throw new SQLException(e);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			// 롤백 및 오토커밋 실행
			setRollBackAndAutoCommit();
			throw new Exception(e);
		}
		
		
		// DB Commit
		relation.getJDBCManager().commit();
		
		// Auto Commit 설정
		relation.getJDBCManager().getConnection().setAutoCommit(true);
		
		
		// 주문 번호에 할당된 대기 번호 조회
		SQL = "SELECT " +
				"CT.CUST_SQ " +
				"FROM CUSTOMERS_TB CT " +
				"WHERE CT.ORDER_SQ = '" + _millis + "'";
					
		relation.setSQL(SQL);
		
		// 현재 주문자의 주문 번호 = 받은 주문 번호 - 금일 시작 주문번호 + 1
		return String.valueOf(Integer.parseInt(relation.getIntension().get(0).get("CUST_SQ").toString()) - Integer.parseInt(getStartCustomerNumberAtToday()) + 1);
	}
	
	
	/**
	 * 오늘 날짜의 첫 주문 번호를 가져오는 함수
	 * @param _startDate
	 * @param _endDate
	 * @return String 대기 번호
	 * @throws SQLException
	 * @throws Exception
	 */
	private static String getStartCustomerNumberAtToday() throws SQLException, Exception {
		String SQL = "SELECT " +
				"CT.CUST_SQ " +
				"FROM CUSTOMERS_TB CT, ORDERS_TB OT " +
				"WHERE CT.ORDER_SQ = OT.ORDER_SQ AND " +
				"OT.ORDER_DT BETWEEN TO_CHAR(SYSDATE, 'YYYY-MM-DD') AND TO_CHAR(SYSDATE + 1, 'YYYY-MM-DD') " +
				"ORDER BY CT.CUST_SQ ASC";
		
		relation.setSQL(SQL);
		return relation.getIntension().get(0).get("CUST_SQ").toString();
	}
	
	
	/**
	 * DB 정보를 RollBack 하고 AutoCommit을 활성화 하는 함수
	 * @throws SQLException
	 */
	private static void setRollBackAndAutoCommit() throws SQLException {
		// DB RollBack
		relation.getJDBCManager().rollback();
					
		// Auto Commit 설정
		relation.getJDBCManager().getConnection().setAutoCommit(true);
	}
}
