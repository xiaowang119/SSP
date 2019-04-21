package myUtil;

import android.os.Handler;
import android.util.Log;

import static com.example.zhx.ssp.MainActivity.dataCircle;
import static com.example.zhx.ssp.MainActivity.meterData;

public class RefreshUtil extends Thread{
    private Handler mHandler;

    public RefreshUtil(Handler mHandler) {
        this.mHandler = mHandler;
    }

    public void run() {
        /*long time;
        while (true) {
            time = SystemClock.currentThreadTimeMillis();
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("time", Long.toString(time));
            msg.setData(data);
            mHandler.sendMessage(msg);
            try {
                Thread.sleep(30);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }*/

        mHandler.postDelayed(readThread,10);
    }

    //开启一个轮询读取数据环的子线程
    private static int count = 0, previous = 0x00;
    private static byte tmpPack[] = new byte[14];
    private static boolean isStart = false;
    private Runnable readThread = new Runnable() {
        private Byte tmp;
        public void run() {
            if(!dataCircle.isEmpty()) {
                for (int i=0; i<14; i++) {
                    if (isStart) {
                        if ((tmp=dataCircle.pull()) == null) {
                            break;
                        } else {
                            tmpPack[count++] = tmp;
                            if (count==14) {
                                sendData(tmpPack);
                                count = 2;
                                previous = 0x00;
                                isStart = false;
                                tmpPack = new byte[14];
                            }
                        }
                    } else {
                        if ((tmp=dataCircle.pull()) == null) {
                            break;
                        } else {
                            if ((tmp&0xff)==0xaa && previous == 0xaa) {
                                //java中byte由补码来表示
                                tmpPack[0] = -86;  //0xAA
                                tmpPack[1] = -86;
                                isStart = true;
                            } else if((tmp&0xff)==0xaa) {
                                previous = tmp&0xff;
                            }
                        }
                    }
                }
            } else {
                getMeterData(new byte[14]);
            }
            mHandler.postDelayed( readThread, 30); }
    };

    //从读取线程中读取到的数据发出来
    private void sendData(byte[] pack) {
        if (isLegal(pack)) {
            Log.i("CZQ", bytesToHex(pack, 14));
            getMeterData(pack);
            mHandler.sendEmptyMessage(1);
        }
    }


    //判断数据包是否合法
    private boolean isLegal(byte[] pack) {
        int index;
        int sumCheck = 0;
        for (index = 0; index < 14; index++) {
            if (index < 2) {
                //判断数据包包头是否合法
                if ((pack[index] & 0xff) != 0xaa) {
                    Log.i("CZQ", "头错误");
                    return false;
                }
            } else if (index == 11) {
                sumCheck = sumCheck & 0xff;
                //判断数据包校验和是否正常
                if ((pack[index] & 0xff) != sumCheck) {
                    Log.i("CZQ", "校验错误");
                    return false;
                }
            } else if (index > 11) {
                //判断数据包包尾是否合法
                if ((pack[index] & 0xff) != 0xbb) {
                    Log.i("CZQ", "尾错误");
                    return false;
                }
            } else if (index > 2) {
                //sumCheck += (byte)(msg[index] & 0x0f);
                sumCheck += pack[index] & 0xff;
            }
        }
        return true;
    }

    //将接收到的数据包转化为虚拟表数据
    private void getMeterData(byte[] packet) {
        //mDeviceID = packet[3] & 0xff;
        int row, column, index = 4;
        for (row = 0; row < 3; row++) {
            for (column = 0; column < 2; column++) {
                meterData[row][column] = packet[index++] & 0xff;
            }
        }
        /*if ((packet[10] & 0xff) == 0) {
            meterData[0][0] = 0;
            meterData[0][1] = 0;
        }*/
    }

    //将byte转为对应字符串的函数
    private static final char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private  String bytesToHex(byte[] bytes, int length) {
        char[] buf = new char[length * 2];
        int index = 0;
        byte tmp;
        for(int i=0; i<length; i++) { // 利用位运算进行转换，可以看作方法一的变种
            tmp = bytes[i];
            buf[index++] = HEX_CHAR[tmp >>> 4 & 0x0f];
            buf[index++] = HEX_CHAR[tmp & 0x0f];
        }
        return new String(buf);
    }
}
