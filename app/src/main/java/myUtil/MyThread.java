package MyUtil;

import android.os.Handler;

public class MyThread extends Thread {
    private int flag;
    private long sleepTime;
    private Handler mHandler;

    public MyThread(long sleepTime, Handler mHandler, int flag) {
        this.flag = flag;
        this.sleepTime = sleepTime;
        this.mHandler = mHandler;
    }
    @Override
    public void run() {
        while(true) {
            mHandler.sendEmptyMessage(flag);
            if (this.isInterrupted()) {
                break;
            }
            try {
                Thread.sleep(sleepTime);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
