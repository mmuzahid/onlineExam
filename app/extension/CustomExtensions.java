package extension;

import java.util.*;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.Seconds;

import play.templates.BaseTemplate.RawData;
import play.templates.JavaExtensions;
import utils.*;

public class CustomExtensions extends JavaExtensions{

	public static String removeNewLine(RawData inS){
		String temp;

		temp = inS.toString().replace("\n", "");
		return temp;
	}

	public static String removeNewLine(String inS){
		String temp;

		temp = inS.replace("\n", "");
		return temp;
	}

	public static String diff(Date base, Date comp) {
		DateTime end = new DateTime(base);
		DateTime start = new DateTime(comp);
		int mins = Minutes.minutesBetween(end, start).getMinutes();
		int secs = Seconds.secondsBetween(end, start).minus(mins*60).getSeconds();
		return (mins > 0 ? mins + " Minutes and " : "") + secs + " Seconds";
	}
}
