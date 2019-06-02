import jdbc.oracle.manager.Managers;

public class Main {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// 이곳은 DB 테스트를 위한 메인 클래스 입니다.
		// 테스트 하실 작업을 이곳에 입력해주세요.
		
		try {
			System.out.println(Managers.getOrderNotReceivedAtToday());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
