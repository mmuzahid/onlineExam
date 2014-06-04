package utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.ParamDef;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.sun.xml.internal.fastinfoset.util.StringArray;
import play.Logger;

public class ExtraUtils {

	/**
	 * @param dateString - the date string (format 'mm/dd/yyyy')
	 * @return date - java.util.Date
	 * */
	public static Date getAsDate(String dateString) {
		//convert mm/dd/yyyy to yyyy-mm-dd
    	String yyyy_mm_dd = dateString.split("/")[2]+"-"+dateString.split("/")[0]+"-"+dateString.split("/")[1];    	
		String time_zone_const = "T00:00:00.00Z";
		DateTimeFormatter dtf = ISODateTimeFormat.dateTime(); // ISO8601 (XML) Date/time
		
		return dtf.parseDateTime(yyyy_mm_dd+time_zone_const).toDate();	
	}
	
	/**
	 * @return Date - today's start time
	 * */
	public static Date getTodayStart(){
		 return getTodayPlus(0);
	}
	
	/**
	 * @param plusDay - number of days added with today
	 * @return Date - start time of (today + days)
	 * */
	public static Date getTodayPlus(int plusDay){
		Calendar now =new GregorianCalendar();
        Calendar start = new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        Date todayPlus = new Date(new DateTime(start.getTime()).plusDays(plusDay).getMillis());
        
        return todayPlus;
	}
	/**
	 * @param date - an input date
	 * @return Date - start time of {@link date}
	 * */
	public static Date startPoint(Date date){
		Calendar now =new GregorianCalendar();
		now.setTime(date);
		Calendar start = new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
	   
		return  new Date(new DateTime(start.getTime()).plusDays(0).getMillis());
	}

	/**
	 * convert a java.util.Date to a string and format is 'dd-mm-YYYY'. e.g. 08-05-2013 
	 * */
	public static String getDateString(Date date) {
		String dateString = "";
		if(date != null) {
			dateString = getDateString(date, "dd-mm-YYYY");
		}
		
		return dateString;
	}
	/**
	 * convert a java.util.Date to a string to given format. i.e. if format = 'dd-mm-YYYY' then dateString = 08-05-2013 
	 * */
	public static String getDateString(Date date, String format) {
		String dateString = "";
		if(date != null) {
			DateTime dt = new DateTime(date);
			int month = dt.getMonthOfYear();
			int day = dt.getDayOfMonth();
			int year = dt.getYearOfEra();
			
			format = format.toLowerCase();
			format = format.replace("dd", StringUtils.leftPad(""+day, 2, "0"));
			format = format.replace("mm", StringUtils.leftPad(""+month, 2, "0"));
			format = format.replace("yyyy", ""+year);
			
			dateString = new String(format);
		}
		
		return dateString;
	}

	public static Date toDate(String dateInString) {
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
		Date date = null;
		try {
			date = formatter.parse(dateInString);
			//System.out.println(date);
			//System.out.println(formatter.format(date));
	 
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return date;
	}
	
	public static String toDateString(Date date, String format){
		return new SimpleDateFormat(format).format(date != null ? date : new Date());
	}
}
