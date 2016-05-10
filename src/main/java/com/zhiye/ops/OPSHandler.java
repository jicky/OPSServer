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

	private String readOrWrit; /// ����д����

	private int slaveId; // ��վ��ַ

	private int start; // ���ݿ�ʼ��ַ

	private int len; // ���ݶ�

	private short[] values; // д��ֵ�ĳ���

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

		System.out.println("�����쳣����.....");
		cause.printStackTrace();

	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		analyRevData(message); // ת����������
		
		if (message == null) {
			session.write("����˷�������: ����Ĳ�������Ϊ�գ�");
		} else {
			if (this.readOrWrit.equals(Constant.TYPE_RDI)) { /// ����ɢ����״̬

				boolean[] bools = modbusTCPMaster.readDiscreteInput(this.slaveId, this.start, this.len);
				session.write(bools);  ///����ɢ����״̬

			} else if (this.readOrWrit.equals(Constant.TYPE_RHRS)) { /// ������
				/*short[] shtAry = null;
				shtAry = modbusTCPMaster.readHoldingRegisters(this.slaveId, this.start, this.len);
				session.write(arrayShortToStr(start, shtAry)); // ���ض�ȡ������  �����ּĴ���
                */
				
				Map hasMap  = modbusTCPMaster.getValues(values);

				session.write(objectMapper.writeValueAsString(hasMap));

				
				
			} else if (this.readOrWrit.equals(Constant.TYPE_WHRS)) { /// д����
																		/// ,�������Ƿ�ɹ�

				boolean isWrite = modbusTCPMaster.writeRegisters(this.slaveId, this.start, this.values);
				if (isWrite == true) {
					session.write("д�����ݳɹ� " + isWrite);
				} else {
					session.write("д������ʧ�� " + isWrite);
				}

			} else if (this.readOrWrit.equals(Constant.TYPE_RCS)) {
				/*boolean[] bolAry = null;
				bolAry = modbusTCPMaster.readCoils(this.slaveId, this.start, this.len);
				session.write(arrayBoolToStr(bolAry));   // ���ض�ȡ��Ȧ������*/
				
				short[] sht=new short[4];
				sht[0]=40;
				sht[1]=41;
				System.out.println("------------111��������11");				
				Map hasMap  = modbusTCPMaster.getValues(sht);

				session.write(objectMapper.writeValueAsString(hasMap));


			} else if (this.readOrWrit.equals(Constant.TYPE_WCS)) {
				//System.out.print(this.boolArray + "------" + this.boolArray.length);
				boolean[] bolA = new boolean[1];
				bolA[0] = true;
				boolean isWrite = modbusTCPMaster.writeCoils(this.slaveId, this.start, bolA);

				if (isWrite == true) {
					session.write("д�����ݳɹ� " + isWrite);
				} else {
					session.write("д������ʧ�� " + isWrite);
				}

			} else if (this.readOrWrit.equals(Constant.TYPE_RIRS)) {
				boolean[] bools = modbusTCPMaster.readInputRegisters(slaveId, start, len);
				session.write(bools);   //������Ĵ�������
			}

		}
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("�رջỰ " + session.getRemoteAddress().toString());
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {

		System.out.println("�򿪻Ự " + session.getRemoteAddress().toString());

	}

	/**
	 * ��ȡ��ֵת����������
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
		
		this.readOrWrit = modbusSend.getReadOrWrit(); // ����д����
		this.slaveId = modbusSend.getSlaveId(); // ȡ�ô�վ��ַ
		this.start = modbusSend.getStart(); // ȡ�ÿ�ʼ��ַ
		this.len = modbusSend.getLen(); // ȡ��ȡֵ����
		
		if (modbusSend.getShrtArray()!=null) {   //����
			this.values = modbusSend.getShrtArray(); // ���͵�����ֵ
		} 
		if (this.readOrWrit.equals(Constant.TYPE_WCS)) {

			this.boolArray = modbusSend.getBoolArray(); // ���͵�����ֵ

		}
		System.out.println(this.readOrWrit+"---"+this.slaveId+"--"+this.start+"---"+this.len+"---"+this.values+"----"+this.boolArray);
		// System.out.println("server:"+boolArray[0]+"---"+this.boolArray.length);

	}

	/**
	 * ��װ���ݳ��ַ���
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
		  //��objectMapperֱ�ӷ���listת���ɵ�JSON�ַ���
        System.out.println("���͸��ͻ��˵�����: " + objectMapper.writeValueAsString(hashMap));
		return objectMapper.writeValueAsString(hashMap);
	}

	/**
	 * ��װ���ݳ��ַ���
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
		  //��objectMapperֱ�ӷ���listת���ɵ�JSON�ַ���
        System.out.println("���͸��ͻ��˵�����: " + objectMapper.writeValueAsString(hashMap));

		return objectMapper.writeValueAsString(hashMap);
	}
}
