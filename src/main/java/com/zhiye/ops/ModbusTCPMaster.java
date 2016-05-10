package com.zhiye.ops;

import com.serotonin.modbus4j.*;
import com.serotonin.modbus4j.code.DataType;
import com.serotonin.modbus4j.code.RegisterRange;
import com.serotonin.modbus4j.exception.ErrorResponseException;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.ip.IpParameters;
import com.serotonin.modbus4j.msg.ReadCoilsRequest;
import com.serotonin.modbus4j.msg.ReadCoilsResponse;
import com.serotonin.modbus4j.msg.ReadDiscreteInputsRequest;
import com.serotonin.modbus4j.msg.ReadDiscreteInputsResponse;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersRequest;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersResponse;
import com.serotonin.modbus4j.msg.ReadInputRegistersRequest;
import com.serotonin.modbus4j.msg.ReadInputRegistersResponse;
import com.serotonin.modbus4j.msg.WriteCoilRequest;
import com.serotonin.modbus4j.msg.WriteCoilResponse;
import com.serotonin.modbus4j.msg.WriteCoilsRequest;
import com.serotonin.modbus4j.msg.WriteCoilsResponse;
import com.serotonin.modbus4j.msg.WriteRegistersRequest;
import com.serotonin.modbus4j.msg.WriteRegistersResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * rCs ��������Ȧ�ϵ����� ;wCs ����д���ݵ���Ȧ;rHRs �����ּĴ����ϵ����� ;wHRs ����д���ݵ����ּĴ���;rIRs ������Ĵ��� ;rDI ����ɢ����״̬
 * @author zl 2016/5/4
 */
public class ModbusTCPMaster {
	
	private String ip;
	private Integer port;

	public ModbusTCPMaster(String ip, Integer port) {
		this.ip = ip;
		this.port = port;

	}

	private static ModbusMaster master;
	
	private  void initMaster(){
		/// ����Master
		if (master == null) {
			IpParameters tcpParameters = new IpParameters();
			tcpParameters.setHost(this.ip);
			tcpParameters.setPort(this.port);

			ModbusFactory modbusFactory = new ModbusFactory();
			master = modbusFactory.createTcpMaster(tcpParameters, false);
			try {
				master.init();
			} catch (ModbusInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public synchronized Map<Integer, BigDecimal> get(Integer[] addressCode)
			throws ModbusInitException, ErrorResponseException, ModbusTransportException {
		/// ����Master
		initMaster();

		int slaveId = 1;
		BatchRead<String> batchRead = new BatchRead<String>();
		// master.setTimeout(5000);

		try {

			if (addressCode.length > 0) {
				for (Integer code : addressCode) {
					ModbusLocator locator = new ModbusLocator(slaveId, RegisterRange.HOLDING_REGISTER, code,
							DataType.FOUR_BYTE_FLOAT);
					batchRead.addLocator("voltage " + code, locator);
				}
			}
		} finally {
			// master.destroy();
		}
		Map<Integer, BigDecimal> valMap = new HashMap<Integer, BigDecimal>();
		BatchResults<String> results = master.send(batchRead);
		if (addressCode.length > 0) {
			for (Integer code : addressCode) {
				System.out.println("voltage " + code + "=" + results.getValue("voltage " + code));
				BigDecimal val = new BigDecimal(String.valueOf(results.getValue("voltage " + code)));
				valMap.put(code, val);
			}
		}
		// valMap.put(12,new BigDecimal("123.00"));
		// valMap.put(16,new BigDecimal("456.00"));

		return valMap;
	}

	/**
	 * ����Ϊ ��rDI�� ����ɢ����״̬  ��ַ��10001-19999
	 * 
	 * @param slaveId
	 *            ��վ��ַ
	 * @param start
	 *            ��ʼƫ����
	 * @param len
	 *            �����Ŀ������ĸ���
	 */
	public synchronized boolean[] readDiscreteInput( int slaveId, int start, int len) {
		initMaster();
		boolean[] boolArray = null;
		try {
			ReadDiscreteInputsRequest request = new ReadDiscreteInputsRequest(slaveId, start, len);
			ReadDiscreteInputsResponse response = (ReadDiscreteInputsResponse) master.send(request);
			if (response.isException())
				System.out.println("Exception response: message=" + response.getExceptionMessage());
			else{
				System.out.println(Arrays.toString(response.getBooleanData()));
			    boolArray = response.getBooleanData();
			}
		} catch (ModbusTransportException e) {
			e.printStackTrace();
		}
		return boolArray;
	}
	
	/**
	 * ����Ϊ��rIRs��������Ĵ���  ��ַ��30001-39999
	 * 
	 * @param slaveId
	 *            ��վ��ַ
	 * @param start
	 *            ��ʼƫ����
	 * @param len
	 *            �����Ŀ������ĸ���
	 */
	public synchronized boolean[] readInputRegisters( int slaveId, int start, int len) {
		initMaster();
		boolean[] boolArray = null;
		try {
			ReadInputRegistersRequest  request = new ReadInputRegistersRequest (slaveId, start, len);
			ReadInputRegistersResponse  response = (ReadInputRegistersResponse ) master.send(request);
			if (response.isException())
				System.out.println("Exception response: message=" + response.getExceptionMessage());
			else{
				System.out.println(Arrays.toString(response.getBooleanData()));
			    boolArray = response.getBooleanData();
			}
		} catch (ModbusTransportException e) {
			e.printStackTrace();
		}
		return boolArray;
	}

	/**
	 * ����Ϊ  ��rHRs�� �����ּĴ����ϵ����� ��ַ��40001-49999
	 * 
	 * @param slaveId
	 *            ��վ��ַ
	 * @param start
	 *            ��ʼ��ַ��ƫ����
	 * @param len
	 *            �����Ĵ����ĸ���
	 */
	public synchronized short[] readHoldingRegisters(int slaveId, int start, int len) {
		initMaster();
		short[] list=null;
		try {
			ReadHoldingRegistersRequest request = new ReadHoldingRegistersRequest(slaveId, start, len);
			ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse) master.send(request);
			if (response.isException()) {
				System.out.println("Exception response: message=" + response.getExceptionMessage());
			} else {
				
				list = response.getShortData();

			}
		} catch (ModbusTransportException e) {
			e.printStackTrace();
		}
		return list;
	}
	

	/**
	 * ����Ϊ  ��rHRs�� �����ּĴ����ϵ����� ��ַ��40001-49999
	 * 
	 * @param slaveId
	 *            ��վ��ַ
	 * @param start
	 *            ��ʼ��ַ��ƫ����
	 * @param len
	 *            �����Ĵ����ĸ���
	 */
	public synchronized byte[] readHoldingRegistersByte(int slaveId, int start, int len) {
		initMaster();
		byte[] list=null;
		try {
			ReadHoldingRegistersRequest request = new ReadHoldingRegistersRequest(slaveId, start, len);
			ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse) master.send(request);
			if (response.isException()) {
				System.out.println("Exception response: message=" + response.getExceptionMessage());
			} else {
				
				list = response.getData();//.getShortData();

			}
		} catch (ModbusTransportException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * ����Ϊ ��wHRs�� ����д���ݵ����ּĴ���  ��ַ�� 40001-49999
	 * 
	 * @param slaveId
	 *            ��վ��ַ
	 * @param start
	 *            ��ʼ��ַ��ƫ����
	 * @param values
	 *            ��д����
	 */
	public synchronized boolean writeRegisters(int slaveId, int start, short[] values) {
		initMaster();
		boolean isSuccess = false;
		try {
			WriteRegistersRequest request = new WriteRegistersRequest(slaveId, start, values);
			WriteRegistersResponse response = (WriteRegistersResponse) master.send(request);
			
			//WriteCoilRequest rq = new WriteCoilRequest(1, 40, false);
			//WriteCoilResponse response = (WriteCoilResponse) master.send(rq);
			
			if (response.isException()) {
				System.out.println("Exception response: message=" + response.getExceptionMessage());
			} else {
				isSuccess = true;
				System.out.println("Success");
			}
		} catch (ModbusTransportException e) {
			e.printStackTrace();
		}
		return isSuccess;
	}
	
	

	/**
	 * ���͡�rCs�� ��������Ȧ�ϵ����� ��ַ�ǣ�00001-09999    ����Boolean����
	 * 
	 * @param slaveId
	 *            ��վ��ַ
	 * @param start
	 *            ��ʼ��ַ��ƫ����
	 * @param len
	 *            �����Ĵ����ĸ���
	 */
	public synchronized boolean[] readCoils(int slaveId, int start, int len) {
		initMaster();
		boolean[] list=null;
		try {
	
			ReadCoilsRequest request = new ReadCoilsRequest(slaveId, start, len);
			ReadCoilsResponse response = (ReadCoilsResponse) master.send(request);
			if (response.isException()) {
				System.out.println("Exception response: message=" + response.getExceptionMessage());
			} else {
				System.out.println(response.getBooleanData()+"===="+Arrays.toString(response.getData())+"{{"+response.getShortData().length);
				list = response.getBooleanData();

			}
		} catch (ModbusTransportException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	
	/**
	 * ���͡�rCs�� ��������Ȧ�ϵ����� ��ַ�ǣ�00001-09999  ����Byte����
	 * 
	 * @param slaveId
	 *            ��վ��ַ
	 * @param start
	 *            ��ʼ��ַ��ƫ����
	 * @param len
	 *            �����Ĵ����ĸ���
	 */
	public synchronized byte[] readCoilsByte(int slaveId, int start, int len) {
		initMaster();
		byte[] list=null;
		try {
	
			ReadCoilsRequest request = new ReadCoilsRequest(slaveId, start, len);
			ReadCoilsResponse response = (ReadCoilsResponse) master.send(request);
			if (response.isException()) {
				System.out.println("Exception response: message=" + response.getExceptionMessage());
			} else {
				System.out.println(Arrays.toString(response.getData())+"----"+response.getData()[0]);
				list = response.getData();

			}
		} catch (ModbusTransportException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	
	/**
	 * ���͡�wCs�� ����д���ݵ���Ȧ  ��ַ�ǣ�00001-09999
	 * 
	 * @param slaveId
	 *            ��վ��ַ
	 * @param start
	 *            ��ʼ��ַ��ƫ����
	 * @param values
	 *            ��д����
	 */
	public synchronized boolean writeCoilsByte(int slaveId, int start, boolean[] values) {
		initMaster();
		boolean isSuccess = false;
		try {
			WriteCoilsRequest request = new WriteCoilsRequest(slaveId, start, values);
			WriteCoilsResponse response = (WriteCoilsResponse) master.send(request);
			
			//WriteCoilRequest rq = new WriteCoilRequest(1, 40, false);
			///WriteCoilResponse response = (WriteCoilResponse) master.send(rq);
			if (response.isException()) {
				System.out.println("Exception response: message=" + response.getExceptionMessage());
			} else {
				isSuccess = true;
				System.out.println("Success");
			}
		} catch (ModbusTransportException e) {
			e.printStackTrace();
		}
		return isSuccess;
	}
	
	/**
	 * ���͡�wCs�� ����д���ݵ���Ȧ  ��ַ�ǣ�00001-09999
	 * 
	 * @param slaveId
	 *            ��վ��ַ
	 * @param start
	 *            ��ʼ��ַ��ƫ����
	 * @param values
	 *            ��д����
	 */
	public synchronized boolean writeCoils(int slaveId, int start, boolean[] values) {
		initMaster();
		boolean isSuccess = false;
		try {
			WriteCoilsRequest request = new WriteCoilsRequest(slaveId, start, values);
			WriteCoilsResponse response = (WriteCoilsResponse) master.send(request);
			if (response.isException()) {
				System.out.println("Exception response: message=" + response.getExceptionMessage());
			} else {
				isSuccess = true;
				System.out.println("Success");
			}
		} catch (ModbusTransportException e) {
			e.printStackTrace();
		}
		return isSuccess;
	}

	public synchronized Map<Integer, Short> getValues(short[] values) throws ModbusTransportException, ErrorResponseException {
		/// ����Master
		initMaster();

		int slaveId = 1;
		// master.setTimeout(5000);
		Map<Integer, Short> valMap = new HashMap<Integer, Short>();
		try {
			
			if (values.length > 0) {
				for (short code : values) {
					 short[] sht = readHoldingRegisters(slaveId, code, 1);
					 valMap.put(Integer.parseInt(String.valueOf( code)),  sht[0]);
				}
			}
		} finally {
			// master.destroy();
		}

		// valMap.put(12,new BigDecimal("123.00"));
		// valMap.put(16,new BigDecimal("456.00"));

		return valMap;
	}
	
	
	 public static void main(String[] args) throws IOException, ModbusTransportException, ErrorResponseException{
		 ModbusTCPMaster mtm = new ModbusTCPMaster("192.168.1.245",502);
		 
		 short[] sht1 = new short[1];
		 sht1[0] = 1;
	     mtm.getValues(sht1);



	 }
	
	

}
