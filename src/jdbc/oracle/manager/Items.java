package jdbc.oracle.manager;

import java.sql.SQLException;
import java.util.Vector;

import org.json.simple.JSONObject;

import jdbc.oracle.Relation;

public class Items {
	
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
	 * 판매 메뉴 조회 (카테고리, 이름, 옵션, 단가, 옵션단가) 뷰 릴레이션 반환
	 * @see VIEW(ITEM_CTGRY_NM, ITEM_NM, ITEM_DETAIL_NM, ITEM_PRICE_NO, ITEM_DETAIL_PRICE_NO)
	 * @return Vector<JSONObject>
	 * @throws Exception 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static Vector<JSONObject> getList() throws ClassNotFoundException, SQLException, Exception {
		String SQL = "SELECT " + 
				"ICT.ITEM_CTGRY_NM AS 카테고리, " + 
				"IT.ITEM_NM AS 이름, " + 
				"IDT.ITEM_DETAIL_NM AS 옵션, " + 
				"IT.ITEM_PRICE_NO AS 단가, " + 
				"IDT.ITEM_DETAIL_PRICE_NO AS 옵션단가 " + 
				"FROM ITEMS_CATEGORIES_TB ICT, ITEMS_TB IT, ITEMS_DETAILS_TB IDT " + 
				"WHERE ICT.ITEM_CTGRY_SQ = IT.ITEM_CTGRY_SQ AND IT.ITEM_DETAIL_SQ = IDT.ITEM_DETAIL_SQ";
		
		relation.setSQL(SQL);
		return relation.getIntension();
	}
	
	
	/**
	 * 특정 기간 동안의 판매된 메뉴별 수량 (카테고리, 이름, 판매수량) 뷰 릴레이션 반환
	 * @see VIEW(ITEM_CTGRY_NM, ITEM_NM, SUM(ODT.ITEM_QUANTITY_NO))
	 * @param _dStart
	 * @param _dEnd
	 * @return Vector<JSONObject>
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws Exception
	 */
	public static Vector<JSONObject> getOrderedQuantityListAtPeriod(String _dStart, String _dEnd) throws ClassNotFoundException, SQLException, Exception {
		String SQL = "SELECT " +
				"ICT.ITEM_CTGRY_NM AS 카테고리, " + 
				"IT.ITEM_NM AS 이름, " + 
				"SUM(ODT.ITEM_QUANTITY_NO) AS 판매수량 " +
				"FROM ITEMS_CATEGORIES_TB ICT, ITEMS_TB IT, ORDERS_TB OT, ORDERS_DETAILS_TB ODT, ORDERS_STATUS_TB OST " +
				"WHERE ICT.ITEM_CTGRY_SQ = IT.ITEM_CTGRY_SQ AND IT.ITEM_SQ = ODT.ITEM_SQ AND OT.ORDER_SQ = ODT.ORDER_SQ AND OT.ORDER_STATUS_SQ = OST.ORDER_STATUS_SQ AND OST.ORDER_STATUS_NM = '수령 완료' AND " + 
				"OT.ORDER_DT BETWEEN '" + _dStart + "' AND '" + _dEnd + "' " +
				"GROUP BY ICT.ITEM_CTGRY_NM, IT.ITEM_NM " +
				"ORDER BY 판매수량 DESC";
		
		relation.setSQL(SQL);
		Vector<JSONObject> intension = relation.getIntension();
		
		// 내포가 NULL인 경우, NULL로 채워진 테이블을만들고 값을 반환한다.
		if (intension.isEmpty()) { 
			
			SQL = "SELECT " +
					"MAX(ICT.ITEM_CTGRY_NM) AS 카테고리, " + 
					"MAX(IT.ITEM_NM) AS 이름, " + 
					"MAX(SUM(ODT.ITEM_QUANTITY_NO)) AS 판매수량 " +
					"FROM ITEMS_CATEGORIES_TB ICT, ITEMS_TB IT, ORDERS_TB OT, ORDERS_DETAILS_TB ODT, ORDERS_STATUS_TB OST " +
					"WHERE ICT.ITEM_CTGRY_SQ = IT.ITEM_CTGRY_SQ AND IT.ITEM_SQ = ODT.ITEM_SQ AND OT.ORDER_SQ = ODT.ORDER_SQ AND OT.ORDER_STATUS_SQ = OST.ORDER_STATUS_SQ AND OST.ORDER_STATUS_NM = '수령 완료' AND " + 
					"OT.ORDER_DT BETWEEN '" + _dStart + "' AND '" + _dEnd + "' " +
					"GROUP BY ICT.ITEM_CTGRY_NM, IT.ITEM_NM " +
					"ORDER BY 판매수량 DESC";
			
			relation.setSQL(SQL);
			return relation.getIntension();
		}
		
		return intension;
	}
}
