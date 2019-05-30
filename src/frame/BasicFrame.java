package frame;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class BasicFrame extends JFrame {
	public BasicFrame(String _title, int _width, int _height) {
		
		setTitle(_title);
		
		setSize(_width, _height);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		setLocationRelativeTo(null);
		setResizable(false);
	}
}
