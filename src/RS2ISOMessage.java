import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class RS2ISOMessage {

	public HashMap<String, String> msgMap = new HashMap<>();
	Properties prop = new Properties();
	
	public void setElement(String pos, String value) {
		if (value != null)
			msgMap.put(pos, value);
	}
	
	public void loadProperties() {

		try(InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("rs2message.properties");) {			
			
			prop.load(stream);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public byte[] pack() {
		StringBuffer fieldMessage = null;
		StringBuffer actualBitMap = null;
		StringBuffer message = null;
		StringBuffer msgTypeBitmapField = new StringBuffer();
		byte[] isoMsg = null;
		
		try {
			System.out.println(" ----- Transaction Starts ------------- ");
			System.out.println(" --- Message Packing Starts ---   ");

			// **************** BITMAP & Field attribute generation ******************* //
			// System.out.println("F-4: "+ env.getProperty("F-4"));
			fieldMessage = new StringBuffer();
			actualBitMap = new StringBuffer();
			
			System.out.println("MSG-TYP\t\t\t::\t"+ msgMap.get("MSG-TYP"));
			
			getBitmapFieldAtt(msgMap, fieldMessage, actualBitMap);

			// **************** BITMAP & Field attribute generation Ends*******************
			// //

			/* ************** Message Type + BitMap + Field Attribute Starts *********** */
			message = new StringBuffer();

			message.append(msgMap.get("MSG-TYP"));
			

			message.append(IsoUtils.binary2hex(actualBitMap.toString()));

			message.append(fieldMessage);

			msgTypeBitmapField.append(message);
			
			//Message Frame - 2 byte header - Data length in network byte order (big endian)
			BigInteger messageFrame = BigInteger.valueOf(msgTypeBitmapField.length());
			byte[] messageFrameArr = messageFrame.toByteArray();

			String msgTypeBitmapFieldMessage = msgTypeBitmapField.toString();

			//totalbytes1 = Hex.decodeHex(msgTypeBitmapFieldMessage.toCharArray());
			byte[] totalbytes1 = msgTypeBitmapFieldMessage.getBytes("ISO-8859-1");
			
			isoMsg = new byte[messageFrameArr.length+totalbytes1.length];
			
			System.arraycopy(messageFrameArr, 0, isoMsg, 0, messageFrameArr.length);
			System.arraycopy(totalbytes1, 0, isoMsg, messageFrameArr.length, totalbytes1.length);

			System.out.println(" --- Message Packing Completed ---   ");

		} catch (Exception e) {
			System.out.println("Error occured in formatting the VISA message pack()");
			isoMsg = null;
		} finally {
			fieldMessage = null;
			actualBitMap = null;
			message = null;
			msgTypeBitmapField = null;
		}
		return isoMsg;
	}
	
	public Map<String, String> unpack(String message) {
		
		Map<String, String> isoBuffer = new HashMap<>();
		loadProperties();
		try {
			System.out.println(" --- Unpack Started ---- ");
			int offset = 0;
			
			String msgType = message.substring(offset,offset+4);
			offset += 4;
			isoBuffer.put("MSG-TYP", msgType);
			System.out.println("MSG-TYP\t\t\t::\t"+ msgType);
			
			String primaryBitMap = message.substring(offset, offset+16);
			primaryBitMap = parseBitmap(primaryBitMap);
			String secondaryBitMap = "", teritaryBitMap = "";
			offset += 16;
			
			switch(primaryBitMap.charAt(0)) {
			
			case '1':
				secondaryBitMap = message.substring(offset, offset+16);
				secondaryBitMap = parseBitmap(secondaryBitMap);
				offset += 16;
				switch(secondaryBitMap.charAt(0)) {
				case '1':
					teritaryBitMap = message.substring(offset, offset+16);
					offset += 16;
					break;
				default:
					break;
				}
				break;
			default:
				break;
			}
			
			String bitMap = primaryBitMap + secondaryBitMap + teritaryBitMap;
			splitMessage(message, isoBuffer, bitMap, offset);
			
		}catch(Exception e) {
			System.out.println();
		}
		return isoBuffer;
	}
	
	protected void splitMessage(String message, Map<String, String> isoBuffer, String bitMap, int offset) {
		
		for(int i=0; i<bitMap.length(); i++) {
			
			int messageSize = 0;
			String data = "";
			char fieldSubElement = 'N';
			int lengthIndicator = 0;
			
			switch(bitMap.charAt(i)) {
			case '1':
				String propVal = String.valueOf(prop.getProperty("F-" + (i+1)));
				if(propVal!=null && !propVal.equals("null")) {
					String[] fieldProp = propVal.split(",");
					messageSize = Integer.parseInt(fieldProp[0]);
					fieldSubElement = fieldProp[1].charAt(0);
				}
				if (fieldSubElement == 'S') {
					
					lengthIndicator = Integer.parseInt(message.substring(offset, offset+Math.abs(messageSize)));
					offset += Math.abs(messageSize);
					//data = message.substring(offset, offset+lengthIndicator);
					offset = splitSubElementMessage(message, isoBuffer, offset, i+1, lengthIndicator);
				} 
				else {
						if(messageSize < 0) {
							lengthIndicator = Integer.parseInt(message.substring(offset, offset+Math.abs(messageSize)));
							offset += Math.abs(messageSize);
							data = message.substring(offset, offset+lengthIndicator);
							offset += lengthIndicator;
							isoBuffer.put("F-" + (i+1), data);
						}else {
							data = message.substring(offset, offset+messageSize);
							offset += messageSize;
							isoBuffer.put("F-" + (i+1), data);
						}
						System.out.println("F-" + (i + 1) + "\t\t\t::\t"+ data);
				}
				break;
			case '0':
				break;
			default:
				break;
			}
		}
	}

	protected int getBitmapFieldAtt(final Map<String, String> isoBuffer,
			StringBuffer fieldMessage, StringBuffer finalbitMap) {
		
		int bitMapLen = 0;
		StringBuffer bitMap = new StringBuffer();

		try {
			for (int i = 0; i < 192; i++) {
				
				String bitDetails = "";
				int messageSize = 0;
				char fieldSubElement = 'N';
				String data = "";

				bitDetails = (String) isoBuffer.get("F-" + (i + 1));

				if (bitDetails!=null) {
					//if((i + 1) != 35 && (i+1) != 14 && (i+1) != 2){
						System.out.println("F-" + (i + 1) + "\t\t\t::\t"+ bitDetails);
					//}

					bitMap.append("1");

					data = bitDetails;
					String propVal = String.valueOf(prop.getProperty("F-" + (i+1)));
					if(propVal!=null && !propVal.equals("null")) {
						String[] fieldProp = propVal.split(",");
						messageSize = Integer.parseInt(fieldProp[0]);
						fieldSubElement = fieldProp[1].charAt(0);
					}
					
					if (fieldSubElement == 'S') {
						
						String subElement = getSubBitmapFieldAtt(isoBuffer, (i + 1 + ""));
						fieldMessage.append(calculateLength(subElement.length(), Math.abs(messageSize))); 
						fieldMessage.append(subElement);
					} 
					else {
							if(messageSize < 0) {
								fieldMessage.append(calculateLength(data.length(), Math.abs(messageSize)));
							}

							fieldMessage.append(data);

							/*
							 * case 'B':
							 * 
							 * String asciivalue = null;
							 * 
							 * for (int j = 0; j < data.length(); j++) { if
							 * (IsoUtils.noSplChars(data.charAt(j))) { data = data.replaceAll(data.charAt(j)
							 * + "", IsoUtils.alpha2Hex(data.charAt(j) + "")); } }
							 * 
							 * asciivalue = IsoUtils.binary2hex(IsoUtils .hex2binary(data));
							 * 
							 * fieldMessage.append(calculateLength(data.length(), messageSize,
							 * fieldLengthDataType));
							 * 
							 * fieldMessage.append(asciivalue); break;
							 * 
							 * case 'S':
							 * 
							 * fieldMessage.append(calculateLength(data.length(), messageSize,
							 * fieldLengthDataType)); fieldMessage.append(new String(data)); break;
							 * 
							 * case 'E': String ebLen = calculateLength(data.length(), messageSize,
							 * fieldLengthDataType); String ebData = IsoUtils.alpha2Hex(new String(data
							 * .getBytes("cp037"), StandardCharsets.ISO_8859_1));
							 * fieldMessage.append(ebLen); fieldMessage.append(ebData); break;
							 */					//}
						} 
				}
				else {
					bitMap.append("0");
				}
			} 
			System.out.println("Data in string representation: "+fieldMessage);

			// **************** Bitmap formatting starts*********************

			String primaryBitMap = bitMap.substring(0, 64);
			String secondaryBitMap = bitMap.substring(64, 128);
			String tertiaryBitMap = bitMap.substring(128, 192);

			finalbitMap.append(primaryBitMap.toString());

			bitMapLen = 16;

			if (!checkZero(secondaryBitMap)) {
				finalbitMap.setCharAt(0, '1');
				finalbitMap.append(secondaryBitMap);
				bitMapLen = 32;
			}
			// **************** Bitmap formatting ends*********************
		}catch (Exception e) {
			throw new RuntimeException(
					"Problem in Bitmap and filed attribute formatting ");
		} finally {
			bitMap = null;
		}
		return bitMapLen;
	}

	protected boolean checkZero(String bitsdetail) {
		boolean flag = true;
		try {
			if (bitsdetail.contains("1"))
				flag = false;
		} catch (Exception e) {
			System.out.println("Error in checkZero:" + e.getMessage());
		}
		return flag;
	}

	private static String calculateLength(int dataLength, int varLength) {

		String strLength = String.valueOf(dataLength);

		while (strLength.length() < varLength)
			strLength = "0" + strLength;

		return strLength;
	}

	public String getSubBitmapFieldAtt(final Map<String, String> isoBuffer, String bitPos) {

		String bitDetails = "";
		StringBuffer fieldMessage = new StringBuffer();
		String data = "";
		int fieldlength = 100;

		try {

			for (int i = 0; i < fieldlength; i++) {
				
				String subElementPos = "";
				if(i+1 < 10)
					subElementPos = "0"+ (i+1);
				else
					subElementPos = String.valueOf(i+1);
				
				bitDetails = (String) isoBuffer.get("S-" + bitPos + "." + subElementPos);
				if (bitDetails != null) {

					System.out.println("S-" + bitPos + "." + subElementPos + "\t\t\t::\t" + bitDetails);
					data = subElementPos + bitDetails;
					
					String subElement = prop.getProperty("S-" + bitPos + "." + subElementPos);
					String[] subEleProps = subElement.split(",");
					int messageSize = Integer.parseInt(subEleProps[0]);
					fieldMessage.append(calculateLength(data.length(),Math.abs(messageSize)));
					fieldMessage.append(data);
				}
			}
			return fieldMessage.toString();

		} catch (Exception e) {
			throw new RuntimeException("Error occured Sub Element formatting");
		} finally {
			fieldMessage = null;
		}
	}
	
	public int splitSubElementMessage(String subElementData, Map<String, String> isoBuffer, int offset, int bitPos, int totalSubElementsLength) {
		
		if(bitPos == 48) {
			int subElementLength = 0;
			
			for(int i=0; i < totalSubElementsLength; i += subElementLength) {
				String pdsLength = subElementData.substring(offset, offset+2);
				int pdsLengthIndicator = Integer.parseInt(pdsLength);
				offset += 2;
				String tag = subElementData.substring(offset, offset+2);
				//int tagIndicator = Integer.parseInt(tag);
				offset += 2;
				int dataLength = pdsLengthIndicator - tag.length();
				String data = subElementData.substring(offset, offset+dataLength);
				offset += dataLength;
				subElementLength = pdsLength.length() + tag.length() + dataLength;
				isoBuffer.put("S-"+bitPos+"."+tag, data);
				System.out.println("S-" +bitPos+"."+ tag + "\t\t\t::\t"+ data);
			}
		}else if(bitPos == 55) {
			
		}else if(bitPos == 62) {
			
		}else if(bitPos == 63) {
			
		}else if(bitPos == 120) {
			
		}
		return offset;
	}
	
	protected static String parseBitmap(final String bitmap) {

		String upperBitmap = "00000000000000000000000000000000";
		String lowerBitmap = "00000000000000000000000000000000";

		upperBitmap += Long.toBinaryString(Long.parseLong(bitmap
				.substring(0, 8), 16));
		lowerBitmap += Long.toBinaryString(Long.parseLong(bitmap.substring(8),
				16));

		upperBitmap = upperBitmap.substring(upperBitmap.length() - 32);
		lowerBitmap = lowerBitmap.substring(lowerBitmap.length() - 32);

		return upperBitmap + lowerBitmap;
	}
}
