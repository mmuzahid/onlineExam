package models;

public enum ExamVisibility {
	PUBLIC , PRIVATE;
	
	public String getName() {
		String tmp =  this.name();
		return tmp.charAt(0) + tmp.toLowerCase().substring(1).replaceAll("_", " ");
	}
}
