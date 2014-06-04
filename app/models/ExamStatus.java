package models;

public enum ExamStatus {

	ACTIVE, INACTIVE, CANCELED, EXPIRED;
	
	public String getName() {
		String tmp =  this.name();
		return tmp.charAt(0) + tmp.toLowerCase().substring(1).replaceAll("_", " ");
	}
}
