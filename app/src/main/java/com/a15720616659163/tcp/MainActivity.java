package com.a15720616659163.tcp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.Enumeration;

import android.Manifest;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.Toast;

import com.a15720616659163.tcp.R;

public class MainActivity extends AppCompatActivity implements OnClickListener {
    private TabHost tabHost;
    private EditText edtIP;
    private EditText edtPort;
    EditText edtSend;
    private EditText edtReceiver;

    private Button btnConn;
    private Button btnSend;
    private Button switch1;
    private Button switch2;
    private Button switch3;
    private Button switch4;
    private Button switch5;
    private Button switch6;
    private Button switch7;
    private Button switch8;

    private CheckBox checkBoxTimer;

    private String tag = "MainActivity";
    String bb;
    String cc;
    String ip;
    String Gip;
    String mtype;
    String mtyb;
//    String numer;
    String numer1;
    String imei;//手机imei号

    InputStream in;
    PrintWriter printWriter = null;
    BufferedReader reader;

    Socket mSocket = null;
    public boolean isConnected = false;

    private MyHandler myHandler;

    Thread receiverThread;

    CheckBoxListener listener;

    private class MyReceiverRunnable implements Runnable {

        public void run() {

            while (true) {

                Log.i(tag, "---->>client receive....");
                if (isConnected) {
                    if (mSocket != null && mSocket.isConnected()) {

                        String result = readFromInputStream(in);

                        try {
                            if (!result.equals("")) {

                                Message msg = new Message();
                                msg.what = 1;
                                Bundle data = new Bundle();
                                data.putString("msg", result);
                                msg.setData(data);
                                myHandler.sendMessage(msg);
                            }

                        } catch (Exception e) {
                            Log.e(tag, "--->>read failure!" + e.toString());
                        }
                    }
                }
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            receiverData(msg.what);
            if (msg.what == 1) {
                String result = msg.getData().get("msg").toString();
                edtReceiver.append(result);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button Tip=(Button)findViewById(R.id.tip);
        init();

        Tip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog= new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("Phone Information & Tips:");
                dialog.setMessage("提示：此应用在点击“连接”后(从最后一次操作开始计时)若五分钟之内没有进行任何命令发送或者数据传输操作，则系统将会自动断开连接，此时请先“断开”重新点击“连接”！\n\n"+"AndroidID:["+bb+"]\n"+"LocalMAC:["+cc+"]\n"+"WIFI网络IP:["+ip+"]\n"+"GPRS网络IP:["+Gip+"]\n"+"IMEI/MEID号:["+imei+"]\n"+"手机型号：["+mtype+"]\n"+"手机品牌：["+mtyb+"]\n"+"设备ID：["+numer1+"]\n"+"\nDesigned by: Zhang YT.\n"+"Email: zyt510998645@gmail.com\n");
                dialog.setCancelable(false);

                dialog.setPositiveButton("OK!  GOT IT", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog,int which){
                        Log.d(tag, which + "  ok");
                    }
                });
                dialog.show();
            }
        });

        getInfo();
        bb=getAndroidId(MainActivity.this);
        Log.e(tag,"AndroidID:"+bb);
        cc=getLocalMac(MainActivity.this);
        Log.e(tag,"LocalMAC:"+cc);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(tag,"denied");
        }
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        ip = intToIp(ipAddress);
        Log.e(tag,"WIFI网络IP:"+ip);
        Gip=getLocalIpAddress();
        Log.e(tag,"GPRS网络IP:"+Gip);

        Switch sc1=(Switch)findViewById(R.id.switch1);
        sc1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    Toast.makeText(getApplicationContext(),"打开控制开关-1",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getApplicationContext(),"关闭控制开关-1",Toast.LENGTH_SHORT).show();
                }
            }
        });
        Switch sc2=(Switch)findViewById(R.id.switch2);
        sc2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    Toast.makeText(getApplicationContext(),"打开控制开关-2",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getApplicationContext(),"关闭控制开关-2",Toast.LENGTH_SHORT).show();
                }
            }
        });
        Switch sc3=(Switch)findViewById(R.id.switch3);
        sc3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    Toast.makeText(getApplicationContext(),"打开控制开关-3",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getApplicationContext(),"关闭控制开关-3",Toast.LENGTH_SHORT).show();
                }
            }
        });
        Switch sc4=(Switch)findViewById(R.id.switch4);
        sc4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    Toast.makeText(getApplicationContext(),"打开控制开关-4",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getApplicationContext(),"关闭控制开关-4",Toast.LENGTH_SHORT).show();
                }
            }
        });
        Switch sc5=(Switch)findViewById(R.id.switch5);
        sc5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    Toast.makeText(getApplicationContext(),"打开控制开关-5",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getApplicationContext(),"关闭控制开关-5",Toast.LENGTH_SHORT).show();
                }
            }
        });
        Switch sc6=(Switch)findViewById(R.id.switch6);
        sc6.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    Toast.makeText(getApplicationContext(),"打开控制开关-6",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getApplicationContext(),"关闭控制开关-6",Toast.LENGTH_SHORT).show();
                }
            }
        });
        Switch sc7=(Switch)findViewById(R.id.switch7);
        sc7.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    Toast.makeText(getApplicationContext(),"打开控制开关-7",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getApplicationContext(),"关闭控制开关-7",Toast.LENGTH_SHORT).show();
                }
            }
        });
        Switch sc8=(Switch)findViewById(R.id.switch8);
        sc8.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    Toast.makeText(getApplicationContext(),"打开OLED开关",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getApplicationContext(),"关闭OLED开关",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void init() {

        edtIP = (EditText) this.findViewById(R.id.id_edt_inputIP);
        edtPort = (EditText) this.findViewById(R.id.id_edt_inputport);
        edtSend = (EditText) this.findViewById(R.id.id_edt_sendArea);
        edtReceiver = (EditText) findViewById(R.id.id_edt_jieshou);

        checkBoxTimer = (CheckBox) this.findViewById(R.id.id_checkBox_timer);
        listener = new CheckBoxListener(this);
        checkBoxTimer.setOnCheckedChangeListener(listener);

        btnSend = (Button) findViewById(R.id.id_btn_send);
        btnSend.setOnClickListener(this);
        btnConn = (Button) findViewById(R.id.id_btn_connClose);
        btnConn.setOnClickListener(this);
        switch1 = (Button) findViewById(R.id.switch1);
        switch1.setOnClickListener(this);
        switch2 = (Button) findViewById(R.id.switch2);
        switch2.setOnClickListener(this);
        switch3 = (Button) findViewById(R.id.switch3);
        switch3.setOnClickListener(this);
        switch4 = (Button) findViewById(R.id.switch4);
        switch4.setOnClickListener(this);
        switch5 = (Button) findViewById(R.id.switch5);
        switch5.setOnClickListener(this);
        switch6 = (Button) findViewById(R.id.switch6);
        switch6.setOnClickListener(this);
        switch7 = (Button) findViewById(R.id.switch7);
        switch7.setOnClickListener(this);
        switch8 = (Button) findViewById(R.id.switch8);
        switch8.setOnClickListener(this);

        myHandler = new MyHandler();
    }

    /******************************************************************************/
    public String readFromInputStream(InputStream in) {
        int count = 0;
        byte[] inDatas = null;
        try {
            while (count == 0) {
                count = in.available();
            }
            inDatas = new byte[count];
            in.read(inDatas);
            return new String(inDatas, "gb2312");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /******************************************************************************/
    public boolean flag1=true;
    public boolean flag2=true;
    public boolean flag3=true;
    public boolean flag4=true;
    public boolean flag5=true;
    public boolean flag6=true;
    public boolean flag7=true;
    public boolean flag8=true;

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            // 启动2个工作线程:发送、接收。
            case R.id.id_btn_connClose:

                connectThread();
                break;
            case R.id.id_btn_send:
                sendData();
                break;
            case R.id.switch1:
                if(flag1==true){
                    sendData1("1ON");
                    flag1=false;
                }else{
                    sendData1("1OFF");
                    flag1=true;
                }
                break;
            case R.id.switch2:
                if(flag2==true){
                    sendData1("2ON");
                    flag2=false;
                }else{
                    sendData1("2OFF");
                    flag2=true;
                }
                break;
            case R.id.switch3:
                if(flag3==true){
                    sendData1("3ON");
                    flag3=false;
                }else{
                    sendData1("3OFF");
                    flag3=true;
                }
                break;
            case R.id.switch4:
                if(flag4==true){
                    sendData1("4ON");
                    flag4=false;
                }else{
                    sendData1("4OFF");
                    flag4=true;
                }
                break;
            case R.id.switch5:
                if(flag5==true){
                    sendData1("5ON");
                    flag5=false;
                }else{
                    sendData1("5OFF");
                    flag5=true;
                }
                break;
            case R.id.switch6:
                if(flag6==true){
                    sendData1("6ON");
                    flag6=false;
                }else{
                    sendData1("6OFF");
                    flag6=true;
                }
                break;
            case R.id.switch7:
                if(flag7==true){
                    sendData1("7ON");
                    flag7=false;
                }else{
                    sendData1("7OFF");
                    flag7=true;
                }
                break;
            case R.id.switch8:
                if(flag8==true){
                    sendData1("OLED-ON");
                    flag8=false;
                }else{
                    sendData1("OLED-OFF");
                    flag8=true;
                }
                break;
        }
    }

    /**
     * 当连接到服务器时,可以触发接收事件.
     */
    private void receiverData(int flag) {

        if (flag == 2) {
            // mTask = new ReceiverTask();
            receiverThread = new Thread(new MyReceiverRunnable());
            receiverThread.start();

            Log.i(tag, "--->>socket 连接成功!");
            btnConn.setText("断开");

            isConnected = true;
            // mTask.execute(null);
        }

    }

    /**
     * 发送数据线程.
     */
    private void sendData() {

        // sendThread.start();
        new Thread() {
            public void run() {
                try {
                    String context = edtSend.getText().toString();

                    if (printWriter == null || context == null) {

                        if (printWriter == null) {
                            showInfo("连接失败!");
                            return;
                        }
                        if (context == null) {
                            showInfo("连接失败!");
                            return;
                        }
                    }
                    printWriter.write(context);
                    printWriter.flush();
                    Log.i(tag, "--->> client send data!");
                } catch (Exception e) {
                    Log.e(tag, "--->> send failure! " + e.toString());

                }
            }
        }.start();
    }

    public String somestr;
    private void sendData1(String text1) {

        // sendThread.start();
        somestr = text1;
        Log.d("TAG", "sendData1: " + text1);
        new Thread() {
            public void run() {
                try {
                    String context = edtSend.getText().toString();

                    if (printWriter == null || context == null) {

                        if (printWriter == null) {
                            showInfo("连接失败!");
                            return;
                        }
                        if (context == null) {
                            showInfo("连接失败!");
                            return;
                        }
                    }
                    printWriter.write(somestr);
                    printWriter.flush();
                    Log.i(tag, "--->> client send data!");
                } catch (Exception e) {
                    Log.e(tag, "--->> send failure! " + e.toString());

                }
            }
        }.start();
    }

    /**
     * 启动连接线程.
     */
    private void connectThread() {
        if (!isConnected) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    Looper.prepare();
                    Log.i(tag, "---->> connect/close server!");

                    connectServer(edtIP.getText().toString(), edtPort.getText()
                            .toString());
                }
            }).start();
        } else {
            try {
                if (mSocket != null) {
                    mSocket.close();
                    mSocket = null;
                    Log.i(tag, "--->>取消server.");
                    // receiverThread.interrupt();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            btnConn.setText("连接");
            isConnected = false;
        }
    }

    // 连接服务器.(网络调试助手的服务器端编码方式:gb2312)
    private void connectServer(String ip, String port) {
        try {
            Log.e(tag, "--->>start connect  server !" + ip + "," + port);

            mSocket = new Socket(ip, Integer.parseInt(port));
            Log.e(tag, "--->>end connect  server!");

            OutputStream outputStream = mSocket.getOutputStream();

            printWriter = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(outputStream,
                            Charset.forName("gb2312"))));
            listener.setOutStream(printWriter);
            // reader = new BufferedReader(new InputStreamReader(
            // mSocket.getInputStream()));
            in = mSocket.getInputStream();
            myHandler.sendEmptyMessage(2);

            showInfo("连接成功!");
        } catch (Exception e) {
            isConnected = false;
            showInfo("连接失败！");
            Log.e(tag, "exception:" + e.toString());
        }

    }

    private void showInfo(String msg) {
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();

    }

    public void getInfo() {
        TelephonyManager mTm = (TelephonyManager)this.getSystemService(TELEPHONY_SERVICE);
        imei = mTm.getDeviceId();
        mtype = Build.MODEL;
        mtyb = Build.BRAND;
//        numer = mTm.getLine1Number();
        numer1 = mTm.getDeviceId();
//        Log.e(tag, "手机IMEI/MEID号："+imei);
//        Log.e(tag, "手机型号："+mtype);
//        Log.e(tag, "手机品牌："+mtyb);
////        Log.e(tag, "手机号码："+numer);
//        Log.e(tag, "设备ID："+numer1);
    }
    private static String getAndroidId(Context context) {
        String androidId = Settings.Secure.getString(
                context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return androidId;
    }
    private static String getLocalMac(Context context) {
        WifiManager wifi = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }
    private String intToIp(int i) {

        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ( i >> 24 & 0xFF) ;
    }
    public String getLocalIpAddress()
    {
        try
        {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)
            {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
                {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress())
                    {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        }
        catch (SocketException ex)
        {
            Log.e("WifiPreference IpAddress", ex.toString());
        }
        return null;
    }
}
