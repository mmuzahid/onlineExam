package models;

public enum ExamDurationType {
	
	NO_TIME_LIMIT, FIXED_TIME_LIMIT;
	
	public String getName() {
		String tmp =  this.name();
		return tmp.charAt(0) + tmp.toLowerCase().substring(1).replaceAll("_", " ");
	}
}
