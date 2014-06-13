import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.Locale;

import com.fasterxml.uuid.impl.RandomBasedGenerator;


public class UuidGen {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String s = "1e-8";
		System.out.println(1+Double.valueOf(s));
		byte[] bs = {};
		System.out.println((new String(bs, Charset.forName("UTF-8"))).equals(""));
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMANY);
		System.out.println("haha: " + nf.getMaximumFractionDigits() + " || " + nf.getMaximumIntegerDigits());
		nf.setMaximumFractionDigits(17);
//		NumberFormat nf = NumberFormat.getNumberInstance();
		System.out.println("hehe: " + 1.0E-9 + " || " + nf.format(1.0E-9) + " || " + nf.format(1.0E-9).replaceAll(",", "."));
		RandomBasedGenerator j;
	}

}
