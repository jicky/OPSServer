package com.zhiye.ops;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.fasterxml.jackson.core.JsonGenerator;

import com.zhiye.ops.model.ModbusSend;

public class OPSHandler extends IoHandlerAdapter {

	public ModbusTCPMaster modbusTCPMaster;

	private String readOrWrit; /// 读或写类型

	private int slaveId; // 从站地址

	private int start; // 数据开始地址

	private int len; // 数据度

	private short[] values; // 写数值的长度

	private boolean[] boolArray;

	private org.codehaus.jackson.JsonGenerator jsonGenerator = null;

	private ObjectMapper objectMapper = null;

	public OPSHandler(String ip, Integer port) {
		try {
			modbusTCPMaster = new ModbusTCPMaster(ip, port);
			objectMapper = new ObjectMapper();
			try {
				jsonGenerator = objectMapper.getJsonFactory().createJsonGenerator(System.out, JsonEncoding.UTF8);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {

		}
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {

		System.out.println("出现异常请检查.....");
		cause.printStackTrace();

	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		analyRevData(message); // 转换对象类型
		
		if (message == null) {
			session.write("服务端返回数据: 传入的参数数据为空！");
		} else {
			if (this.readOrWrit.equals(Constant.TYPE_RDI)) { /// 读离散输入状态

				boolean[] bools = modbusTCPMaster.readDiscreteInput(this.slaveId, this.start, this.len);
				session.write(bools);  ///读离散输入状态

			} else if (this.readOrWrit.equals(Constant.TYPE_RHRS)) { /// 读数据
				/*short[] shtAry = null;
				shtAry = modbusTCPMaster.readHoldingRegisters(this.slaveId, this.start, this.len);
				session.write(arrayShortToStr(start, shtAry)); // 返回读取的数据  读保持寄存器
                */
				
				Map hasMap  = modbusTCPMaster.getValues(values);

				session.write(objectMapper.writeValueAsString(hasMap));

				
				
			} else if (this.readOrWrit.equals(Constant.TYPE_WHRS)) { /// 写数据
																		/// ,并返回是否成功

				boolean isWrite = modbusTCPMaster.writeRegisters(this.slaveId, this.start, this.values);
				if (isWrite == true) {
					session.write("写入数据成功 " + isWrite);
				} else {
					session.write("写入数据失败 " + isWrite);
				}

			} else if (this.readOrWrit.equals(Constant.TYPE_RCS)) {
				/*boolean[] bolAry = null;
				bolAry = modbusTCPMaster.readCoils(this.slaveId, this.start, this.len);
				session.write(arrayBoolToStr(bolAry));   // 返回读取线圈的数据*/
				
				short[] sht=new short[4];
				sht[0]=40;
				sht[1]=41;
				System.out.println("------------111测试数据11");				
				Map hasMap  = modbusTCPMaster.getValues(sht);

				session.write(objectMapper.writeValueAsString(hasMap));


			} else if (this.readOrWrit.equals(Constant.TYPE_WCS)) {
				//System.out.print(this.boolArray + "------" + this.boolArray.length);
				boolean[] bolA = new boolean[1];
				bolA[0] = true;
				boolean isWrite = modbusTCPMaster.writeCoils(this.slaveId, this.start, bolA);

				if (isWrite == true) {
					session.write("写入数据成功 " + isWrite);
				} else {
					session.write("写入数据失败 " + isWrite);
				}

			} else if (this.readOrWrit.equals(Constant.TYPE_RIRS)) {
				boolean[] bools = modbusTCPMaster.readInputRegisters(slaveId, start, len);
				session.write(bools);   //读输入寄存器数据
			}

		}
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("关闭会话 " + session.getRemoteAddress().toString());
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {

		System.out.println("打开会话 " + session.getRemoteAddress().toString());

	}

	/**
	 * 将取的值转换对象类型
	 * 
	 * @param message
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public void analyRevData(Object message) throws JsonParseException, JsonMappingException, IOException {
		/// slaveId+";"+start+";"+len+";"+values;
		System.out.println(message.toString() + "---------*****");
		short[] shtVal = null;
		boolean[] boolVal = null;
		ModbusSend modbusSend = objectMapper.readValue(message.toString(), ModbusSend.class); 
		
		this.readOrWrit = modbusSend.getReadOrWrit(); // 读或写类型
		this.slaveId = modbusSend.getSlaveId(); // 取得从站地址
		this.start = modbusSend.getStart(); // 取得开始地址
		this.len = modbusSend.getLen(); // 取得取值长度
		
		if (modbusSend.getShrtArray()!=null) {   //整数
			this.values = modbusSend.getShrtArray(); // 发送的数组值
		} 
		if (this.readOrWrit.equals(Constant.TYPE_WCS)) {

			this.boolArray = modbusSend.getBoolArray(); // 发送的数组值

		}
		System.out.println(this.readOrWrit+"---"+this.slaveId+"--"+this.start+"---"+this.len+"---"+this.values+"----"+this.boolArray);
		// System.out.println("server:"+boolArray[0]+"---"+this.boolArray.length);

	}

	/**
	 * 封装数据成字符串
	 * 
	 * @param shtArray
	 * @return
	 * @throws IOException
	 * @throws JsonProcessingException
	 */
	public String arrayShortToStr(int start, short[] shtAry) throws JsonProcessingException, IOException {
		String valStr = "";
		Map hashMap = new HashMap();
		if (shtAry != null && shtAry.length > 0) {
			for (int i = 0; i < shtAry.length; i++) {
				hashMap.put((start + i), shtAry[i]);
			}
		}
		  //用objectMapper直接返回list转换成的JSON字符串
        System.out.println("发送给客户端的内容: " + objectMapper.writeValueAsString(hashMap));
		return objectMapper.writeValueAsString(hashMap);
	}

	/**
	 * 封装数据成字符串
	 * 
	 * @param shtArray
	 * @return
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonGenerationException 
	 */
	public String arrayBoolToStr(boolean[] bolAry) throws JsonGenerationException, JsonMappingException, IOException {
		String valStr = "";
		Map hashMap = new HashMap();
		if (bolAry != null && bolAry.length > 0) {
			for (int i = 0; i < bolAry.length; i++) {
				hashMap.put((start + i), bolAry[i]);
			}
		}
		  //用objectMapper直接返回list转换成的JSON字符串
        System.out.println("发送给客户端的内容: " + objectMapper.writeValueAsString(hashMap));

		return objectMapper.writeValueAsString(hashMap);
	}
}
