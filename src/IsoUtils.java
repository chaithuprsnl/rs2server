
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class IsoUtils {

	public static boolean noSplChars(char val) {
		String iChars = "~`!@#$%^&*()+=[]\\\'{}|\"<>?";
			if (iChars.indexOf(val) != -1) {
	  			return true;
	  		}
	  	return false;
	}
	
	public static String alpha2Hex(String data)
	{
		char[] alpha = data.toCharArray();
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < alpha.length; i++)
		{
			int count = Integer.toHexString(alpha[i]).toUpperCase().length();
			if(count <= 1)
			{
				sb.append("0").append(Integer.toHexString(alpha[i]).toUpperCase());
			}
			else
			{
				sb.append(Integer.toHexString(alpha[i]).toUpperCase());
			}
		}
		return sb.toString();
	}
	
	public static String binary2hex(final String binaryString) {

		if (binaryString == null) {
			return null;
		}

		String hexString = "";

		for (int i = 0; i < binaryString.length(); i += 8) {

			String losgTemp = binaryString.substring(i, i + 8);

			int value = 0;

			for (int k = 0, j = losgTemp.length() - 1; j >= 0; j--, k++) {
				value += Integer.parseInt("" + losgTemp.charAt(j))
						* Math.pow(2, k);
			}
			losgTemp = "0" + Integer.toHexString(value);

			hexString += losgTemp.substring(losgTemp.length() - 2);
		}
		return hexString.toUpperCase();
	}
	
	public static String hex2binary(String hexString) {
		if (hexString == null) {
			return null;
		}		
		if (hexString.length() % 2 != 0) {
			hexString = "0" + hexString;
		}
		String binary = "";
		String temp = "";
		for (int i = 0; i < hexString.length(); i++) {
			
			temp = "0000"+ Integer.toBinaryString(Character.digit(hexString.charAt(i), 16));
			binary += temp.substring(temp.length() - 4);
		}
		return binary;
	}
	
	public static String padRightZero(StringBuffer s, int i)
    {
	   StringBuffer stringbuffer = new StringBuffer(i);
        int j = i - s.length();
        for(int k = 0; k < j; k++)
            stringbuffer.append("0");

        s.append(stringbuffer.toString());
        return s.toString();
    }
	
	public static String formatAmount(String amount) {
		int currencyFractionDigits = 2;
		double currencyFraction = Math.pow(10, currencyFractionDigits);
		NumberFormat nf = NumberFormat.getInstance();
		String formattedAmount = "";
		if(nf instanceof DecimalFormat) {
			DecimalFormat df = (DecimalFormat)nf;
			df.applyLocalizedPattern("000000000000");
			formattedAmount = df.format(Double.parseDouble(amount) * currencyFraction);
		}
		return formattedAmount;
	}
	
	public static String formatUTCDateTime() {
		LocalDateTime localDateTime = LocalDateTime.now(ZoneOffset.UTC);
		return localDateTime.format(DateTimeFormatter.ofPattern("MMddHHmmss"));
	}
	
	public static String formatLocalDate() {
		LocalDate localDate = LocalDate.now();
		return localDate.format(DateTimeFormatter.ofPattern("MMdd"));
	}
	
	public static String formatLocalTime() {
		LocalTime localTime = LocalTime.now();
		return localTime.format(DateTimeFormatter.ofPattern("HHmmss"));
	}
	
	//Generates six digit stan number
	public static String formatStan() {
		Random random = new Random();
		int number = random.nextInt(999999);
		return String.format("%06d", number);
	}
	
	public static String formatISOCurrencyCode(String currencyCode) {
		String isoCurrCode = "";
		switch(currencyCode.toLowerCase()) {
		case "usd":
			isoCurrCode = "840";
			break;
		case "inr":
			isoCurrCode = "356";
			break;
		default:
			break;
		}
		return isoCurrCode;
	}
}
