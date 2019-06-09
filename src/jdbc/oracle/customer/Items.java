package jdbc.oracle.customer;

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
	 * 카테고리(이름) 뷰 릴레이션 반환
	 * @see VIEW(ITEM_CTGRY_NM)
	 * @return Vector<JSONObject>
	 * @throws Exception 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static Vector<JSONObject> getCategoryList() throws ClassNotFoundException, SQLException, Exception {
		String SQL = "SELECT ITEM_CTGRY_NM AS 카테고리 FROM ITEMS_CATEGORIES_TB";
		
		relation.setSQL(SQL);
		return relation.getIntension();
	}
	
	/**
	 * 특정 카테고리에 속해있는 아이템(이름, 단가) 뷰 릴레이션 반환
	 * @see VIEW(ITEM_NM, ITEM_PRICE_NO)
	 * @param String _categoryName
	 * @return Vector<JSONObject>
	 * @throws Exception 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static Vector<JSONObject> getCategoryItemList(String _categoryName) throws ClassNotFoundException, SQLException, Exception {
		String SQL = "SELECT "
				+ "DISTINCT IT.ITEM_NM AS 품명, "
				+ "IT.ITEM_PRICE_NO AS 단가, "
				+ "IT.ITEM_STATUS_SQ AS 상태 "
				+ "FROM ITEMS_CATEGORIES_TB ICT, ITEMS_TB IT "
				+ "WHERE ICT.ITEM_CTGRY_SQ = IT.ITEM_CTGRY_SQ "
				+ "AND ICT.ITEM_CTGRY_NM = '" + _categoryName + "' "
				+ "AND IT.ITEM_STATUS_SQ IN ("
										+ "SELECT IST.ITEM_STATUS_SQ "
										+ "FROM ITEMS_STATUS_TB IST "
										+ "WHERE IST.ITEM_STATUS_NM IN ('판매 가능', '일시 품절'))";
				
		relation.setSQL(SQL);
		return relation.getIntension();
	}
	
	/**
	 * 특정 물품에 해당하는 선택가능 옵션(이름, 단가) 뷰 릴레이션 반환
	 * @see VIEW(ITEM_DETAIL_NM, ITEM_DETAIL_PRICE_NO)
	 * @param String _itemName
	 * @return Vector<JSONObject> 물품 옵션 리스트
	 * @throws Exception 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static Vector<JSONObject> getItemDetailList(String _itemName) throws ClassNotFoundException, SQLException, Exception  {
		String SQL = "SELECT "
				+ "DISTINCT IDT.ITEM_DETAIL_NM AS 옵션, "
				+ "IDT.ITEM_DETAIL_PRICE_NO AS 옵션단가 "
				+ "FROM ITEMS_TB IT, ITEMS_DETAILS_TB IDT "
				+ "WHERE IT.ITEM_DETAIL_SQ = IDT.ITEM_DETAIL_SQ "
				+ "AND IT.ITEM_NM = '" + _itemName + "'";
		
		relation.setSQL(SQL);
		return relation.getIntension();
	}
	
	/**
	 * 물건의 품번을 반환
	 * @param String _itemName
	 * @return String 품번
	 * @throws SQLException
	 * @throws Exception
	 */
	public static String getItemNumber(String _itemName, String _itemDetailName) throws SQLException, Exception {
		String SQL = "SELECT "
				+ "IT.ITEM_SQ "
				+ "FROM ITEMS_TB IT "
				+ "WHERE IT.ITEM_NM = '" + _itemName + "' AND IT.ITEM_DETAIL_SQ = '" + getItemDetailNumber(_itemDetailName) + "'";
		
		relation.setSQL(SQL);
		return relation.getIntension().get(0).get("ITEM_SQ").toString();
	}
	
	/**
	 * 특정 물품의 옵션 번호 반환
	 * @param String _itemDetailName
	 * @return String 아이템 옵션의 고유 물품 번호
	 * @throws SQLException
	 * @throws Exception
	 */
	public static String getItemDetailNumber(String _itemDetailName) throws SQLException, Exception {
		String SQL = "SELECT "
				+ "IDT.ITEM_DETAIL_SQ "
				+ "FROM ITEMS_DETAILS_TB IDT "
				+ "WHERE IDT.ITEM_DETAIL_NM = '" + _itemDetailName + "' ";
		
		relation.setSQL(SQL);
		return relation.getIntension().get(0).get("ITEM_DETAIL_SQ").toString();
	}
}
