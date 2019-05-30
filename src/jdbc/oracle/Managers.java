package jdbc.oracle;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Vector;

import org.json.simple.JSONObject;

public class Managers {
	
	private static Relation relation = new Relation();
	private static SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
	
	/**
	 * 지정한 기간 동안의 전체 주문 내역(승인번호, 품명, 옵션, 단가, 옵션단가, 수량, 합계, 결제일시)을 반환
	 * @see VIEW(CUST_SQ, ITEM_NM, ITEM_DETAIL_NM, ITEM_QUANTITY_NO, ITEM_PRICE_NO, ITEM_DETAIl_PRICE_NO, ITEM_TOTAL_PRICE_NO, ORDER_DT)
	 * @param boolean _isReceived
	 * @return Vector<JSONObject> 전체 주문 내역 리스트
	 * @throws Exception 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @_isReceived 수령 완료 여부
	 */
	@SuppressWarnings("unchecked")
	public static Vector<JSONObject> getAllTodayList(String _startDate, String _endDate, boolean _isReceived) throws Exception {
		String SQL = "SELECT " + 
				"OT.ORDER_SQ AS 승인번호, " +
				"IT.ITEM_NM AS 품명, " + 
				"IDT.ITEM_DETAIL_NM AS 옵션, " + 
				"IT.ITEM_PRICE_NO AS 단가, " +
				"IDT.ITEM_DETAIL_PRICE_NO AS 옵션단가, " + 
				"ODT.ITEM_QUANTITY_NO AS 수량, " + 
				"((IT.ITEM_PRICE_NO+IDT.ITEM_DETAIL_PRICE_NO)*ODT.ITEM_QUANTITY_NO) AS 합계, " + 
				"OT.ORDER_DT AS 결제일시 " + 
				"FROM CUSTOMERS_TB CT, ITEMS_TB IT, ITEMS_DETAILS_TB IDT, ORDERS_TB OT, ORDERS_DETAILS_TB ODT " + 
				"WHERE CT.ORDER_SQ = OT.ORDER_SQ AND OT.ORDER_SQ = ODT.ORDER_SQ AND OT.ORDER_ST = " + ((_isReceived) ? "1" : "0") + " AND ODT.ITEM_SQ = IT.ITEM_SQ AND IT.ITEM_DETAIL_SQ = IDT.ITEM_DETAIL_SQ AND " +
				"OT.ORDER_DT BETWEEN '" + _startDate + "' AND '" + _endDate + "' " + 
				"ORDER BY OT.ORDER_DT ASC";
		
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
					"MAX(OT.ORDER_DT) AS 결제일시 " + 
					"FROM CUSTOMERS_TB CT, ITEMS_TB IT, ITEMS_DETAILS_TB IDT, ORDERS_TB OT, ORDERS_DETAILS_TB ODT " + 
					"WHERE CT.ORDER_SQ = OT.ORDER_SQ AND OT.ORDER_SQ = ODT.ORDER_SQ AND OT.ORDER_ST = " + ((_isReceived) ? "1" : "0") + " AND ODT.ITEM_SQ = IT.ITEM_SQ AND IT.ITEM_DETAIL_SQ = IDT.ITEM_DETAIL_SQ AND " +
					"OT.ORDER_DT BETWEEN '" + _startDate + "' AND '" + _endDate + "' " + 
					"ORDER BY OT.ORDER_DT ASC";
			
			relation.setSQL(SQL);
			return relation.getIntension();
		}
		
		// 대기자 번호를 하루 단위로 변환한다.
		int customerNumber = Integer.parseInt(intension.get(0).get("CUST_SQ").toString());
		for (int i = 0; i < intension.size(); i++) {
			intension.get(i).put("CUST_SQ", Integer.parseInt(intension.get(i).get("CUST_SQ").toString()) - customerNumber + 1);
		}
		
		return intension;
	}

	/**
	 * 특정 기간 동안의 지점 총 판매 금액을 반환
	 * @param String _startDate
	 * @param String _endDate
	 * @return Integer 총 판매 금액
	 * @throws Exception 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @_startDate YYYY-MM-DD
	 * @_endDate YYYY-MM-DD
	 */
	public static int getStoreTotalPriceAtPeriod(String _startDate, String _endDate) throws Exception {
		String SQL = "SELECT " + 
				"SUM((IT.ITEM_PRICE_NO+IDT.ITEM_DETAIL_PRICE_NO)*ODT.ITEM_QUANTITY_NO) AS STORE_TOTAL_PRICE_NO " + 
				"FROM ITEMS_TB IT, ITEMS_DETAILS_TB IDT, ORDERS_TB OT, ORDERS_DETAILS_TB ODT " + 
				"WHERE OT.ORDER_SQ = ODT.ORDER_SQ AND OT.ORDER_ST = 1 AND ODT.ITEM_SQ = IT.ITEM_SQ AND IT.ITEM_DETAIL_SQ = IDT.ITEM_DETAIL_SQ AND " +
				"OT.ORDER_DT BETWEEN '" + _startDate + "' AND '" + _endDate + "'";
		
		relation.setSQL(SQL);
		Vector<JSONObject> intension = relation.getIntension();
		if (intension.get(0).get("STORE_TOTAL_PRICE_NO") == null) { return 0; }
		
		return Integer.parseInt(intension.get(0).get("STORE_TOTAL_PRICE_NO").toString());
	}
	
	/**
	 * 지정한 기간동안의 요일별 총 매출액을 반환
	 * @return Vector<JSONObject> 요일별 총 매출액 리스트
	 * @throws Exception 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	
	public static Vector<JSONObject> getStoreTotalPriceList(String _startDate, String _endDate) throws SQLException, Exception {
		String SQL = "SELECT " + 
				"TO_DATE(TO_CHAR(ORDER_DT, 'YYYY-MM-DD'), 'YYYY-MM-DD') AS 날짜, " + 
				"SUM((IT.ITEM_PRICE_NO+IDT.ITEM_DETAIL_PRICE_NO)*ODT.ITEM_QUANTITY_NO) AS STORE_TOTAL_PRICE_NO " + 
				"FROM ITEMS_TB IT, ITEMS_DETAILS_TB IDT, ORDERS_TB OT, ORDERS_DETAILS_TB ODT " + 
				"WHERE OT.ORDER_SQ = ODT.ORDER_SQ AND OT.ORDER_ST = 1 AND ODT.ITEM_SQ = IT.ITEM_SQ AND IT.ITEM_DETAIL_SQ = IDT.ITEM_DETAIL_SQ AND " +
				"OT.ORDER_DT BETWEEN '" + _startDate + "' AND '" + _endDate + "' " +
				"GROUP BY TO_DATE(TO_CHAR(ORDER_DT, 'YYYY-MM-DD'), 'YYYY-MM-DD') " +
				"ORDER BY TO_DATE(TO_CHAR(ORDER_DT, 'YYYY-MM-DD'), 'YYYY-MM-DD') ASC";
		
		relation.setSQL(SQL);
		return relation.getIntension();
	}
	
	/**
	 * 주문번호를 입력받아 주문상태를 수령 완료로 업데이트
	 * @param _orderNumber
	 * @return Integer 쿼리문 처리 상태 반환 값
	 * @throws Exception 
	 * @throws SQLException 
	 */
	public static int setOrderComplete(String _customerNumber) throws Exception, SQLException {
		
		int result;
		String SQL;
		
		try {
			
			String _orderNumber = getOrderNumber(_customerNumber, false);

			// _orderNumber 존재 여부 검사
			SQL = "SELECT " +
					"ORDER_SQ " +
					"FROM ORDERS_TB " +
					"WHERE ORDER_SQ " +
					"IN '" + _orderNumber + "'";
			
			relation.setSQL(SQL);
			if (relation.getIntension().get(0).get("ORDER_SQ").toString().equals("")) {
				throw new Exception("인자값이 잘못되었습니다.");
			}
			
			// ORDER_ST 업데이트
			SQL = "UPDATE " +
					"ORDERS_TB " +
					"SET " +
					"ORDER_ST = '1'" +
					"WHERE ORDER_SQ = '" + _orderNumber + "'";
			
			// Auto Commit 해제
			relation.getJDBCManager().getConnection().setAutoCommit(false);
			
			// SQL문 실행
			result = relation.updateSQL(SQL);
			
		}  catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			// RollBack 실행
			relation.getJDBCManager().rollback();
						
			// Auto Commit 설정
			relation.getJDBCManager().getConnection().setAutoCommit(true);
			
			throw new SQLException(e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			// RollBack 실행
			relation.getJDBCManager().rollback();
									
			// Auto Commit 설정
			relation.getJDBCManager().getConnection().setAutoCommit(true);
			
			throw new Exception(e);
		}
		
		// Commit 실행
		relation.getJDBCManager().commit();
		
		// Auto Commit 설정
		relation.getJDBCManager().getConnection().setAutoCommit(true);
		
		return result;
	}
	
	
	/**
	 * 주문번호에 해당하는 승인번호를 반환
	 * @param boolean _isReceived
	 * @return String 승인번호
	 * @throws Exception 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @_isReceived 수령 완료 여부
	 */
	private static String getOrderNumber(String _customerNumber, boolean _isReceived) throws Exception {
		
		_customerNumber = String.valueOf(Integer.parseInt(_customerNumber) + Integer.parseInt(getStartNumberAtPeriod(date.format(System.currentTimeMillis()), date.format(System.currentTimeMillis() + 86400 * 1000))) - 1);
		String SQL = "SELECT " + 
				"ORDER_SQ " + 
				"FROM CUSTOMERS_TB " + 
				"WHERE CUST_SQ = '" + _customerNumber + "' " + 
				"ORDER BY CUST_SQ ASC";
		
		relation.setSQL(SQL);
		return relation.getIntension().get(0).get("ORDER_SQ").toString();
	}
	
	/**
	 * 특정 기간 사이의 시작 커스토머 번호 
	 * @param _startDate
	 * @param _endDate
	 * @return String 대기 번호
	 * @throws SQLException
	 * @throws Exception
	 */
	private static String getStartNumberAtPeriod(String _startDate, String _endDate) throws SQLException, Exception {
		String SQL = "SELECT " +
				"CT.CUST_SQ " +
				"FROM CUSTOMERS_TB CT, ORDERS_TB OT " +
				"WHERE CT.ORDER_SQ = OT.ORDER_SQ AND " +
				"OT.ORDER_DT BETWEEN '" + _startDate + "' AND '" + _endDate + "'";
		
		relation.setSQL(SQL);
		return relation.getIntension().get(0).get("CUST_SQ").toString();
	}
}
