package utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import models.AnswerPaper;
import models.AnswerPaperStatus;
import models.Exam;

public class ReportData {
	
	public String key;
	public String value;
	
	public ReportData(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public static List<ReportData> getDataByPercent(Exam exam) {
		List<ReportData> listReportData = new ArrayList<ReportData>();
		List<AnswerPaper> listAnswerPaper = AnswerPaper.find("exam = ? and status = ?", exam, AnswerPaperStatus.SUBMITED).fetch();
		
		int total = listAnswerPaper.size();
		
		int interval = 10;
		Map<String, Integer> scoreBoard = new LinkedHashMap<String, Integer>();	
		for (int i = 0; i <= 100; i = i + interval) {
			scoreBoard.put(""+i, 0);
		}
		
		for (AnswerPaper answerPaper : listAnswerPaper) {
			int correctScore = answerPaper.numberOfCorrectAnswer;
			int totalMarks = answerPaper.questionList.size();
			
			double percent = 0;
			try { 
				percent = ((double)correctScore / totalMarks) * 100;
			}
			catch(ArithmeticException ae){
				percent = 0;
			}
		
			//System.out.println("percent " + percent + " score = " + correctScore + " total : " + totalMarks);
			
			String key = "" + ((int)percent/interval) * interval;
			if (scoreBoard.containsKey(key)) {
				Integer updateValue = (scoreBoard.get(key)) + 1;
				scoreBoard.put(key, updateValue);
			}
			else {
				//scoreBoard.put(key, 0);
			}
			
		}
		
		for (Entry<String, Integer> e : scoreBoard.entrySet()) {
			int fromRange = Integer.parseInt(e.getKey());
			int toRange = fromRange >= 100 ? 100 : (fromRange + interval); 
			listReportData.add(new ReportData(fromRange + "%>= Mark  <" + toRange + "%" , ""+(double)e.getValue()/total));
		}
		
		return listReportData;
	}	
	
}
