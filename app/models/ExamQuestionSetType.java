package models;

public enum ExamQuestionSetType {
	
	FIXED, RANDOM;
	
	public String getName() {
		String tmp =  this.name();
		return tmp.charAt(0) + tmp.toLowerCase().substring(1).replaceAll("_", " ");
	}
}
