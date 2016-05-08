package com.zhiye.ops;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesRead {
	
	    private static String hostPort;   
	    private static String modbusIp;   
	    private static String modbusPort;
	    
		static {   
	        Properties prop = new Properties();   
	        InputStream in = Object.class.getResourceAsStream("/config.properties");   
	        try {   
	            prop.load(in);   
	            hostPort = prop.getProperty("HostPort").trim();  
	            modbusIp = prop.getProperty("ModbusIp").trim();   
	            modbusPort = prop.getProperty("ModbusPort").trim();  
	        } catch (IOException e) {   
	            e.printStackTrace();   
	        }   
	    }   
		 /**  
	     * 私有构造方法，不需要创建对象  
	     */   
	    private PropertiesRead() {   
	    }  
		

		public static String getHostPort() {
			return hostPort;
		}

		public static String getModbusIp() {
			return modbusIp;
		}

		public static String getModbusPort() {
			return modbusPort;
		}


}
