package edu.cmu.bioasq.summarization;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.util.StringUtils;

import com.google.gson.Gson;

import edu.emory.clir.clearnlp.component.AbstractComponent;
import edu.emory.clir.clearnlp.dependency.DEPTree;

public class QuestionAnalysis {
	
	public static Map<String,Map<String,Ans>> qMap = new HashMap<String,Map<String,Ans>>();
	public static void main(String args[]) throws FileNotFoundException
	{
		QuestionAnalysis qAnalysis = new QuestionAnalysis();
		Gson gson = new Gson();
		BufferedReader br = new BufferedReader( new FileReader("/Users/niloygupta/git/bioasq/src/main/resources/input/3b-dev.json.fixed"));


		Questions questions = gson.fromJson(br, Questions.class);

		for(Question question:questions.getQuestions())
		{
			if(!"summary".equals(question.getType()))
				continue;

			//System.out.println("==========="+question.getId()+"========");
			/*
			System.out.println(question.getBody());
			System.out.println();
			System.out.println(question.getIdeal_answer());
			System.out.println();*/

			String qLabel = getQLabel(question.getBody());
			List<String> aLabels = getALabel(question.getIdeal_answer().get(0));
			
			qAnalysis.addToMap(qLabel,aLabels);
			
			
			
		}
		
		qAnalysis.printMap();
		

	}

	private void printMap() {
		for(String qLabel:qMap.keySet())
		{
			List<Ans> aLabels = new ArrayList<Ans>(qMap.get(qLabel).values());
			
			Collections.sort(aLabels, new AnsComparator());
			
			System.out.println(qLabel);
			for(Ans aLabel:aLabels)
				System.out.print(aLabel.ansLabel+":"+aLabel.count+" ");
			System.out.println();
		}
		
	}
	
	public class AnsComparator implements Comparator<Ans>
	{
		
		@Override
		public int compare(Ans o1, Ans o2) {
			return Double.compare(o2.count,o1.count);
			
		}
	}
	

	private void addToMap(String qLabel, List<String> aLabels) {
		if(!qMap.containsKey(qLabel))
			qMap.put(qLabel, new HashMap<String,Ans>());
		
		for(String aLabel:aLabels)
		{
			if(!qMap.get(qLabel).containsKey(aLabel))
				qMap.get(qLabel).put(aLabel,new Ans(aLabel, 0));
			qMap.get(qLabel).get(aLabel).setCount(qMap.get(qLabel).get(aLabel).getCount()+1);
		}
		
	}

	private static List<String> getALabel(String sentence) {
		List<String> tokens = StringUtils.split(sentence);
		List<String> aLabels = new ArrayList<String>();
		DEPTree tree = new DEPTree(tokens);
		
		for (AbstractComponent component: BioMedPOSTagger.getInstance().getBioinformaticsModels())
			component.process(tree);
		
		for(int i=1;i<tree.size() && i<=3 ;i++)
			aLabels.add( tree.get(i).getLabel());
		return aLabels;
	}

	private static String getQLabel(String sentence) {
		List<String> tokens = StringUtils.split(sentence);

		DEPTree tree = new DEPTree(tokens);
		
		for (AbstractComponent component: BioMedPOSTagger.getInstance().getBioinformaticsModels())
			component.process(tree);

		return tree.get(1).getPOSTag();
	}

	
	public class Ans implements Comparator<Ans>
	{
		
		String ansLabel;
		int count;
		
		public Ans(String ansLabel,int count)
		{
			this.ansLabel = ansLabel;
			this.count = count;
		}
		
		public int getCount() {
			return count;
		}

		public void setCount(int count) {
			this.count = count;
		}

		@Override
		public int compare(Ans o1, Ans o2) {
			
				return Integer.compare(o2.count,o1.count);
				
			
		}

	}
}
