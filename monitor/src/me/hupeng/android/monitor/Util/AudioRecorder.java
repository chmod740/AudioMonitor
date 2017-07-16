package me.hupeng.android.monitor.Util;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import me.hupeng.android.monitor.Listener.AudioBufferListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 此类主要实现录音</br>
 * <a href="http://www.jb51.net/article/64806.htm">Android中实时获取音量分贝值详解</a>
 * @version 1.0
 * 录音类
 */
public class AudioRecorder {
    /**
     * 一、实例化一个AudioRecord类我们需要传入几种参数
     * 1、AudioSource：这里可以是MediaRecorder.AudioSource.MIC
     * 2、SampleRateInHz:录制频率，可以为8000hz或者11025hz等，不同的硬件设备这个值不同
     * 3、ChannelConfig:录制通道，可以为AudioFormat.CHANNEL_CONFIGURATION_MONO和AudioFormat.CHANNEL_CONFIGURATION_STEREO
     * 4、AudioFormat:录制编码格式，可以为AudioFormat.ENCODING_16BIT和8BIT,其中16BIT的仿真性比8BIT好，但是需要消耗更多的电量和存储空间
     * 5、BufferSize:录制缓冲大小：可以通过getMinBufferSize来获取
     * */
    private static final String TAG = "AudioRecord";

    static final int SAMPLE_RATE_IN_HZ = 16000;//一秒钟采集8000个点

    static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
            AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
    //static final int BUFFER_SIZE = 1024;

    AudioRecord mAudioRecord;
    public boolean isGetVoiceRun;
    Object mLock;


    public AudioBufferListener audioBufferListener;

    //数据队列
    List<short[]> list = new ArrayList<>();

    public AudioRecorder() {
        mLock = new Object();

    }

//    private RecordFileUtil recordFileUtil;

    /**
     * 开始录音
     * */
    public void startRecord() {
//        recordFileUtil = new RecordFileUtil((int)(System.currentTimeMillis() % 10000000));

        if (isGetVoiceRun) {
            Log.e(TAG, "还在录着呢");
            return;
        }
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_DEFAULT,
                AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE);
        if (mAudioRecord == null) {
            Log.e("sound", "mAudioRecord初始化失败");
        }
        isGetVoiceRun = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                mAudioRecord.startRecording();
                short[] buffer = new short[BUFFER_SIZE];
                while (isGetVoiceRun) {
                    //r是实际读取的数据长度，一般而言r会小于buffersize
                    int r = mAudioRecord.read(buffer, 0, BUFFER_SIZE);
                    long v = 0;
                    //将buffer加入数据队列中
                    addToList(buffer);
                    // 将 buffer 内容取出，进行平方和运算
                    for (int i = 0; i < buffer.length; i++) {
                        v += buffer[i] * buffer[i];
                    }

//                    recordFileUtil.writeBuffer(buffer);


                    // 平方和除以数据总长度，得到音量大小。
                    double mean = v / (double) r;
                    //Log.d(TAG,"能量数据:" + mean);
                    double volume = 10 * Math.log10(mean);
                    //audioRecorderListener.onSuccess(volume);
                    //以下给监听器设值
                    if (audioBufferListener!=null){
                        audioBufferListener.getValue(buffer);
                    }
                    //Log.d(TAG, "分贝值:" + volume);
                    // 大概一秒二十次
                    synchronized (mLock) {
                        try {
                            mLock.wait(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;
            }
        }).start();
    }


    /**
     * 停止录音
     * */
    public void stopRecord(){
        isGetVoiceRun = false;

        try {
//            recordFileUtil.stopWriteBuffer();
        }catch (Exception e){

        }
    }

    /**
     * 往数据队列里面存数据
     * */
    private void addToList(short[] buffer){
        if (list == null){
            list = new ArrayList<>();
        }
        if (list.size()>100){
            list.remove(0);
        }
        list.add(buffer);
    }

    /**
     * 从数据队列里面读取buffer，主动获取数据的接口，此处不用
     * @param n 要从队列里面取得的buffer[]的最大数量
     * */
    public short[] readBuffer(int n){
        if (n>list.size()){
            n = list.size();
        }
        int sum = 0;
        for (int i=0;i<n;i++){
            sum += list.get(i).length;
        }
        int t = 0;
        short[] dataBuffer = new short[sum];
        for (int i=0;i<n;i++){
            for (int j=0;j<list.get(i).length;j++){
                dataBuffer[t++] = list.get(i)[j];
            }
        }
        for (int i = 0 ; i< n ; i ++){
            list.remove(0);
        }//此处的写法可能有问题，但是我又用不到这个函数随它去吧>>>>>>>by hp
        return dataBuffer;
    }

    /**
     * 使用监听者模式从List中读取数据
     * @param audioBufferListener 录音的接口
     * */
    public void setBufferListener(AudioBufferListener audioBufferListener){
        this.audioBufferListener = audioBufferListener;
    }
}