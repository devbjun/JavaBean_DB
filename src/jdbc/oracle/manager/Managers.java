package jdbc.oracle.manager;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Vector;

import org.json.simple.JSONObject;

import jdbc.oracle.Relation;

public class Managers {
	
	// 날짜 및 가격 포맷 설정
	private static SimpleDateFormat[] fDate = {
			new SimpleDateFormat("yyyy-MM-dd"),
			new SimpleDateFormat("yyyy년 MM월 dd일")
	};
	private static DecimalFormat fPrice = new DecimalFormat("###,###");
	
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
	 * 금일 전체 미수령 주문 내역(주문번호, 결제일시) 뷰 릴레이션 반환
	 * @see VIEW(CUST_SQ, ORDER_DT)
	 * @return Vector<JSONObject>
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public static Vector<JSONObject> getOrderNotReceivedAtToday() throws ClassNotFoundException, SQLException, Exception {
		String SQL = "SELECT " + 
				"CT.CUST_SQ AS 주문번호, " +
				"OT.ORDER_DT AS 결제일시 " + 
				"FROM CUSTOMERS_TB CT, ORDERS_TB OT, ORDERS_STATUS_TB OST " + 
				"WHERE CT.ORDER_SQ = OT.ORDER_SQ AND OT.ORDER_STATUS_SQ = OST.ORDER_STATUS_SQ AND OST.ORDER_STATUS_NM = '수령 대기' AND " +
				"OT.ORDER_DT BETWEEN TO_CHAR(SYSDATE, 'YYYY-MM-DD') AND TO_CHAR(SYSDATE + 1, 'YYYY-MM-DD') " +
				"ORDER BY OT.ORDER_DT DESC";
		
		relation.setSQL(SQL);
		Vector<JSONObject> intension = relation.getIntension();
		
		// 외연이 NULL인 경우, NULL로 채워진 테이블을만들고 값을 반환한다.
		if (intension.isEmpty()) {
			
			SQL = "SELECT " + 
					"MAX(CT.CUST_SQ) AS 주문번호, " +
					"MAX(OT.ORDER_DT) AS 결제일시 " + 
					"FROM CUSTOMERS_TB CT, ORDERS_TB OT, ORDERS_STATUS_TB OST " + 
					"WHERE CT.ORDER_SQ = OT.ORDER_SQ AND OT.ORDER_STATUS_SQ = OST.ORDER_STATUS_SQ AND OST.ORDER_STATUS_NM = '수령 대기' AND " +
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
	 * 주문 번호에 대한 미수령 상세 내역 (품명, 옵션, 단가, 옵션단가, 수량, 합계) 뷰 릴레이션 반환
	 * @see VIEW(ITEM_NM, ITEM_DETAIL_NM, ITEM_QUANTITY_NO, ITEM_PRICE_NO, ITEM_DETAIl_PRICE_NO, ITEM_TOTAL_PRICE_NO)
	 * @return Vector<JSONObject>
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 * @throws Exception 
	 */
	public static Vector<JSONObject> getOrderDetailNotReceivedAtNumber(String _nCustomer) throws ClassNotFoundException, SQLException, Exception {
		
		_nCustomer = String.valueOf(Integer.parseInt(_nCustomer) + Integer.parseInt(Customers.getStartCustomerNumberAtToday()) - 1);
		String SQL = "SELECT " + 
				"IT.ITEM_NM AS 품명, " + 
				"IDT.ITEM_DETAIL_NM AS 옵션, " + 
				"IT.ITEM_PRICE_NO AS 단가, " +
				"IDT.ITEM_DETAIL_PRICE_NO AS 옵션단가, " + 
				"ODT.ITEM_QUANTITY_NO AS 수량, " + 
				"((IT.ITEM_PRICE_NO+IDT.ITEM_DETAIL_PRICE_NO)*ODT.ITEM_QUANTITY_NO) AS 합계 " + 
				"FROM CUSTOMERS_TB CT, ITEMS_TB IT, ITEMS_DETAILS_TB IDT, ORDERS_TB OT, ORDERS_DETAILS_TB ODT, ORDERS_STATUS_TB OST " + 
				"WHERE CT.CUST_SQ = '" + _nCustomer + "' " +
					"AND CT.ORDER_SQ = OT.ORDER_SQ " +
					"AND OT.ORDER_SQ = ODT.ORDER_SQ " +
					"AND ODT.ORDER_STATUS_SQ = OST.ORDER_STATUS_SQ " +
					"AND OT.ORDER_STATUS_SQ IN (SELECT OST.ORDER_STATUS_SQ FROM ORDERS_STATUS_TB OST WHERE OST.ORDER_STATUS_NM = '수령 대기') " +
					"AND OST.ORDER_STATUS_NM = '수령 대기' " +
					"AND ODT.ITEM_SQ = IT.ITEM_SQ AND " +
					"IT.ITEM_DETAIL_SQ = IDT.ITEM_DETAIL_SQ AND " +
					"OT.ORDER_DT BETWEEN TO_CHAR(SYSDATE, 'YYYY-MM-DD') AND TO_CHAR(SYSDATE + 1, 'YYYY-MM-DD')";
	
		relation.setSQL(SQL);
		Vector<JSONObject> intension = relation.getIntension();
		
		// 외연이 NULL인 경우, NULL로 채워진 테이블을만들고 값을 반환한다.
		if (intension.isEmpty()) {
			
			SQL = "SELECT " + 
					"MAX(IT.ITEM_NM) AS 품명, " + 
					"MAX(IDT.ITEM_DETAIL_NM) AS 옵션, " + 
					"MAX(IT.ITEM_PRICE_NO) AS 단가, " +
					"MAX(IDT.ITEM_DETAIL_PRICE_NO) AS 옵션단가, " + 
					"MAX(ODT.ITEM_QUANTITY_NO) AS 수량, " + 
					"MAX(((IT.ITEM_PRICE_NO+IDT.ITEM_DETAIL_PRICE_NO)*ODT.ITEM_QUANTITY_NO)) AS 합계 " + 
					"FROM CUSTOMERS_TB CT, ITEMS_TB IT, ITEMS_DETAILS_TB IDT, ORDERS_TB OT, ORDERS_DETAILS_TB ODT, ORDERS_STATUS_TB OST " + 
					"WHERE CT.CUST_SQ = '" + _nCustomer + "' " +
						"AND CT.ORDER_SQ = OT.ORDER_SQ " +
						"AND OT.ORDER_SQ = ODT.ORDER_SQ " +
						"AND ODT.ORDER_STATUS_SQ = OST.ORDER_STATUS_SQ " +
						"AND OT.ORDER_STATUS_SQ IN (SELECT OST.ORDER_STATUS_SQ FROM ORDERS_STATUS_TB OST WHERE OST.ORDER_STATUS_NM = '수령 대기') " +
						"AND OST.ORDER_STATUS_NM = '수령 대기' " +
						"AND ODT.ITEM_SQ = IT.ITEM_SQ AND " +
						"IT.ITEM_DETAIL_SQ = IDT.ITEM_DETAIL_SQ AND " +
						"OT.ORDER_DT BETWEEN TO_CHAR(SYSDATE, 'YYYY-MM-DD') AND TO_CHAR(SYSDATE + 1, 'YYYY-MM-DD')";
			
			relation.setSQL(SQL);
			return relation.getIntension();
		}
		
		return intension;
	}
	
	
	/**
	 * 주문번호와 상태를 입력받아 주문상태를 입력받은 주문 상태로 업데이트하는 함수
	 * @param _nCustomer
	 * @param _nStatus
	 * @return Integer 쿼리문 처리 상태 반환 값
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 * @throws Exception 
	 */
	public static int updateOrderStatus(String _nCustomer, String _nStatus) throws ClassNotFoundException, SQLException, Exception {
		
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
			
			// ORDER_STATUS 업데이트
			SQL = "UPDATE " +
					"ORDERS_TB " +
					"SET " +
					"ORDER_STATUS_SQ = '" + _nStatus + "' " +
					"WHERE ORDER_SQ = '" + _nOrder + "'";
			
			// Auto Commit 해제
			relation.getJDBCManager().getConnection().setAutoCommit(false);
			
			// SQL문 실행
			result = relation.updateSQL(SQL);
			
			// ORDER_DETAILS_TB.ORDER_STATUS 업데이트
			SQL = "UPDATE " +
					"ORDERS_DETAILS_TB " +
					"SET " +
					"ORDER_STATUS_SQ = '" + _nStatus + "' " +
					"WHERE ORDER_SQ = '" + _nOrder + "' AND ORDER_STATUS_SQ = '0'";

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
	 * 주문번호와 상태, 주문 상세 정보를 입력받아 주문 상세 상태를 입력받은 주문 상태로 업데이트하는 함수
	 * @param _nCustomer
	 * @param _nStatus
	 * @return Integer 쿼리문 처리 상태 반환 값
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 * @throws Exception 
	 */
	public static int updateOrderDetailStatus(String _nCustomer, String[] _nItem, String[] _nDetail, String _nStatus) throws ClassNotFoundException, SQLException, Exception {
		
		int result = 0;
		String SQL;
		
		// 입력받은 정보에 대한 기본 검사
		if (_nItem.length != _nDetail.length) { throw new Exception("입력받은 인자값이 잘못되었습니다."); }
		if (_nItem.length <= 0 || _nDetail.length <= 0) { throw new Exception("입력받은 인자값이 잘못되었습니다."); } 
		
		try {
			
			// _nOrder 및 _nCustomer 정보 변환
			String _nOrder = getOrderNumber(_nCustomer);
			_nCustomer = String.valueOf(Integer.parseInt(_nCustomer) + Integer.parseInt(Customers.getStartCustomerNumberAtToday()) - 1);

			
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
			
			
			// 상세번호 목록 값을 저장할 벡터타입의 _nDetail 선언
			Vector<String> _lDetail = new Vector<String>();
			
			// 반복하며 정보를 저장한다.
			for (int i = 0; i < _nItem.length; i++) {
				
				// 주문 상세 번호 목록 조회
				SQL = "SELECT " +
						"DISTINCT ODT.ORDER_DETAIL_SQ AS 상세번호 " +
						"FROM ITEMS_TB IT, ITEMS_DETAILS_TB IDT, ORDERS_TB OT, ORDERS_DETAILS_TB ODT, ORDERS_STATUS_TB OST  " +
						"WHERE IT.ITEM_NM = '" + _nItem[i] + "' AND IDT.ITEM_DETAIL_NM = '" + _nDetail[i] + "' AND IT.ITEM_DETAIL_SQ = IDT.ITEM_DETAIL_SQ AND ODT.ITEM_SQ = IT.ITEM_SQ AND ODT.ORDER_SQ = '" + _nOrder + "' AND ODT.ORDER_STATUS_SQ = OST.ORDER_STATUS_SQ AND OST.ORDER_STATUS_NM = '수령 대기'";
				
				relation.setSQL(SQL);
				
				// 없을 시 오류 반환
				if (relation.getIntension().isEmpty()) {
					throw new Exception("인자값이 잘못되었습니다.");
				}
				
				_lDetail.add(relation.getColumn("상세번호")[0]);
				
			}
			
			// Auto Commit 해제
			relation.getJDBCManager().getConnection().setAutoCommit(false);
			
			// 반복하며 처리한다.
			for (String _oDetail : _lDetail) {
				
				// ORDER_STATUS 업데이트
				SQL = "UPDATE " +
						"ORDERS_DETAILS_TB " +
						"SET " +
						"ORDER_STATUS_SQ = '" + _nStatus + "' " +
						"WHERE ORDER_DETAIL_SQ = '" + _oDetail + "'";
				
				// SQL문 실행
				result = relation.updateSQL(SQL);
				
				// 처리후 결과 확인
				if (result == 0) {
					
					// RollBack & AutoCommit 실행
					setRollBackAndAutoCommit();
					
					// 오류 발생
					throw new Exception("처리 도중 알 수 없는 오류가 발생했습니다."); 
				}
			}
			
			// 주문 번호의 상태가 수령 대기면서 주문번호의 세부 주문 상태에 수령 대기인 주문 상세 목록을 조회한다
			SQL = "SELECT CT.CUST_SQ, OT.ORDER_SQ, ODT.ORDER_DETAIL_SQ, OT.ORDER_STATUS_SQ, ODT.ORDER_STATUS_SQ " + 
					"FROM CUSTOMERS_TB CT, ORDERS_TB OT, ORDERS_DETAILS_TB ODT " + 
					"WHERE CT.CUST_SQ = '" + _nCustomer +  "' AND CT.ORDER_SQ = OT.ORDER_SQ AND OT.ORDER_SQ = ODT.ORDER_SQ AND OT.ORDER_STATUS_SQ IN (SELECT OST.ORDER_STATUS_SQ FROM ORDERS_STATUS_TB OST WHERE OST.ORDER_STATUS_NM = '수령 대기') AND ODT.ORDER_STATUS_SQ IN (SELECT OST.ORDER_STATUS_SQ FROM ORDERS_STATUS_TB OST WHERE OST.ORDER_STATUS_NM = '수령 대기')";
			
			relation.setSQL(SQL);
			
			// 주문 번호의 상태가 수령 대기면서 주문번호의 세부 주문 상태에 수령 대기가 존재하는 경우 함수를 종료하고 1을 반환한다.
			if(!relation.getIntension().isEmpty()) { 
				
				// Commit 실행
				relation.getJDBCManager().commit();
				
				// Auto Commit 설정
				relation.getJDBCManager().getConnection().setAutoCommit(true);
				return 1; 
			}
			
			
			/*
			 * 주문 번호 상태가 수령 대기인 상태에서 만일 세부 수령 상태가 모두 주문 취소인지를 검사한다.
			 */
			
			// 주문 번호에 대한 상세 주문 내역 전체 개수를 받아온다
			SQL = "SELECT COUNT(*) 상세주문총개수 " +
					"FROM CUSTOMERS_TB CT, ORDERS_TB OT, ORDERS_DETAILS_TB ODT " +
					"WHERE CT.CUST_SQ = '" + _nCustomer + "' AND CT.ORDER_SQ = OT.ORDER_SQ AND OT.ORDER_SQ = ODT.ORDER_SQ";
			
			relation.setSQL(SQL);
			int tDetail = Integer.parseInt(relation.getColumn("상세주문총개수")[0]);
			
			// 주문 번호에 대한 상세 주문 내역의 주문 상태가 '주문 취소'인 주문의 전체 개수를 받아온다.
			SQL = "SELECT COUNT(*) 주문취소총개수 " +
					"FROM CUSTOMERS_TB CT, ORDERS_TB OT, ORDERS_DETAILS_TB ODT " +
					"WHERE CT.CUST_SQ = '" + _nCustomer + "' AND CT.ORDER_SQ = OT.ORDER_SQ AND OT.ORDER_SQ = ODT.ORDER_SQ " +
					"AND ODT.ORDER_STATUS_SQ IN (SELECT OST.ORDER_STATUS_SQ FROM ORDERS_STATUS_TB OST WHERE OST.ORDER_STATUS_NM = '주문 취소')";
			
			relation.setSQL(SQL);
			int cDetail = Integer.parseInt(relation.getColumn("주문취소총개수")[0]);
			
			// 두 개수가 동일한 경우에 한해 주문 번호의 전체 상태를 주문 취소로 설정하도록 한다.
			_nStatus = (tDetail == cDetail) ? "주문 취소" : "수령 완료";
			SQL = "UPDATE ORDERS_TB " +
					"SET ORDERS_TB.ORDER_STATUS_SQ = (" +
						"SELECT OST.ORDER_STATUS_SQ " +
						"FROM ORDERS_STATUS_TB OST " +
						"WHERE OST.ORDER_STATUS_NM = '" + _nStatus + "') " +
					"WHERE ORDERS_TB.ORDER_SQ = (" +
						"SELECT CT.ORDER_SQ " +
						"FROM CUSTOMERS_TB CT " +
						"WHERE CT.CUST_SQ = '" + _nCustomer + "')";
			
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
				"WHERE CT.ORDER_SQ = OT.ORDER_SQ AND OT.ORDER_SQ = ODT.ORDER_SQ AND ODT.ITEM_SQ = IT.ITEM_SQ AND IT.ITEM_DETAIL_SQ = IDT.ITEM_DETAIL_SQ AND ODT.ORDER_STATUS_SQ = OST.ORDER_STATUS_SQ AND OST.ORDER_STATUS_NM NOT IN '수령 대기' AND " +
				"OT.ORDER_DT BETWEEN '" + _dStart + "' AND '" + _dEnd + "' " + 
				"ORDER BY OT.ORDER_DT DESC";
			
		relation.setSQL(SQL);
		Vector<JSONObject> intension = relation.getIntension();
			
		// 외연이 NULL인 경우, NULL로 채워진 테이블을만들고 값을 반환한다.
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
					"WHERE CT.ORDER_SQ = OT.ORDER_SQ AND OT.ORDER_SQ = ODT.ORDER_SQ AND ODT.ITEM_SQ = IT.ITEM_SQ AND IT.ITEM_DETAIL_SQ = IDT.ITEM_DETAIL_SQ AND ODT.ORDER_STATUS_SQ = OST.ORDER_STATUS_SQ AND OST.ORDER_STATUS_NM NOT IN '수령 대기' AND " +
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
				"WHERE CT.ORDER_SQ = OT.ORDER_SQ AND OT.ORDER_SQ = ODT.ORDER_SQ AND ODT.ITEM_SQ = IT.ITEM_SQ AND IT.ITEM_DETAIL_SQ = IDT.ITEM_DETAIL_SQ AND ODT.ORDER_STATUS_SQ = OST.ORDER_STATUS_SQ AND " +
				"OT.ORDER_SQ = '" + _nOrder + "' " +
				"ORDER BY OT.ORDER_DT DESC";
			
		relation.setSQL(SQL);
		Vector<JSONObject> intension = relation.getIntension();
			
		// 외연이 NULL인 경우, NULL로 채워진 테이블을만들고 값을 반환한다.
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
					"WHERE CT.ORDER_SQ = OT.ORDER_SQ AND OT.ORDER_SQ = ODT.ORDER_SQ AND ODT.ITEM_SQ = IT.ITEM_SQ AND IT.ITEM_DETAIL_SQ = IDT.ITEM_DETAIL_SQ AND ODT.ORDER_STATUS_SQ = OST.ORDER_STATUS_SQ AND " +
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
	public static String getStoreTotalPriceAtPeriod(String _dStart, String _dEnd) throws ClassNotFoundException, SQLException, Exception {
		String SQL = "SELECT " + 
				"SUM((IT.ITEM_PRICE_NO+IDT.ITEM_DETAIL_PRICE_NO)*ODT.ITEM_QUANTITY_NO) AS 총매출액 " + 
				"FROM ITEMS_TB IT, ITEMS_DETAILS_TB IDT, ORDERS_TB OT, ORDERS_DETAILS_TB ODT, ORDERS_STATUS_TB OST " + 
				"WHERE OT.ORDER_SQ = ODT.ORDER_SQ AND ODT.ORDER_STATUS_SQ = OST.ORDER_STATUS_SQ AND OST.ORDER_STATUS_NM = '수령 완료' AND ODT.ITEM_SQ = IT.ITEM_SQ AND IT.ITEM_DETAIL_SQ = IDT.ITEM_DETAIL_SQ AND " +
				"OT.ORDER_DT BETWEEN '" + _dStart + "' AND '" + _dEnd + "'";
		
		relation.setSQL(SQL);
		
		// 값을 받아와 값이 없는 경우 0을 반환
		if (relation.getIntension().get(0).get("총매출액") == null) { return "0"; }
		return fPrice.format(Integer.parseInt(relation.getIntension().get(0).get("총매출액").toString()));
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
				"SUM((IT.ITEM_PRICE_NO+IDT.ITEM_DETAIL_PRICE_NO)*ODT.ITEM_QUANTITY_NO) AS 매출액 " + 
				"FROM ITEMS_TB IT, ITEMS_DETAILS_TB IDT, ORDERS_TB OT, ORDERS_DETAILS_TB ODT, ORDERS_STATUS_TB OST " + 
				"WHERE OT.ORDER_SQ = ODT.ORDER_SQ AND ODT.ORDER_STATUS_SQ = OST.ORDER_STATUS_SQ AND OST.ORDER_STATUS_NM = '수령 완료' AND ODT.ITEM_SQ = IT.ITEM_SQ AND IT.ITEM_DETAIL_SQ = IDT.ITEM_DETAIL_SQ AND " +
				"OT.ORDER_DT BETWEEN '" + _dStart + "' AND '" + _dEnd + "' " +
				"GROUP BY TO_DATE(TO_CHAR(ORDER_DT, 'YYYY-MM-DD'), 'YYYY-MM-DD') " +
				"ORDER BY TO_DATE(TO_CHAR(ORDER_DT, 'YYYY-MM-DD'), 'YYYY-MM-DD') ASC";
		
		relation.setSQL(SQL);
		Vector<JSONObject> intension = relation.getIntension();
		
		// 외연이 NULL인 경우, NULL로 채워진 테이블을만들고 값을 반환한다.
		if (intension.isEmpty()) { 
			
			SQL = "SELECT " + 
					"MAX(TO_DATE(TO_CHAR(ORDER_DT, 'YYYY-MM-DD'), 'YYYY-MM-DD')) AS 날짜, " + 
					"MAX(SUM((IT.ITEM_PRICE_NO+IDT.ITEM_DETAIL_PRICE_NO)*ODT.ITEM_QUANTITY_NO)) AS 매출액 " + 
					"FROM ITEMS_TB IT, ITEMS_DETAILS_TB IDT, ORDERS_TB OT, ORDERS_DETAILS_TB ODT, ORDERS_STATUS_TB OST " + 
					"WHERE OT.ORDER_SQ = ODT.ORDER_SQ AND ODT.ORDER_STATUS_SQ = OST.ORDER_STATUS_SQ AND OST.ORDER_STATUS_NM = '수령 완료' AND ODT.ITEM_SQ = IT.ITEM_SQ AND IT.ITEM_DETAIL_SQ = IDT.ITEM_DETAIL_SQ AND " +
					"OT.ORDER_DT BETWEEN '" + _dStart + "' AND '" + _dEnd + "' " +
					"GROUP BY TO_DATE(TO_CHAR(ORDER_DT, 'YYYY-MM-DD'), 'YYYY-MM-DD') " +
					"ORDER BY TO_DATE(TO_CHAR(ORDER_DT, 'YYYY-MM-DD'), 'YYYY-MM-DD') ASC";
			
			relation.setSQL(SQL);
			return relation.getIntension();
		}
		
		// 날짜를 하루 단위로 변환한다.
		for (int i = 0; i < intension.size(); i++) {
			intension.get(i).put("날짜", fDate[1].format(fDate[0].parse(intension.get(i).get("날짜").toString().split(" ")[0])));
			intension.get(i).put("매출액", fPrice.format(Integer.parseInt(intension.get(i).get("매출액").toString())) + " ₩");
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
