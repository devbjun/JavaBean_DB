package jdbc.oracle;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Vector;

import org.json.simple.JSONObject;

public class Customers {
	
	private static Relation relation = new Relation();
	private static SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
	
	/**
	 * 주문번호에 해당하는 세부 주문 내역(품명, 옵션, 단가, 옵션단가, 수량, 합계)을 반환
	 * @see VIEW(ITEM_NM, ITEM_DETAIL_NM, ITEM_QUANTITY_NO, ITEM_PRICE_NO, ITEM_DETAIL_PRICE_NO, ITEM_TOTAL_PRICE_NO)
	 * @param boolean _isReceived
	 * @return Vector<JSONObject> 세부 주문 내역 리스트
	 * @throws Exception 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @_isReceived 수령 완료 여부
	 */
	public static Vector<JSONObject> getAllDetailList(String _customerNumber, boolean _isReceived) throws SQLException, Exception  {
		
		_customerNumber = String.valueOf(Integer.parseInt(_customerNumber) + Integer.parseInt(getStartNumberAtPeriod(date.format(System.currentTimeMillis()), date.format(System.currentTimeMillis() + 86400 * 1000))) - 1);
		String SQL = "SELECT " + 
				"IT.ITEM_NM AS 품명, " + 
				"IDT.ITEM_DETAIL_NM AS 옵션, " + 
				"IT.ITEM_PRICE_NO AS 단가, " +
				"IDT.ITEM_DETAIL_PRICE_NO AS 옵션단가, " + 
				"ODT.ITEM_QUANTITY_NO AS 수량, " + 
				"((IT.ITEM_PRICE_NO+IDT.ITEM_DETAIL_PRICE_NO)*ODT.ITEM_QUANTITY_NO) AS 합계 " + 
				"FROM CUSTOMERS_TB CT, ITEMS_TB IT, ITEMS_DETAILS_TB IDT, ORDERS_TB OT, ORDERS_DETAILS_TB ODT " + 
				"WHERE CT.CUST_SQ = '" + _customerNumber + "' AND CT.ORDER_SQ = OT.ORDER_SQ AND OT.ORDER_SQ = ODT.ORDER_SQ AND OT.ORDER_ST = " + ((_isReceived) ? "1" : "0") + " AND ODT.ITEM_SQ = IT.ITEM_SQ AND IT.ITEM_DETAIL_SQ = IDT.ITEM_DETAIL_SQ";
		
		relation.setSQL(SQL);
		
		// 내포가 NULL인 경우, NULL로 채워진 테이블을만들고 값을 반환한다.
		if (relation.getIntension().isEmpty()) {
			SQL = "SELECT " + 
					"MAX(IT.ITEM_NM) AS 품명, " + 
					"MAX(IDT.ITEM_DETAIL_NM) AS 옵션, " + 
					"MAX(IT.ITEM_PRICE_NO) AS 단가, " +
					"MAX(IDT.ITEM_DETAIL_PRICE_NO) AS 옵션단가, " + 
					"MAX(ODT.ITEM_QUANTITY_NO) AS 수량, " + 
					"MAX(((IT.ITEM_PRICE_NO+IDT.ITEM_DETAIL_PRICE_NO)*ODT.ITEM_QUANTITY_NO)) AS 합계 " + 
					"FROM CUSTOMERS_TB CT, ITEMS_TB IT, ITEMS_DETAILS_TB IDT, ORDERS_TB OT, ORDERS_DETAILS_TB ODT " + 
					"WHERE CT.CUST_SQ = '" + _customerNumber + "' AND CT.ORDER_SQ = OT.ORDER_SQ AND OT.ORDER_SQ = ODT.ORDER_SQ AND OT.ORDER_ST = " + ((_isReceived) ? "1" : "0") + " AND ODT.ITEM_SQ = IT.ITEM_SQ AND IT.ITEM_DETAIL_SQ = IDT.ITEM_DETAIL_SQ";
			
			relation.setSQL(SQL);
			return relation.getIntension();
		}
		
		return relation.getIntension();
	}
	
	/**
	 * 입력받은 아이템명과 주문 수량을 가지고 주문 접수
	 * @param _itemName
	 * @param _itemQuantity
	 * @return String 접수된 주문에 해당하는 대기 번호
	 * @throws NumberFormatException 
	 * @throws SQLException
	 * @throws Exception
	 */
	public static String setOrder(String[] _itemName, int[] _itemQuantity, String[] _itemDetailName) throws NumberFormatException, SQLException, Exception {
		SimpleDateFormat _date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		long _millis = System.currentTimeMillis() * 1000;
		
		// 작업 시작 전 필수 조건 검사
		if (!((_itemName.length == _itemQuantity.length) && (_itemQuantity.length == _itemDetailName.length))) { throw new Exception("인자값이 잘못되었습니다."); }
		if ((_itemName.length <= 0) || (_itemQuantity.length <= 0) || (_itemDetailName.length <= 0)) { throw new Exception("주문 수량이 잘못되었습니다."); }
		
		// 아이템 및 옵션의 고유 번호를 담을 변수 선언
		String[] _itemNumber = new String[_itemName.length];
		String[] _itemDetailNumber = new String[_itemDetailName.length];
		
		// 아이템 이름 및 옵션 이름을 고유번호로 변환
		for (int i = 0; i < _itemNumber.length; i++) {
			_itemNumber[i] = Items.getItemNumber(_itemName[i], _itemDetailName[i]);
			_itemDetailNumber[i] = Items.getItemDetailNumber(_itemDetailName[i]);
			
			if (_itemNumber[i].equals("") || _itemDetailNumber[i].equals("")) { throw new Exception("아이템명 또는 옵션값이 잘못되었습니다."); }
		}
		
		String SQL;
		
		try {
			
			// Auto Commit 해제
			relation.getJDBCManager().getConnection().setAutoCommit(false);
			
			// 주문 테이블에 정보 입력
			SQL = "INSERT INTO " +
					"ORDERS_TB(ORDER_SQ, ORDER_ST, ORDER_DT) " +
					"VALUES ('" + _millis +  "', 0, TO_DATE('" + _date.format(System.currentTimeMillis()) + "', 'YYYY-MM-DD HH24:MI:SS'))";
			
			relation.updateSQL(SQL);
			
			// 고객 테이블에 정보 입력
			SQL = "INSERT INTO " +
					"CUSTOMERS_TB(ORDER_SQ) " +
					"VALUES ('" + _millis + "')";
			
			relation.updateSQL(SQL);
			
			// 세부 주문 테이블에 세부 주문 정보 입력
			for (int i = 0; i < _itemNumber.length; i++) {
				SQL = "INSERT INTO " +
						"ORDERS_DETAILS_TB(ORDER_SQ, ITEM_SQ, ITEM_QUANTITY_NO) " +
						"VALUES ('" + _millis + "', '" + _itemNumber[i] + "', " + _itemQuantity[i] + ")";
				
				relation.updateSQL(SQL);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			// DB RollBack
			relation.getJDBCManager().rollback();
			
			// Auto Commit 설정
			relation.getJDBCManager().getConnection().setAutoCommit(true);
			throw new SQLException(e);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			// DB RollBack
			relation.getJDBCManager().rollback();
			
			// Auto Commit 설정
			relation.getJDBCManager().getConnection().setAutoCommit(true);
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
					
		// 대기 번호 반환
		relation.setSQL(SQL);
		return String.valueOf(Integer.parseInt(relation.getIntension().get(0).get("CUST_SQ").toString()) - Integer.parseInt(getStartNumberAtPeriod(date.format(System.currentTimeMillis()), date.format(System.currentTimeMillis() + 86400 * 1000))) + 1);
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
