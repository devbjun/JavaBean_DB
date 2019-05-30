import javax.swing.DefaultListSelectionModel;
import javax.swing.JScrollPane;

import component.MutableTable;
import frame.BasicFrame;
import jdbc.oracle.Customers;
import jdbc.oracle.Items;

public class Main {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// 이곳에 로그인 프레임을 선언해주세요.
		// 나머지 프레임 및 기타 작업들은 패키지에 맞게 클래스 선언해서 사용해주세요.
		// 이곳은 프로그램 실행을 위한 메인함수만 작성하도록 합니다.
		
		// 이하는 예제 사용법입니다.
		BasicFrame fBasic = new BasicFrame("JCP v1.0.0", 500, 500);
		try {
			
			// 이하는 주문 예제입니다.
			/*String[] itemName = {
					"카페 라떼",
					"아메리카노"
			};
			
			int[] itemQuantity = {
					5,
					3
			};
			
			String[] itemDetailName = {
					"HOT & TALL",
					"HOT & VENTI"
			};
			
			System.out.println(Customers.setOrder(itemName, itemQuantity, itemDetailName));*/
			
			// 동적 테이블 선언
			MutableTable tableMutable = new MutableTable(Customers.getAllDetailList("20", false));
			
			// 동적 테이블 선언 및 람타식 형태의 이벤트 처리기 등록
			tableMutable.addListSelectionListener((e) -> {
				DefaultListSelectionModel dlsm = (DefaultListSelectionModel) e.getSource();
				
				// TODO Auto-generated method stub
				if(!e.getValueIsAdjusting()) {
					
					// 선택된 행 전체 출력
					for (int i = 0; i < tableMutable.getHeader().length; i++) {
						System.out.print(((i == 0) ? "" : ", ") + tableMutable.getHeader()[i] + ": " + tableMutable.getContents()[dlsm.getAnchorSelectionIndex()][i]);
					}
					System.out.println();
				}
			});
			
			// 프레임에 동적 테이블 추가
			JScrollPane spTable = tableMutable.getScrollTable();
			fBasic.add(spTable);
			
			// 프레임 표시
			fBasic.setVisible(true);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
