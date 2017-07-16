package me.hupeng.android.monitor.Mina;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/**
 * Created by HUPENG on 2016/9/4.
 */
public class MyImageEncoder implements ProtocolEncoder {

    @Override
    public void encode(IoSession ioSession, Object message, ProtocolEncoderOutput out) throws Exception {
        MySendData mySendData = null;
        if (message instanceof MySendData){
            mySendData = (MySendData) message;
        }
        if (mySendData != null){
            //读取图片到ByteArrayOutputStream

//            CharsetEncoder charsetEncoder = (CharsetEncoder)ioSession.getAttribute("encoder");
//            if(charsetEncoder == null){
//                charsetEncoder = Charset.defaultCharset().newEncoder();
//                ioSession.setAttribute("encoder", charsetEncoder);
//            }


            /**
             * 因为一个short占两个字节,所以...
             * */
            byte[] audioByte = new byte[mySendData.buffer.length*2];

            for (int i = 0 ; i < audioByte.length ; i=i+2){
                byte[] b = shortToByte(mySendData.buffer[i/2]);
                audioByte[i] = b[0];
                audioByte[i + 1] = b[1];
            }

            int length = audioByte.length;

            IoBuffer ioBuffer;
            ioBuffer = IoBuffer.allocate(1024).setAutoExpand(true);
            ioBuffer.setAutoShrink(true);
            ioBuffer.setAutoExpand(true);
            ioBuffer.putInt(length);
            ioBuffer.putInt(mySendData.clientId);
            ioBuffer.put(audioByte);
            ioBuffer.capacity(length+8);
            ioBuffer.flip();
            out.write(ioBuffer);

        }
    }

    @Override
    public void dispose(IoSession ioSession) throws Exception {

    }


    /**
     * 功能 短整型与字节的转换
     * @param number 短整型
     * @return 两位的字节数组
     */
    private byte[] shortToByte(short number) {
        int temp = number;
        byte[] b = new byte[2];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Integer(temp & 0xff).byteValue();// 将最低位保存在最低位
            temp = temp >> 8; // 向右移8位
        }
        return b;
    }

}
