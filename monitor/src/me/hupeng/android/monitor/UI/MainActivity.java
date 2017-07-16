package me.hupeng.android.monitor.UI;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.*;

import android.util.Log;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;
import me.hupeng.android.monitor.Listener.AudioBufferListener;
import me.hupeng.android.monitor.Listener.MinaSimpleListener;
import me.hupeng.android.monitor.Mina.MinaUtil;
import me.hupeng.android.monitor.Mina.MySendData;
import me.hupeng.android.monitor.R;
import me.hupeng.android.monitor.Util.AudioRecorder;
import me.hupeng.android.monitor.Util.SharedPreferencesUtil;
import me.hupeng.android.monitor.Util.ToastUtil;
import org.apache.mina.core.session.IoSession;

import java.util.Timer;
import java.util.TimerTask;


/**
 * 主功能界面
 * @author HUPENG
 */
public class MainActivity extends Activity implements AudioBufferListener,MinaSimpleListener {
    /**
     * 对Mina库进行的一个封装
     * */
    private MinaUtil minaUtil = null;

    /**
     * 录音开关
     * */
    private boolean audioFlag = false;


    /**
     * 一般控件关联变量
     * */
    private TextView tvSelfIp,tvServerIp;
    private TextView tvTime;
    private TextView tvStatus;

    /**
     * 录音机对象
     * */
    private AudioRecorder audioRecorder;

    private int clientId;

    @Override
    protected void onResume() {
        tvServerIp .setText(getServerIp());
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        init();
    }

    /**
     * 初始化变量
     * */
    private void init(){
        clientId = SharedPreferencesUtil.readInt(MainActivity.this, "client_id");
        tvSelfIp = (TextView) findViewById(R.id.tv_self_ip);
        tvServerIp = (TextView) findViewById(R.id.tv_server_ip);
        tvStatus = (TextView) findViewById(R.id.tv_status);
        tvTime = (TextView) findViewById(R.id.tv_time);


        //设置客户端的IP地址
        try{
            String ipAddress = getClientIp();
            if (ipAddress.equals("0.0.0.0")){
                ToastUtil.toast(MainActivity.this, "请先连接WIFI");
            }
            tvSelfIp.setText(ipAddress);
        }catch (Exception e){
            Log.i("MainActivity", e.getMessage());
        }
        //设置服务器端的IP地址
        tvServerIp.setText(getServerIp());

        //新开一个线程，在这个线程中用socket直接进行数据的收发，此处采用了TCP/IP的协议保证数据收发的完整性
        try{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    minaUtil = MinaUtil.getInstance(MainActivity.this,false,getServerIp());
                }
            }).start();
        }catch (Exception e){

        }

        //设置定时器
        timer.schedule(task, 1000, 1000); // 1s后执行task,经过1s再次执行

        //构造一个录音机对象
        audioRecorder = new AudioRecorder();
        audioRecorder.setBufferListener(this);
        audioRecorder.startRecord();
    }

    @Override
    protected void onDestroy() {
        audioRecorder.stopRecord();
        super.onDestroy();
    }

    /**
     * 得到本机IP地址
     * */
    private String getClientIp() {
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        String maxText = info.getMacAddress();
        String ipText = intToIp(info.getIpAddress());

        String status = "";
        if (wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLED)
        {
            status = "WIFI_STATE_ENABLED";
        }
        //获取到的各种WIFI信息
        String ssid = info.getSSID();
        int networkID = info.getNetworkId();
        int speed = info.getLinkSpeed();
        return ipText;
    }

    /**
     * 将获取到的Integer类型的IP地址转换成String类型的IP地址
     * */
    private String intToIp(int ip) {
        return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 24) & 0xFF);
    }

    /**
     * 创建菜单选项
     * */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(1, Menu.FIRST, Menu.FIRST, "配置");
        menu.add(1, Menu.FIRST+1, Menu.FIRST+1, "退出");
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 添加响菜单的响应事件
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int item_id = item.getItemId();
        switch (item_id){
            case Menu.FIRST:
                Intent intent = new Intent(MainActivity.this, ConfigActivity.class);
                MainActivity.this.startActivity(intent);
                break;
            case Menu.FIRST +1:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 再按一次退出功能关联变量
     * */
    private long exitTime = 0;

    /**
     * 监听返回键，实现再按一次退出功能
     * */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            if((System.currentTimeMillis()-exitTime) > 2000){
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 得到服务器端的IP地址
     * */
    private String getServerIp(){
        String tempServerIp = SharedPreferencesUtil.readString(MainActivity.this, "server");
        return (tempServerIp==null || tempServerIp.equals("")) ? "183.175.12.160" : tempServerIp;
    }


    /**
     * 计时器对象
     * */
    Timer timer = new Timer();

    /**
     * 定时任务的对象
     * */
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            // 需要做的事:发送消息
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
        }
    };

    /**
     * 延迟1秒进行消息发送，以实现自动走秒的功能
     * */
    private int timeCount = 1;
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                if (audioFlag){
                    tvTime.setText(getFormatTime(timeCount++));
                }
            }
            super.handleMessage(msg);
        }
    };

    /**
     * 格式化时间串
     * */
    private String getFormatTime(int t){
        int a = t % 60;
        int b = t / 60;
        return (b < 10 ? "0" + b : b) + ":" + (a < 10 ? "0" + a : a );
    }

    /**
     * 得到录音机的回调
     * */
    @Override
    public void getValue(short[] buffer) {
        //通过socket进行数据的发送,频率在每秒20次,是不是太快了,需要等待后期测试~



        if (audioFlag){
            MySendData mySendData = new MySendData();
            mySendData.clientId = this.clientId;
            mySendData.buffer = buffer;
            minaUtil.send(mySendData);

//            for (int i = 0 ; i< buffer.length ; i++){
//                System.out.print(buffer[i]);
//            }
//            System.out.println();
        }
    }

    @Override
    public void onReceive(Object obj, IoSession ioSession) {

    }

    /**
     * Mina Socket打开完成
     * 此时发送一条消息给服务器,告诉服务器某一个clientId上线了
     * */
    @Override
    public void opened() {
        tvStatus.setText("连接状态:已经连接至服务器");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    MySendData mySendData = new MySendData();
                    mySendData.clientId = MainActivity.this.clientId;
                    mySendData.buffer = new short[0];
                    minaUtil.send(mySendData);

                    //暂时采用这个方案
                    audioFlag = true;

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Mina Socket关闭成功
     * */
    @Override
    public void closed() {
        tvStatus.setText("连接状态:未连接至服务器");
    }

}
