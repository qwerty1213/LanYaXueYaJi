package com.example.l.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class ClientActivity extends BaseActivity implements OnItemClickListener,View.OnClickListener{

	private Context mContext;
	private BluetoothAdapter mBluetoothAdapter; // Bluetooth适配器
	private BluetoothDevice device;             // 蓝牙设备
	private ListView mListView;
	private ArrayList<ChatMessage> list;
	private ClientAdapter clientAdapter;        // ListView适配器
	private Button disconnect ,fasongBtn,huoqushujuBtn,scanBtn;
	private BluetoothSocket socket;     // 客户端socket
	private ClientThread mClientThread; // 客户端运行线程
	private ReadThread mReadThread;     // 读取流线程
	private byte[] data;
	private int gaoya,diya,xinlv;
	@Override
	protected int layoutId() {
		return R.layout.activity_bluetooth;
	}

	@Override
	protected void initView() {
		// TODO Auto-generated method stub
		mContext = this;
		//获取蓝牙适配器
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		list = new ArrayList<ChatMessage>();
		clientAdapter = new ClientAdapter(mContext, list);
		mListView = (ListView) findViewById(R.id.list);
		mListView.setAdapter(clientAdapter);
		mListView.setOnItemClickListener(this);

		// 注册receiver监听
		IntentFilter filter  = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter);

		// 获取已经配对过的蓝牙设备
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				list.add(new ChatMessage(device.getName() + "\n" + device.getAddress(), true));
				clientAdapter.notifyDataSetChanged();
				mListView.setSelection(list.size() - 1);
			}
		} else {
			list.add(new ChatMessage("没有已经配对过的设备", true));
			clientAdapter.notifyDataSetChanged();
			mListView.setSelection(list.size() - 1);
		}

		disconnect = (Button) findViewById(R.id.disconnect);
		disconnect.setOnClickListener(this);
		disconnect.setEnabled(false);

		fasongBtn= (Button) findViewById(R.id.fasongBtn);
		fasongBtn.setOnClickListener(this);
		huoqushujuBtn= (Button) findViewById(R.id.huoquShujuBtn);
		huoqushujuBtn.setOnClickListener(this);
		scanBtn= (Button) findViewById(R.id.scanBtn);
		scanBtn.setOnClickListener(this);
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		if (mBluetoothAdapter != null) {
			if (!mBluetoothAdapter.isEnabled()) {
				// 发送打开蓝牙的意图，系统会弹出一个提示对话框
				Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableIntent, RESULT_FIRST_USER);
				// 设置蓝牙的可见性，最大值3600秒，默认120秒，0表示永远可见(作为客户端，可见性可以不设置，服务端必须要设置)
				Intent displayIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				displayIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
				startActivity(displayIntent);

				// 直接打开蓝牙
				mBluetoothAdapter.enable();
			}
		}
	}

	@Override
	protected void initData() {

	}

	@Override
	protected void loadData() {

	}

	@Override
	protected void initListener() {

	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		scanDevice();
	}

	/**
	 * 蓝牙设备扫描过程中(mBluetoothAdapter.startDiscovery())会发出的消息
	 * ACTION_FOUND 扫描到远程设备
	 * ACTION_DISCOVERY_FINISHED 扫描结束
	 */
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                // Get the BluetoothDevice object from the Intent
            	// 通过EXTRA_DEVICE附加域来得到一个BluetoothDevice设备
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // If it's already paired, skip it, because it's been listed already
                // 如果这个设备是不曾配对过的，添加到list列表
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                	list.add(new ChatMessage(device.getName() + "\n" + device.getAddress(), false));
                	clientAdapter.notifyDataSetChanged();
            		mListView.setSelection(list.size() - 1);
                }
            // When discovery is finished, change the Activity title
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                setProgressBarIndeterminateVisibility(false);
                if (mListView.getCount() == 0)
                {
                	list.add(new ChatMessage("没有发现蓝牙设备", false));
                	clientAdapter.notifyDataSetChanged();
            		mListView.setSelection(list.size() - 1);
                }
            }
        }
    };
  

    // Handler更新UI
    private Handler LinkDetectedHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

        	if(msg.what==1) {
        		data= (byte[]) msg.obj;
				if(data.length==13){
					if (data[5]==0) {
						gaoya = data[6];
						diya = data[8];
						xinlv = data[10];

						Toast.makeText(mContext, "高压"+gaoya+"-------------"+"低压"+diya+"------------"+"心率"+xinlv, Toast.LENGTH_SHORT).show();
					}
				}else {

					Toast.makeText(mContext, "开始测量", Toast.LENGTH_SHORT).show();
				}
        	}
        	else
        	{
        		list.add(new ChatMessage(msg.obj.toString(), false));
        	}
			clientAdapter.notifyDataSetChanged();
			mListView.setSelection(list.size() - 1);
        }

    };


	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.disconnect:
				// 关闭相关服务
				closeClient();
				BluetoothMsg.isOpen = false;
				BluetoothMsg.serviceOrCilent = BluetoothMsg.ServerOrCilent.NONE;
				Toast.makeText(mContext, "连接已断开", Toast.LENGTH_SHORT).show();
				break;
			case R.id.fasongBtn:
				sendMessageHandler(BlueToothContion.START_MEASURE);
				break;
			case R.id.scanBtn:
				// 扫描
				scanDevice();
				break;
			case R.id.huoquShujuBtn:
				sendMessageHandler(BlueToothContion.GET_REUSLT);

				break;
		}

	}
	// 发送数据
	private void sendMessageHandler(byte[] bytes) {
		if (socket == null) {
			Toast.makeText(ClientActivity.this, "没有可用的连接", Toast.LENGTH_SHORT).show();
			return;
		}
		try {
			OutputStream os = socket.getOutputStream();
			os.write(bytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		list.add(new ChatMessage(bytes.toString(), false));
		clientAdapter.notifyDataSetChanged();
		mListView.setSelection(list.size() - 1);
	}
	// 开启客户端连接服务端
    private class ClientThread extends Thread {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (device != null) {
				try {
					socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
					// 连接
					Message msg = new Message();
					msg.obj = "请稍候，正在连接服务器: "+ BluetoothMsg.BlueToothAddress;
					msg.what = 0;
					LinkDetectedHandler.sendMessage(msg);
					// 通过socket连接服务器，这是一个阻塞过程，直到连接建立或者连接失效
					socket.connect();
					Message msg2 = new Message();
					msg2.obj = "已经连接上服务端！可以发送信息";
					msg2.what = 0;
					LinkDetectedHandler.sendMessage(msg2);

					// 更新UI界面
					Message uiMessage = new Message();
					uiMessage.what = 0;
					refreshUI.sendMessage(uiMessage);

					// 可以开启读数据线程
					mReadThread = new ReadThread();
					mReadThread.start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					// socket.connect()连接失效
					Message msg = new Message();
					msg.obj = "连接服务端异常！断开连接重新试一试。";
					msg.what = 0;
					LinkDetectedHandler.sendMessage(msg);
				}
			}
		}
    }



    // 通过socket获取InputStream流
    private class ReadThread extends Thread {
    	@Override
    	public void run() {
    		// TODO Auto-generated method stub
    		byte[] buffer = new byte[1024];
    		int bytes;
    		InputStream is = null;
    		try {
				is = socket.getInputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		while(true) {
    			try {
					if ((bytes = is.read(buffer)) > 0) {
						 data = new byte[bytes];
						for (int i = 0; i < data.length; i++) {
							data[i] = buffer[i];
						}
						Message msg = new Message();
						msg.obj =data;
						msg.what = 1;
						LinkDetectedHandler.sendMessage(msg);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					try {
						is.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					break;
				}
    		}
    	}
    }


	// 停止服务
    private void closeClient() {
    	new Thread() {
    		public void run() {
    			if (mClientThread != null) {
    				mClientThread.interrupt();
    				mClientThread = null;
				}
    			if (mReadThread != null) {
					mReadThread.interrupt();
					mReadThread = null;
				}
    			try {
					if (socket != null) {
						socket.close();
						socket = null;
					}
				} catch (IOException e) {
					// TODO: handle exception
				}
    		}
    	}.start();
    }

    // 扫描设备
    private void scanDevice() {
		// TODO Auto-generated method stub
    	if (mBluetoothAdapter.isDiscovering()) {
			mBluetoothAdapter.cancelDiscovery();
		} else {
			list.clear();
			clientAdapter.notifyDataSetChanged();
			// 每次扫描前都先判断一下是否存在已经配对过的设备
			Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
			if (pairedDevices.size() > 0) {
		            for (BluetoothDevice device : pairedDevices) {
		            	list.add(new ChatMessage(device.getName() + "\n" + device.getAddress(), true));
		            	clientAdapter.notifyDataSetChanged();
		        		mListView.setSelection(list.size() - 1);
		            }
		    } else {
		        	list.add(new ChatMessage("没有配对设备", true));
		        	clientAdapter.notifyDataSetChanged();
		    		mListView.setSelection(list.size() - 1);
		     }
	        /* 开始搜索 */
			mBluetoothAdapter.startDiscovery();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		ChatMessage item = list.get(arg2);
		String info = item.getMessage();
		Log.i("tag",info);
		String address = info.substring(info.length() - 17);
		BluetoothMsg.BlueToothAddress = address;

		// 停止扫描
		// BluetoothAdapter.startDiscovery()很耗资源，在尝试配对前必须中止它
		mBluetoothAdapter.cancelDiscovery();
		// 通过Mac地址去尝试连接一个设备
		device = mBluetoothAdapter.getRemoteDevice(BluetoothMsg.BlueToothAddress);
		mClientThread = new ClientThread();
		mClientThread.start();
		BluetoothMsg.isOpen = true;

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mBluetoothAdapter != null) {
			mBluetoothAdapter.cancelDiscovery();
			//关闭蓝牙
			mBluetoothAdapter.disable();
		}
		unregisterReceiver(mReceiver);
		closeClient();
	}


	// 当连接上服务器的时候才可以选择发送数据和断开连接
	private Handler refreshUI = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				disconnect.setEnabled(true);
			}
		}
	};
}
