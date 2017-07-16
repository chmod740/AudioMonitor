package me.hupeng.java.monitorserver.main;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.*;
import me.hupeng.java.monitorserver.Mina.MyReceiveData;
import me.hupeng.java.monitorserver.util.RecordFileUtil;
import org.apache.mina.core.session.IoSession;

import me.hupeng.java.monitorserver.Mina.MinaUtil;

import me.hupeng.java.monitorserver.Mina.MinaSimpleListener;
import me.hupeng.java.monitorserver.util.OperateImage;

public class MonitorServer {
	public static MinaUtil minaUtil;

	public static byte[]  videoDatas[] = new byte[8][];

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				JFrame frame = new AudioViewerFrame();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setVisible(true);
			}
		});
	}
}

class AudioViewerFrame extends JFrame{
	/**
	 * 固定的参数
	 * */
	private static final int DEFAULT_WIDTH = OperateImage.width * 3;
	private static final int DEFAULT_HEIGHT = OperateImage.height * 3;


	private JPanel jPanel = null;

	private JButton[] jButtons = new JButton[16];

	private RecordFileUtil[] recordFileUtils = new RecordFileUtil[16];


	/**
	 * 在线主机情况
	 * */
	private boolean[] onLineClients = new boolean[16];


	/**
	 * 开始录音的主机情况
	 * */
	private boolean[] startRecordClient = new boolean[16];

	public AudioViewerFrame(){


		for (int i = 0 ; i < onLineClients.length ; i ++){
			onLineClients[i] = false;
		}

		for (int i = 0 ; i < startRecordClient.length ; i ++){
			startRecordClient[i] = false;
		}

		setTitle("录音平台服务端");
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

		jPanel = new JPanel(new GridLayout(4,4,3,3));
		//往JPanel添加一些东西
		for (int i = 0 ; i < 16 ; i ++){
			JButton jButton = new JButton("客户端" + i + "未连接");
			jButtons[i] = jButton;
			jPanel.add(jButton);
			int finalI = i;
			jButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onClick(finalI);
				}
			});
		}

		getContentPane().add(jPanel,BorderLayout.CENTER);

		//添加一个菜单项
		JMenuBar menubar = new JMenuBar();
		setJMenuBar(menubar);
		JMenu menu = new JMenu("操作");
		menubar.add(menu);
		JMenuItem exitItem = new JMenuItem("关闭程序");
		JMenuItem allStartItem = new JMenuItem("全部开始");
		JMenuItem allStopItem = new JMenuItem("全部结束");

		menu.add(allStartItem);
		menu.add(allStopItem);
		menu.add(exitItem);

		//设置退出的监听器
		exitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});

		allStartItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 0 ; i < 16 ; i ++){


					if (onLineClients[i]){
						int finalI = i;
						new Thread(new Runnable() {
							@Override
							public void run() {
								if (startRecordClient[finalI] == false){
									//当前的状态是 非录音状态 点击后  开始 录音
									jButtons[finalI].setText("点击结束录音");
									if (recordFileUtils[finalI] == null){
										recordFileUtils[finalI] =  new RecordFileUtil(finalI);
									}
									startRecordClient[finalI] = true;
								}else {

								}
							}
						}).start();

					}

				}
			}
		});

		allStopItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 0 ; i < 16 ; i ++){
					if (onLineClients[i]){
						if (startRecordClient[i] == false){

						}else {
							//当前的状态是录音状态  点击后开始录音
							int finalI = i;
							new Thread(new Runnable() {
								@Override
								public void run() {
									startRecordClient[finalI] =false;
									jButtons[finalI].setText("点击开始录音");
									recordFileUtils[finalI].stopWriteBuffer();
//									JOptionPane.showMessageDialog(null, "文件路径:" + recordFileUtils[finalI].getFileName(), "标题", JOptionPane.INFORMATION_MESSAGE);
								}
							}).start();

						}
					}

				}
			}
		});

		MonitorServer.minaUtil = MinaUtil.getInstance(new MinaSimpleListener() {

			@Override
			public void onReceive(Object obj, IoSession ioSession) {



				MyReceiveData myData = (MyReceiveData)obj;

//				for (int i = 0 ; i< myData.buffer.length ; i++){
//					System.out.print(myData.buffer[i]);
//				}
//				System.out.println();

				int clientId = MonitorServer.minaUtil.sessionClientMap.get(ioSession.getId());

				if (startRecordClient[clientId] == true){
					recordFileUtils[clientId].writeBuffer(myData.buffer);
				}



			}

			@Override
			public void onLine(int clientId) {
				jButtons[clientId].setText("客户端" + clientId + "上线了\n点击开始录音");
				onLineClients[clientId] = true;
			}



			@Override
			public void offLine(int clientId) {
				jButtons[clientId].setText("客户端" + clientId + "下线了");
				onLineClients[clientId] = false;

				if (startRecordClient[clientId]){

					recordFileUtils[clientId].stopWriteBuffer();
					JOptionPane.showMessageDialog(null , "文件路径:" + recordFileUtils[clientId].getFileName(), "标题", JOptionPane.INFORMATION_MESSAGE);

				}

			}
		}, true, null);

	}

	private void onClick(int i){
		if (onLineClients[i] == true){

			if (startRecordClient[i] == false){
				//当前的状态是 非录音状态 点击后  开始 录音
				jButtons[i].setText("点击结束录音");
				if (recordFileUtils[i] == null){
					recordFileUtils[i] =  new RecordFileUtil(i);
				}
				startRecordClient[i] = true;
			}else {
				//当前的状态是录音状态  点击后开始录音
				startRecordClient[i] =false;
				jButtons[i].setText("点击开始录音");
				recordFileUtils[i].stopWriteBuffer();
				JOptionPane.showMessageDialog(null, "文件路径:" + recordFileUtils[i].getFileName(), "标题", JOptionPane.INFORMATION_MESSAGE);
			}

		}
	}


}