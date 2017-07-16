package me.hupeng.java.monitorserver.util;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * 录音文件处理工具类
 * @author hupeng@imudges.com
 * @version 1.0
 */
public class RecordFileUtil {
    private int clientId;

    /**
     * 文件路径对象
     */
    private File fpath;


    /**
     *  音频流对象
     */
    private DataOutputStream dos = null;

    //文件名
    private String fileName;

    //录音文件对象
    private File audioFile;

    /**
     * 初始化值
     */
    public void init(){
        //文件路径
        String pathStr ="./pcm/";
        fpath = new File(pathStr);
        //判断文件夹是否存在,若不存在则创建
        if (!fpath.exists()){
            fpath.mkdirs();
        }
    }

    /**
     * 记录文件
     */
    public RecordFileUtil(int clientId){
        this.clientId = clientId;
        //每个loveLog对应一个录音文件
        init();
    }

    /**
     * 将数据写入buffer
     * @param buffer 音频数据
     */
    public synchronized void writeBuffer(short buffer){
        if (dos==null){
            try {
                audioFile = File.createTempFile("client_" + clientId + "_" + (System.currentTimeMillis() % 1000000l), ".pcm", fpath);
                dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(audioFile)));
                fileName = audioFile.getName();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        try{
            dos.writeShort(buffer);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 将数据写入buffer
     * @param buffer 音频数据
     */
    public void writeBuffer(short buffer[]){
        for (int i = 0 ; i < buffer.length ; i ++){
            writeBuffer(buffer[i]);
        }
    }

    /**
     * 停止写入buffer
     */
    public void stopWriteBuffer(){
        if (dos!=null){
            try{
                dos.close();
                dos = null;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        stopWriteBuffer();
        super.finalize();
    }

    /**
     * 得到文件名
     * @return 文件名
     */
    public String getFileName(){
        return fileName;
    }
}
