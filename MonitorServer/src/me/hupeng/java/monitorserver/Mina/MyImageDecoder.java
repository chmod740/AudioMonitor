package me.hupeng.java.monitorserver.Mina;


import me.hupeng.java.monitorserver.util.OperateImage;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/**
 * Created by HUPENG on 2016/9/4.
 */
public class MyImageDecoder extends CumulativeProtocolDecoder {

    @Override
    protected boolean doDecode(IoSession ioSession, IoBuffer ioBuffer, ProtocolDecoderOutput protocolDecoderOutput) throws Exception {
        if(ioBuffer.remaining() > 6){//前4字节是包头
            //标记当前position的快照标记mark，以便后继的reset操作能恢复position位置
            ioBuffer.mark();
//            byte[] l = new byte[4];
//            ioBuffer.get(l);
            //包体数据长度

            int len = ioBuffer.getInt();
            int client_id = ioBuffer.getInt();

            //int len = MyTools.bytes2int(l);//将byte转成int



            if (len == 0){

                MyReceiveData myReceiveData = new MyReceiveData();
                myReceiveData.buffer = new short[0];
                myReceiveData.clientId = client_id;
                protocolDecoderOutput.write(myReceiveData);
                return true;
            }

            //注意上面的get操作会导致下面的remaining()值发生变化
            if(ioBuffer.remaining() < len){
                //如果消息内容不够，则重置恢复position位置到操作前,进入下一轮, 接收新数据，以拼凑成完整数据
                ioBuffer.reset();
                return false;
            }else{
                //消息内容足够

                ioBuffer.reset();
                int length = ioBuffer.getInt();
                client_id = ioBuffer.getInt();


                byte dest[] = new byte[length];
                ioBuffer.get(dest);


                short s[] = new short[dest.length / 2];
                for (int i = 0 ; i < dest.length ; i= i+2){
                    byte[] b = new byte[2];
                    b[0] = dest[i];
                    b[1] = dest[i+1];
                    s[i/2] =byteToShort(b);
                }

                MyReceiveData myReceiveData = new MyReceiveData();
                myReceiveData.buffer = s;
                myReceiveData.clientId = client_id;
                protocolDecoderOutput.write(myReceiveData);

                return true;
            }
        }
        return false;//处理成功，让父类进行接收下个包
    }


    private short byteToShort(byte[] b) {
        short s = 0;
        short s0 = (short) (b[0] & 0xff);// 最低位
        short s1 = (short) (b[1] & 0xff);
        s1 <<= 8;
        s = (short) (s0 | s1);
        return s;
    }

}
