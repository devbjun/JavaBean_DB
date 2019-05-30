package jdbc.oracle;

import java.sql.SQLException;
import java.util.Vector;

import org.json.simple.JSONObject;

public class Items {
	
	private static Relation relation = new Relation();
	
	/**
	 * 아이템(카테고리, 품명, 옵션, 단가, 옵션단가, 합계) 전체 목록을 반환
	 * @see VIEW(ITEM_CTGRY_NM, ITEM_NM, ITEM_DETAIL_NM, ITEM_PRICE_NO, ITEM_DETAIl_PRICE_NO, ITEM_TOTAL_PRICE_NO)
	 * @return Vector<JSONObject> 아이템 리스트
	 * @throws Exception 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static Vector<JSONObject> getAll() throws ClassNotFoundException, SQLException, Exception {
		String SQL = "SELECT " + 
				"ICT.ITEM_CTGRY_NM AS 카테고리, " + 
				"IT.ITEM_NM AS 품명, " + 
				"IDT.ITEM_DETAIL_NM AS 옵션, " + 
				"IT.ITEM_PRICE_NO AS 단가, " + 
				"IDT.ITEM_DETAIL_PRICE_NO AS 옵션단가, " + 
				"IT.ITEM_PRICE_NO+IDT.ITEM_DETAIL_PRICE_NO AS 합계 " + 
				"FROM ITEMS_CATEGORIES_TB ICT, ITEMS_TB IT, ITEMS_DETAILS_TB IDT " + 
				"WHERE ICT.ITEM_CTGRY_SQ = IT.ITEM_CTGRY_SQ AND IT.ITEM_DETAIL_SQ = IDT.ITEM_DETAIL_SQ";
		
		relation.setSQL(SQL);
		return relation.getIntension();
	}
	
	/**
	 * 카테고리(카테고리) 전체 목록을 반환
	 * @see ITEMS_CATEGORIES_TB(ITEM_CTGRY_NM)
	 * @return Vector<JSONObject> 카테고리 리스트
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
	 * 특정 카테고리에 속해있는 아이템(품명, 단가) 전체 목록을 반환
	 * @see VIEW(ITEM_NM, ITEM_PRICE_NO) 카테고리 아이템 리스트
	 * @param String _categoryName
	 * @return Vector<JSONObject>
	 * @throws Exception 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static Vector<JSONObject> getCategoryItemList(String _categoryName) throws ClassNotFoundException, SQLException, Exception {
		String SQL = "SELECT "
				+ "IT.ITEM_NM AS 품명, "
				+ "IT.ITEM_PRICE_NO AS 단가 "
				+ "FROM ITEMS_CATEGORIES_TB ICT, ITEMS_TB IT "
				+ "WHERE ICT.ITEM_CTGRY_SQ = IT.ITEM_CTGRY_SQ "
				+ "AND ICT.ITEM_CTGRY_NM = '" + _categoryName + "'";

		relation.setSQL(SQL);
		return relation.getIntension();
	}
	
	/**
	 * 아이템(품명, 단가) 전체 목록을 반환
	 * @see ITEMS_TB(ITEM_NM, ITEM_PRICE_NO)
	 * @return Vector<JSONObject> 아이템 리스트
	 * @throws Exception 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static Vector<JSONObject> getItemList() throws ClassNotFoundException, SQLException, Exception  {
		String SQL = "SELECT DISTINCT ITEM_NM AS 품명, ITEM_PRICE_NO AS 단가 FROM ITEMS_TB";
		
		relation.setSQL(SQL);
		return relation.getIntension();
	}
	
	/**
	 * 품목에 해당하는 선택가능 옵션(옵션, 옵션단가)를 반환
	 * @see ITEM_DETAILS_TB(ITEM_DETAIL_NM, ITEM_DETAIL_PRICE_NO)
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
	 * 특정 물품의 옵션번호 반환
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
