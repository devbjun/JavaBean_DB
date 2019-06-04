package jdbc.oracle.manager;

import java.sql.SQLException;

import jdbc.oracle.Relation;

public class Customers {
	
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
	 * 오늘 날짜의 첫 주문 번호를 가져오는 함수
	 * @param _startDate
	 * @param _endDate
	 * @return String
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 * @throws Exception 
	 */
	public static String getStartCustomerNumberAtToday() throws ClassNotFoundException, SQLException, Exception {
		String SQL = "SELECT " +
				"CT.CUST_SQ " +
				"FROM CUSTOMERS_TB CT, ORDERS_TB OT " +
				"WHERE CT.ORDER_SQ = OT.ORDER_SQ AND " +
				"OT.ORDER_DT BETWEEN TO_CHAR(SYSDATE, 'YYYY-MM-DD') AND TO_CHAR(SYSDATE + 1, 'YYYY-MM-DD') " +
				"ORDER BY CT.CUST_SQ ASC";
		
		relation.setSQL(SQL);
		
		// 값이 존재하지 않을 경우 -1 반환
		if (relation.getIntension().isEmpty()) { return "-1"; }
		return relation.getIntension().get(0).get("CUST_SQ").toString();
	}
}
