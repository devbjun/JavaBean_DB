package component;

import java.awt.Component;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.json.simple.JSONObject;

@SuppressWarnings("serial")
public class MutableTable extends JTable {
	
	private DefaultTableCellRenderer celAlignCenter;
	private MutableDefaultTableModel model;
	private JScrollPane spTable;
	
	private JTable table;
	private String[] header;
	private String[][] contents;
	
	/**
	 * 릴레이션의 내포를 기반으로 테이블을 만드는 클래스
	 * @param Vector<JSONObject> _intension
	 * @param ListSelectionListener _listSelectionListener
	 * _intension 테이블로 표시할 릴레이션의 내포 정보
	 * _listSelectionListener 테이블의 행이 선택되었을 때 처리할 이벤트 처리 담당 클래스
	 */
	public MutableTable(Vector<JSONObject> _intension) throws Exception {
		
		// 테이블 컬럼 가운데 정렬을 위한 변수 선언
		celAlignCenter = new DefaultTableCellRenderer();
		celAlignCenter.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
		
		header = new String[_intension.get(0).keySet().size() - 1];
		
		// 외연을 String[]로 변환
		for (int i = 0; i < header.length; i++) {
			header[i] = ((String[]) _intension.get(0).get("order"))[i];
		}

		// 내포를 String[][]로 변환
		contents = new String[_intension.size()][];
				
		if (!_intension.isEmpty()) {
			for (JSONObject _json : _intension) {
				String[] _tuple = new String[header.length];
								
				for (int i = 0; i < _tuple.length; i++) {
					_tuple[i] = (_json.get(header[i]) == null) ? null : _json.get(header[i]).toString();
				}
								
			contents[_intension.indexOf(_json)] = _tuple;
			}
		}
				
		// 모델 설정 및 모델이 적용된 테이블 생성
		model = new MutableDefaultTableModel(contents, header);
				
		// model이 적용된 테이블 생성
		table = new JTable(model);
		 			
		// 테이블 헤더 순서 편집 불가 설정
		table.getTableHeader().setReorderingAllowed(false);
		 			
		// 셀 중앙 정렬 설정
		for (String _h : header) {
			table.getColumn(_h).setCellRenderer(celAlignCenter);
		}
		 		    
		// 테이블 열 크기 자동 조정
		resizeColumnWidth(table);
		 			
		// 스크롤이 가능하도록 처리
		spTable = new JScrollPane(
				table, 
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
		);
	}
	
	/**
	 * 이벤트 처리기를 등록하기 위한 함수
	 * @param _listSelectionListener
	 */
	public void addListSelectionListener(ListSelectionListener _listSelectionListener) {
		table.getSelectionModel().addListSelectionListener(_listSelectionListener);
	}
	
	public JScrollPane getScrollTable() {
		return spTable;
	}
	
	public String[] getHeader() {
		return header;
	}
	
	public String[][] getContents() {
		return contents;
	}
	
	/**
	 * Column 내용이 모두 보여지도록 가로 사이즈를 자동 조절해주는 함수
	 * @param table
	 */
	private void resizeColumnWidth(JTable table) {
		final TableColumnModel columnModel = table.getColumnModel(); 
		for (int column = 0; column < table.getColumnCount(); column++) {
			
			int width = 50;
			for (int row = 0; row < table.getRowCount(); row++) { 
				TableCellRenderer renderer = table.getCellRenderer(row, column); 
				Component comp = table.prepareRenderer(renderer, row, column); 
				width = Math.max(comp.getPreferredSize().width + 1 , width); 
			}
			
			columnModel.getColumn(column).setPreferredWidth(width); 
		} 
	}
}
