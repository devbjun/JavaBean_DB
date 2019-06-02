package jdbc.oracle.manager;

import java.sql.SQLException;
import java.util.Vector;

import org.json.simple.JSONObject;

import jdbc.oracle.Relation;

public class Managers {
	
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
	
	
	/*
	 * 메인 페이지에서의 작업 처리를 위한 함수 구현 영역
	 */
	
	/**
	 * 금일 전체 미수령 주문 내역(주문번호, 품명, 옵션, 단가, 옵션단가, 수량, 합계, 결제일시) 뷰 릴레이션 반환
	 * @see VIEW(CUST_SQ, ITEM_NM, ITEM_DETAIL_NM, ITEM_QUANTITY_NO, ITEM_PRICE_NO, ITEM_DETAIl_PRICE_NO, ITEM_TOTAL_PRICE_NO, ORDER_DT)
	 * @return Vector<JSONObject>
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public static Vector<JSONObject> getOrderNotReceivedAtToday() throws ClassNotFoundException, SQLException, Exception {
		String SQL = "SELECT " + 
				"CT.CUST_SQ AS 주문번호, " +
				"IT.ITEM_NM AS 품명, " + 
				"IDT.ITEM_DETAIL_NM AS 옵션, " + 
				"IT.ITEM_PRICE_NO AS 단가, " +
				"IDT.ITEM_DETAIL_PRICE_NO AS 옵션단가, " + 
				"ODT.ITEM_QUANTITY_NO AS 수량, " + 
				"((IT.ITEM_PRICE_NO+IDT.ITEM_DETAIL_PRICE_NO)*ODT.ITEM_QUANTITY_NO) AS 합계, " + 
				"OT.ORDER_DT AS 결제일시 " + 
				"FROM CUSTOMERS_TB CT, ITEMS_TB IT, ITEMS_DETAILS_TB IDT, ORDERS_TB OT, ORDERS_DETAILS_TB ODT, ORDERS_STATUS_TB OST " + 
				"WHERE CT.ORDER_SQ = OT.ORDER_SQ AND OT.ORDER_SQ = ODT.ORDER_SQ AND OT.ORDER_STATUS_SQ = OST.ORDER_STATUS_SQ AND OST.ORDER_STATUS_NM = '수령 대기' AND ODT.ITEM_SQ = IT.ITEM_SQ AND IT.ITEM_DETAIL_SQ = IDT.ITEM_DETAIL_SQ AND " +
				"OT.ORDER_DT BETWEEN TO_CHAR(SYSDATE, 'YYYY-MM-DD') AND TO_CHAR(SYSDATE + 1, 'YYYY-MM-DD') " +
				"ORDER BY OT.ORDER_DT DESC";
		
		relation.setSQL(SQL);
		Vector<JSONObject> intension = relation.getIntension();
		
		// 내포가 NULL인 경우, NULL로 채워진 테이블을만들고 값을 반환한다.
		if (intension.isEmpty()) {
			
			SQL = "SELECT " + 
					"MAX(CT.CUST_SQ) AS 주문번호, " +
					"MAX(IT.ITEM_NM) AS 품명, " + 
					"MAX(IDT.ITEM_DETAIL_NM) AS 옵션, " + 
					"MAX(IT.ITEM_PRICE_NO) AS 단가, " +
					"MAX(IDT.ITEM_DETAIL_PRICE_NO) AS 옵션단가, " + 
					"MAX(ODT.ITEM_QUANTITY_NO) AS 수량, " + 
					"MAX(((IT.ITEM_PRICE_NO+IDT.ITEM_DETAIL_PRICE_NO)*ODT.ITEM_QUANTITY_NO)) AS 합계, " + 
					"MAX(OT.ORDER_DT) AS 결제일시 " + 
					"FROM CUSTOMERS_TB CT, ITEMS_TB IT, ITEMS_DETAILS_TB IDT, ORDERS_TB OT, ORDERS_DETAILS_TB ODT, ORDERS_STATUS_TB OST " + 
					"WHERE CT.ORDER_SQ = OT.ORDER_SQ AND OT.ORDER_SQ = ODT.ORDER_SQ AND OT.ORDER_STATUS_SQ = OST.ORDER_STATUS_SQ AND OST.ORDER_STATUS_NM = '수령 대기' AND ODT.ITEM_SQ = IT.ITEM_SQ AND IT.ITEM_DETAIL_SQ = IDT.ITEM_DETAIL_SQ AND " +
					"OT.ORDER_DT BETWEEN TO_CHAR(SYSDATE, 'YYYY-MM-DD') AND TO_CHAR(SYSDATE + 1, 'YYYY-MM-DD') " +
					"ORDER BY OT.ORDER_DT DESC";
			
			relation.setSQL(SQL);
			return relation.getIntension();
		}
		
		// 주문 번호를 하루 단위로 변환한다.
		int nCustomer = Integer.parseInt(Customers.getStartCustomerNumberAtToday());
		for (int i = 0; i < intension.size(); i++) {
			intension.get(i).put("주문번호", Integer.parseInt(intension.get(i).get("주문번호").toString()) - nCustomer + 1);
		}
		
		return intension;
	}
	
	
	/**
	 * 주문번호를 와 상태 입력받아 주문상태를 입력받은 주문 상태로 업데이트하는 함수
	 * @param _nCustomer
	 * @param _nStatus
	 * @return Integer 쿼리문 처리 상태 반환 값
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 * @throws Exception 
	 */
	public static int setOrderComplete(String _nCustomer, String _nStatus) throws ClassNotFoundException, SQLException, Exception {
		
		int result;
		String SQL;
		
		try {
			
			String _nOrder = getOrderNumber(_nCustomer);

			// _nStatus 존재 여부 검사
			SQL = "SELECT " +
					"ORDER_STATUS_SQ " +
					"FROM ORDERS_STATUS_TB " +
					"WHERE ORDER_STATUS_NM " +
					"IN '" + _nStatus + "'";
			
			relation.setSQL(SQL);
			
			// 없을 시 오류 반환
			if (relation.getIntension().isEmpty()) {
				throw new Exception("인자값이 잘못되었습니다.");
			}
			
			// _nStatus를 SQ 값으로 대체
			_nStatus = relation.getIntension().get(0).get("ORDER_STATUS_SQ").toString();
			
			// ORDER_ST 업데이트
			SQL = "UPDATE " +
					"ORDERS_TB " +
					"SET " +
					"ORDER_STATUS_SQ = '" + _nStatus + "' " +
					"WHERE ORDER_SQ = '" + _nOrder + "'";
			
			// Auto Commit 해제
			relation.getJDBCManager().getConnection().setAutoCommit(false);
			
			// SQL문 실행
			result = relation.updateSQL(SQL);
			
		}  catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			// RollBack & AutoCommit 실행
			setRollBackAndAutoCommit();
			throw new SQLException(e);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			// RollBack & AutoCommit 실행
			setRollBackAndAutoCommit();
			throw new Exception(e);
			
		}
		
		// Commit 실행
		relation.getJDBCManager().commit();
		
		// Auto Commit 설정
		relation.getJDBCManager().getConnection().setAutoCommit(true);
		
		return result;
	}
	
	
	/**
	 * 주문번호에 해당하는 수령 대기 상태의 승인번호를 반환
	 * @param _nCustomer
	 * @return String
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 * @throws Exception 
	 */
	private static String getOrderNumber(String _nCustomer) throws ClassNotFoundException, SQLException, Exception {
		
		_nCustomer = String.valueOf(Integer.parseInt(_nCustomer) + Integer.parseInt(Customers.getStartCustomerNumberAtToday()) - 1);
		String SQL = "SELECT " + 
				"CT.ORDER_SQ " + 
				"FROM CUSTOMERS_TB CT, ORDERS_TB OT, ORDERS_STATUS_TB OST " + 
				"WHERE CT.ORDER_SQ = OT.ORDER_SQ AND OT.ORDER_STATUS_SQ = OST.ORDER_STATUS_SQ AND " + 
				"CUST_SQ = '" + _nCustomer + "' AND OST.ORDER_STATUS_NM = '수령 대기'";
		
		relation.setSQL(SQL);
		
		// 오류 반환
		if (relation.getIntension().isEmpty()) { throw new Exception("인자값이 잘못되었습니다."); }
		return relation.getIntension().get(0).get("ORDER_SQ").toString();
	}
	
	
	/*
	 * 관리 페이지에서의 작업 처리를 위한 함수 구현 영역
	 */

	/**
	 * 지정한 기간 동안의 수령 대기를 제외한 전체 주문 내역(승인번호, 품명, 옵션, 단가, 옵션단가, 수량, 합계, 상태, 결제일시) 뷰 릴레이션 반환
	 * @see VIEW(CUST_SQ, ITEM_NM, ITEM_DETAIL_NM, ITEM_QUANTITY_NO, ITEM_PRICE_NO, ITEM_DETAIl_PRICE_NO, ITEM_TOTAL_PRICE_NO, ORDER_STATUS_NM, ORDER_DT)
	 * @param _dStart
	 * @param _dEnd
	 * @return Vector<JSONObject>
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 * @throws Exception 
	 * @_dStart YYYY-MM-DD
	 * @_dEnd YYYY-MM-DD
	 */
	public static Vector<JSONObject> getOrderAtPeriod(String _dStart, String _dEnd) throws ClassNotFoundException, SQLException, Exception {
		
		String SQL = "SELECT " + 
				"OT.ORDER_SQ AS 승인번호, " +
				"IT.ITEM_NM AS 품명, " + 
				"IDT.ITEM_DETAIL_NM AS 옵션, " + 
				"IT.ITEM_PRICE_NO AS 단가, " +
				"IDT.ITEM_DETAIL_PRICE_NO AS 옵션단가, " + 
				"ODT.ITEM_QUANTITY_NO AS 수량, " + 
				"((IT.ITEM_PRICE_NO+IDT.ITEM_DETAIL_PRICE_NO)*ODT.ITEM_QUANTITY_NO) AS 합계, " + 
				"OST.ORDER_STATUS_NM AS 상태, " + 
				"OT.ORDER_DT AS 결제일시 " + 
				"FROM CUSTOMERS_TB CT, ITEMS_TB IT, ITEMS_DETAILS_TB IDT, ORDERS_TB OT, ORDERS_DETAILS_TB ODT, ORDERS_STATUS_TB OST " + 
				"WHERE CT.ORDER_SQ = OT.ORDER_SQ AND OT.ORDER_SQ = ODT.ORDER_SQ AND ODT.ITEM_SQ = IT.ITEM_SQ AND IT.ITEM_DETAIL_SQ = IDT.ITEM_DETAIL_SQ AND OT.ORDER_STATUS_SQ = OST.ORDER_STATUS_SQ AND OST.ORDER_STATUS_NM NOT IN '수령 대기' AND " +
				"OT.ORDER_DT BETWEEN '" + _dStart + "' AND '" + _dEnd + "' " + 
				"ORDER BY OT.ORDER_DT DESC";
			
		relation.setSQL(SQL);
		Vector<JSONObject> intension = relation.getIntension();
			
		// 내포가 NULL인 경우, NULL로 채워진 테이블을만들고 값을 반환한다.
		if (intension.isEmpty()) {
			
			SQL = "SELECT " + 
					"MAX(OT.ORDER_SQ) AS 승인번호, " +
					"MAX(IT.ITEM_NM) AS 품명, " + 
					"MAX(IDT.ITEM_DETAIL_NM) AS 옵션, " + 
					"MAX(IT.ITEM_PRICE_NO) AS 단가, " +
					"MAX(IDT.ITEM_DETAIL_PRICE_NO) AS 옵션단가, " + 
					"MAX(ODT.ITEM_QUANTITY_NO) AS 수량, " + 
					"MAX(((IT.ITEM_PRICE_NO+IDT.ITEM_DETAIL_PRICE_NO)*ODT.ITEM_QUANTITY_NO)) AS 합계, " + 
					"MAX(OST.ORDER_STATUS_NM) AS 상태, " + 
					"MAX(OT.ORDER_DT) AS 결제일시 " + 
					"FROM CUSTOMERS_TB CT, ITEMS_TB IT, ITEMS_DETAILS_TB IDT, ORDERS_TB OT, ORDERS_DETAILS_TB ODT, ORDERS_STATUS_TB OST " + 
					"WHERE CT.ORDER_SQ = OT.ORDER_SQ AND OT.ORDER_SQ = ODT.ORDER_SQ AND ODT.ITEM_SQ = IT.ITEM_SQ AND IT.ITEM_DETAIL_SQ = IDT.ITEM_DETAIL_SQ AND OT.ORDER_STATUS_SQ = OST.ORDER_STATUS_SQ AND OST.ORDER_STATUS_NM NOT IN '수령 대기' AND " +
					"OT.ORDER_DT BETWEEN '" + _dStart + "' AND '" + _dEnd + "' " + 
					"ORDER BY OT.ORDER_DT DESC";
				
				
			relation.setSQL(SQL);
			return relation.getIntension();
		}
		
		return intension;
	}
	
	/**
	 * 승인 번호에 대한 주문 상세 내역(승인번호, 품명, 옵션, 단가, 옵션단가, 수량, 합계, 상태, 결제일시) 뷰 릴레이션 반환
	 * @see VIEW(CUST_SQ, ITEM_NM, ITEM_DETAIL_NM, ITEM_QUANTITY_NO, ITEM_PRICE_NO, ITEM_DETAIl_PRICE_NO, ITEM_TOTAL_PRICE_NO, ORDER_STATUS_NM, ORDER_DT)
	 * @param _nOrder
	 * @return Vector<JSONObject>
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 * @throws Exception 
	 */
	public static Vector<JSONObject> getOrderDetailAtNumber(String _nOrder) throws ClassNotFoundException, SQLException, Exception {
		
		String SQL = "SELECT " + 
				"OT.ORDER_SQ AS 승인번호, " +
				"IT.ITEM_NM AS 품명, " + 
				"IDT.ITEM_DETAIL_NM AS 옵션, " + 
				"IT.ITEM_PRICE_NO AS 단가, " +
				"IDT.ITEM_DETAIL_PRICE_NO AS 옵션단가, " + 
				"ODT.ITEM_QUANTITY_NO AS 수량, " + 
				"((IT.ITEM_PRICE_NO+IDT.ITEM_DETAIL_PRICE_NO)*ODT.ITEM_QUANTITY_NO) AS 합계, " + 
				"OST.ORDER_STATUS_NM AS 상태, " + 
				"OT.ORDER_DT AS 결제일시 " + 
				"FROM CUSTOMERS_TB CT, ITEMS_TB IT, ITEMS_DETAILS_TB IDT, ORDERS_TB OT, ORDERS_DETAILS_TB ODT, ORDERS_STATUS_TB OST " + 
				"WHERE CT.ORDER_SQ = OT.ORDER_SQ AND OT.ORDER_SQ = ODT.ORDER_SQ AND ODT.ITEM_SQ = IT.ITEM_SQ AND IT.ITEM_DETAIL_SQ = IDT.ITEM_DETAIL_SQ AND OT.ORDER_STATUS_SQ = OST.ORDER_STATUS_SQ AND " +
				"OT.ORDER_SQ = '" + _nOrder + "' " +
				"ORDER BY OT.ORDER_DT DESC";
			
		relation.setSQL(SQL);
		Vector<JSONObject> intension = relation.getIntension();
			
		// 내포가 NULL인 경우, NULL로 채워진 테이블을만들고 값을 반환한다.
		if (intension.isEmpty()) {
			
			SQL = "SELECT " + 
					"MAX(OT.ORDER_SQ) AS 승인번호, " +
					"MAX(IT.ITEM_NM) AS 품명, " + 
					"MAX(IDT.ITEM_DETAIL_NM) AS 옵션, " + 
					"MAX(IT.ITEM_PRICE_NO) AS 단가, " +
					"MAX(IDT.ITEM_DETAIL_PRICE_NO) AS 옵션단가, " + 
					"MAX(ODT.ITEM_QUANTITY_NO) AS 수량, " + 
					"MAX(((IT.ITEM_PRICE_NO+IDT.ITEM_DETAIL_PRICE_NO)*ODT.ITEM_QUANTITY_NO)) AS 합계, " + 
					"MAX(OST.ORDER_STATUS_NM) AS 상태, " + 
					"MAX(OT.ORDER_DT) AS 결제일시 " + 
					"FROM CUSTOMERS_TB CT, ITEMS_TB IT, ITEMS_DETAILS_TB IDT, ORDERS_TB OT, ORDERS_DETAILS_TB ODT, ORDERS_STATUS_TB OST " + 
					"WHERE CT.ORDER_SQ = OT.ORDER_SQ AND OT.ORDER_SQ = ODT.ORDER_SQ AND ODT.ITEM_SQ = IT.ITEM_SQ AND IT.ITEM_DETAIL_SQ = IDT.ITEM_DETAIL_SQ AND OT.ORDER_STATUS_SQ = OST.ORDER_STATUS_SQ AND " +
					"OT.ORDER_SQ = '" + _nOrder + "' " +
					"ORDER BY OT.ORDER_DT DESC";
				
			relation.setSQL(SQL);
			return relation.getIntension();
		}
		
		return intension;
	}
	

	/**
	 * 특정 기간 동안의 지점 총 판매 금액을 반환
	 * @param _dStart
	 * @param _dEnd
	 * @return Integer
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 * @throws Exception 
	 * @_dStart YYYY-MM-DD
	 * @_dEnd YYYY-MM-DD
	 */
	public static int getStoreTotalPriceAtPeriod(String _dStart, String _dEnd) throws ClassNotFoundException, SQLException, Exception {
		String SQL = "SELECT " + 
				"SUM((IT.ITEM_PRICE_NO+IDT.ITEM_DETAIL_PRICE_NO)*ODT.ITEM_QUANTITY_NO) AS STORE_TOTAL_PRICE_NO " + 
				"FROM ITEMS_TB IT, ITEMS_DETAILS_TB IDT, ORDERS_TB OT, ORDERS_DETAILS_TB ODT, ORDERS_STATUS_TB OST " + 
				"WHERE OT.ORDER_SQ = ODT.ORDER_SQ AND OT.ORDER_STATUS_SQ = OST.ORDER_STATUS_SQ AND OST.ORDER_STATUS_NM = '수령 완료' AND ODT.ITEM_SQ = IT.ITEM_SQ AND IT.ITEM_DETAIL_SQ = IDT.ITEM_DETAIL_SQ AND " +
				"OT.ORDER_DT BETWEEN '" + _dStart + "' AND '" + _dEnd + "'";
		
		relation.setSQL(SQL);
		
		// 값을 받아와 값이 없는 경우 0을 반환
		if (relation.getIntension().get(0).get("STORE_TOTAL_PRICE_NO") == null) { return 0; }
		return Integer.parseInt(relation.getIntension().get(0).get("STORE_TOTAL_PRICE_NO").toString());
	}
	
	
	/**
	 * 지정한 기간동안의 요일별 총 매출액을 반환
	 * @param _dStart
	 * @param _dEnd
	 * @return Vector<JSONObject>
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 * @throws Exception 
	 * @_dStart YYYY-MM-DD
	 * @_dEnd YYYY-MM-DD
	 */
	@SuppressWarnings("unchecked")
	public static Vector<JSONObject> getStoreTotalPricePerDayAtPeriod(String _dStart, String _dEnd) throws ClassNotFoundException, SQLException, Exception {
		String SQL = "SELECT " + 
				"TO_DATE(TO_CHAR(ORDER_DT, 'YYYY-MM-DD'), 'YYYY-MM-DD') AS 날짜, " + 
				"SUM((IT.ITEM_PRICE_NO+IDT.ITEM_DETAIL_PRICE_NO)*ODT.ITEM_QUANTITY_NO) AS STORE_TOTAL_PRICE_NO " + 
				"FROM ITEMS_TB IT, ITEMS_DETAILS_TB IDT, ORDERS_TB OT, ORDERS_DETAILS_TB ODT, ORDERS_STATUS_TB OST " + 
				"WHERE OT.ORDER_SQ = ODT.ORDER_SQ AND OT.ORDER_STATUS_SQ = OST.ORDER_STATUS_SQ AND OST.ORDER_STATUS_NM = '수령 완료' AND ODT.ITEM_SQ = IT.ITEM_SQ AND IT.ITEM_DETAIL_SQ = IDT.ITEM_DETAIL_SQ AND " +
				"OT.ORDER_DT BETWEEN '" + _dStart + "' AND '" + _dEnd + "' " +
				"GROUP BY TO_DATE(TO_CHAR(ORDER_DT, 'YYYY-MM-DD'), 'YYYY-MM-DD') " +
				"ORDER BY TO_DATE(TO_CHAR(ORDER_DT, 'YYYY-MM-DD'), 'YYYY-MM-DD') ASC";
		
		relation.setSQL(SQL);
		Vector<JSONObject> intension = relation.getIntension();
		
		// 내포가 NULL인 경우, NULL로 채워진 테이블을만들고 값을 반환한다.
		if (intension.isEmpty()) { 
			
			SQL = "SELECT " + 
					"MAX(TO_DATE(TO_CHAR(ORDER_DT, 'YYYY-MM-DD'), 'YYYY-MM-DD')) AS 날짜, " + 
					"MAX(SUM((IT.ITEM_PRICE_NO+IDT.ITEM_DETAIL_PRICE_NO)*ODT.ITEM_QUANTITY_NO)) AS STORE_TOTAL_PRICE_NO " + 
					"FROM ITEMS_TB IT, ITEMS_DETAILS_TB IDT, ORDERS_TB OT, ORDERS_DETAILS_TB ODT, ORDERS_STATUS_TB OST " + 
					"WHERE OT.ORDER_SQ = ODT.ORDER_SQ AND OT.ORDER_STATUS_SQ = OST.ORDER_STATUS_SQ AND OST.ORDER_STATUS_NM = '수령 완료' AND ODT.ITEM_SQ = IT.ITEM_SQ AND IT.ITEM_DETAIL_SQ = IDT.ITEM_DETAIL_SQ AND " +
					"OT.ORDER_DT BETWEEN '" + _dStart + "' AND '" + _dEnd + "' " +
					"GROUP BY TO_DATE(TO_CHAR(ORDER_DT, 'YYYY-MM-DD'), 'YYYY-MM-DD') " +
					"ORDER BY TO_DATE(TO_CHAR(ORDER_DT, 'YYYY-MM-DD'), 'YYYY-MM-DD') ASC";
			
			relation.setSQL(SQL);
			return relation.getIntension();
		}
		
		// 날짜를 하루 단위로 변환한다.
		for (int i = 0; i < intension.size(); i++) {
			intension.get(i).put("날짜", intension.get(i).get("날짜").toString().split(" ")[0]);
		}
		
		return intension;
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
