

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.stanford.nlp.util.StringUtils;

import com.google.gson.Gson;

import edu.emory.clir.clearnlp.component.AbstractComponent;
import edu.emory.clir.clearnlp.dependency.DEPTree;

public class SummaryAnalysis {
	public static void main(String args[]) throws FileNotFoundException
	{
		Gson gson = new Gson();
		BufferedReader br = new BufferedReader( new FileReader("/Users/niloygupta/git/bioasq/src/main/resources/input/3b-dev.json.fixed"));


		Questions questions = gson.fromJson(br, Questions.class);

		for(Question question:questions.getQuestions())
		{
			if(!"summary".equals(question.getType()))
				continue;

			System.out.println("==========="+question.getId()+"========");
			/*
			System.out.println(question.getBody());
			System.out.println();
			System.out.println(question.getIdeal_answer());
			System.out.println();*/

			printDependencyLabels(question.getBody(),true);
			printDependencyLabels(question.getIdeal_answer().get(0),true);
			
		}

	}

	private static void printDependencyLabels(String sentence, boolean full) {
		List<String> tokens = StringUtils.split(sentence);

		DEPTree tree = new DEPTree(tokens);
		
		for (AbstractComponent component: BioMedPOSTagger.getInstance().getBioinformaticsModels())
			component.process(tree);
		int limit = tree.size();
		if(!full)
			limit = 10;
		
		for(int i=1;i<tree.size() && i<limit ;i++)
		{	
			System.out.print(tree.get(i).getWordForm()+"_"+tree.get(i).getLabel()+"_"+tree.get(i).getPOSTag()+" ");
		}

		System.out.println();
		
	}
}
