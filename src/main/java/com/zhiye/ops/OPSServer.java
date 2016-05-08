package com.zhiye.ops;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;


public class OPSServer {

	public OPSHandler hanlder;
	
	public NioSocketAcceptor socketAcceptor;
	
	public int port;
	
	public int mobPort;
	
	public String mobIp ;
	
	public OPSServer(){
		try{
			mobIp = PropertiesRead.getModbusIp();
			mobPort = Integer.parseInt(PropertiesRead.getModbusPort());
			port = Integer.parseInt(PropertiesRead.getHostPort());
			
			socketAcceptor = new NioSocketAcceptor(Runtime.getRuntime().availableProcessors() + 1);
			socketAcceptor.setReuseAddress(true);
			socketAcceptor.getSessionConfig().setKeepAlive(true);
			hanlder = new OPSHandler(mobIp,mobPort);
			socketAcceptor.setHandler(hanlder);
			
			///设置过滤器
			DefaultIoFilterChainBuilder chain=socketAcceptor.getFilterChain();
			chain.addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));  //明码字符串
		}catch(Exception e){
			 e.printStackTrace();
		}
		
	}
	

	public void startAcceptor(){
        try {
            socketAcceptor.bind(new InetSocketAddress(port));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	 public static void main(String[] args) {
	        //start mina and database
		 try{

		    OPSServer main = new OPSServer();
	        main.startAcceptor();
	        System.out.println("智慧运维实时监控设备启动完备......");
		 }catch(Exception e){
			 System.out.println(e.getMessage());
		 }
				 
	    }
	
}
