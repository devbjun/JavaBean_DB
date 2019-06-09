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
	 * 특정 기간 동안 판매된 메뉴별 수량 (카테고리, 품명, 판매수량) 뷰 릴레이션 반환
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
				"IT.ITEM_NM AS 품명, " + 
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
					"MAX(IT.ITEM_NM) AS 품명, " + 
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

	/**
	 * 전체 물품 (카테고리, 이름, 단가, 판매상태) 뷰 릴레이션 반환
	 * @see VIEW(ITEM_CTGRY_NM, ITEM_NM, SUM(ODT.ITEM_QUANTITY_NO))
	 * @param _dStart
	 * @param _dEnd
	 * @return Vector<JSONObject>
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws Exception
	 */
	public static Vector<JSONObject> getAllItemsWithStatus() throws ClassNotFoundException, SQLException, Exception {
		String SQL = "SELECT "
				+ "DISTINCT ICT.ITEM_CTGRY_NM AS 카테고리, "
				+ "IT.ITEM_NM AS 품명, "
				+ "IT.ITEM_PRICE_NO AS 단가, "
				+ "IST.ITEM_STATUS_NM AS 판매상태 "
				+ "FROM ITEMS_CATEGORIES_TB ICT, ITEMS_TB IT, ITEMS_STATUS_TB IST "
				+ "WHERE ICT.ITEM_CTGRY_SQ = IT.ITEM_CTGRY_SQ AND IT.ITEM_STATUS_SQ = IST.ITEM_STATUS_SQ "
				+ "ORDER BY ICT.ITEM_CTGRY_NM ASC, IT.ITEM_NM ASC";

		relation.setSQL(SQL);
		return relation.getIntension();
	}

	
	/**
	 * 
	 * @param _nStatus
	 * @return Integer
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws Exception
	 */
	public static int updateItemStatus(String _nItem, String _nStatus) throws ClassNotFoundException, SQLException, Exception {
		
		// 값 저장을 위한 변수 선언
		int result = 0;
		
		// _nStatus 존재 여부 검사
		String SQL = "SELECT " +
				"ITEM_STATUS_SQ " +
				"FROM ITEMS_STATUS_TB " +
				"WHERE ITEM_STATUS_NM " +
				"IN '" + _nStatus + "'";

		relation.setSQL(SQL);

		// 없을 시 오류 반환
		if (relation.getIntension().isEmpty()) {
			throw new Exception("인자값이 잘못되었습니다.");
		}
		
		// _nStatus를 SQ 값으로 대체
		_nStatus = relation.getIntension().get(0).get("ITEM_STATUS_SQ").toString();
		
		// Auto Commit 해제
		relation.getJDBCManager().getConnection().setAutoCommit(false);
				
		// 오류 처리
		try {
			
			// ITEM_STATUS_SQ 업데이트
			SQL = "UPDATE " +
					"ITEMS_TB " +
					"SET " +
					"ITEM_STATUS_SQ = '" + _nStatus + "' " +
					"WHERE ITEM_NM = '" + _nItem + "'";
			
			// SQL문 실행
			result = relation.updateSQL(SQL);
			
			// 처리후 결과 확인
			if (result == 0) {
				
				// RollBack & AutoCommit 실행
				setRollBackAndAutoCommit();
				
				// 오류 발생
				throw new Exception("처리 도중 알 수 없는 오류가 발생했습니다."); 
			}
			
		// 에러 처리
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
				
		// 결과 반환
		return result;
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
