package models;

public enum AnswerPaperStatus {
	
	INACTIVE, ACTIVE, SUBMITED, CANCELED, TIMEOUT, INVITED_BY_EMAIL;

	public String getName() {
		String tmp =  this.name();
		return tmp.charAt(0) + tmp.toLowerCase().substring(1).replaceAll("_", " ");
	}
}
