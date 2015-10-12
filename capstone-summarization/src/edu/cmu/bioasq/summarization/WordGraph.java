package edu.cmu.bioasq.summarization;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import com.google.gson.Gson;

import edu.emory.clir.clearnlp.component.AbstractComponent;
import edu.emory.clir.clearnlp.component.mode.pos.EnglishPOSTagger;
import edu.emory.clir.clearnlp.component.utils.NLPUtils;
import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.util.lang.TLanguage;


public class WordGraph {

	static Set<String> stopWords = new HashSet<String>();


	List<Summary> summaries;

	public void  getCompressedSentence(List<String> snippets)
	{
		Map<String,Node> map = new HashMap<String,Node>();
		summaries = new ArrayList<Summary>();
		map.put("S", new Node("S"));
		map.put("E", new Node("E"));
		int sentenceCount = 0;
		for(String snippet:snippets)
		{
			String[] arr = snippet.split(" ");
			List<String> tokens = new ArrayList<String>(Arrays.asList(arr));

			DEPTree tree = new DEPTree(tokens);
			for (AbstractComponent component: BioMedPOSTagger.getInstance().getBioinformaticsModels())
				component.process(tree);
			Node parent = map.get("S");
			Node end = map.get("E");

			for(int i=1;i<tree.size();i++)
			{
				String id = tree.get(i).getWordForm()+"_"+tree.get(i).getPOSTag();

				if(!map.containsKey(id))
				{
					Node node = new Node(id,tree.get(i).getWordForm(),0,tree.get(i).getPOSTag(),tree.get(i).getLabel());
					map.put(id, node);
				}


				map.get(id).incrFreq();
				map.get(id).addSentencePos(sentenceCount, i-1);



				parent.addChild(map.get(id));
				parent = map.get(id);



			}
			parent.addChild(end);
			sentenceCount++;
		}


		for(Node node:map.values())
		{
			//if("E".equals(node.id)||"E".equals(node.children.get(0).id))
			node.computeWeights();
		}

		//computeShortestPath(map);
		
		
		
		for(String snippet:snippets)
		{
			String[] arr = snippet.split(" ");
			List<String> tokens = new ArrayList<String>(Arrays.asList(arr));

			DEPTree tree = new DEPTree(tokens);
			BioMedPOSTagger.getInstance().getTagger().process(tree);
			Node parent = map.get("S");
			
			double weight = 0;
			
			for(int i=1;i<tree.size();i++)
			{
				String id = tree.get(i).getWordForm()+"_"+tree.get(i).getPOSTag();
				weight+= parent.edgeWeights.get(id);
				parent = parent.childrenMap.get(id);
			}
			
			summaries.add(new Summary(snippet,weight));
		}
		
		
		

		//computeShortestPaths(map);
		
		Collections.sort(summaries,new SummaryComparator() );
		
		/*
		for(int i=0;i<5;i++)
			printSummary(summaries.get(i).summary, summaries.get(i).weight);*/

		//return summaries;
	}


	private void computeShortestPath(Map<String, Node> map) {

		Node source = map.get("S");
		source.distSource = 0.0;

		PriorityQueue<Node> minHeap=new PriorityQueue<Node>(map.keySet().size(), new NodeComparator());
		minHeap.addAll(map.values());

		while(!minHeap.isEmpty())
		{
			Node u = minHeap.peek();
			int index = 0;
			for(Node child:u.childrenMap.values())
			{
				if(child.distSource>u.distSource + u.edgeWeights.get(child.id))
				{
					child.distSource = u.distSource + u.edgeWeights.get(child.id);
					child.parent = u;
				}
				index++;
			}
			minHeap.remove();
		}

		Node end = map.get("E").parent;
		List<String> summary = new ArrayList<String>();

		while(end!=null && end.id!=null && !"S".equals(end.id))
		{
			summary.add(end.word);
			end = end.parent;
		}

		Collections.reverse(summary);
		printSummary(summary,0.0);

	}
	public class NodeComparator implements Comparator<Node>
	{
		public int compare( Node node1, Node node2 )
		{
			return Double.compare(node1.distSource,node2.distSource);
		}
	}
	public class Summary
	{
		String summary;
		Double weight;

		public Summary(String summary,Double weight)
		{
			this.summary = summary;
			this.weight = weight;
		}
	}

	public class SummaryComparator implements Comparator<Summary>
	{

		@Override
		public int compare(Summary o1, Summary o2) {
			return Double.compare(o1.weight,o2.weight);
			//return Double.compare(o2.weight,o1.weight);
		}
	}
	private void computeShortestPaths(Map<String, Node> map) {

		Set<String> visited = new HashSet<String>();
		List<String> summary = new ArrayList<String>();


		Node start = map.get("S");

		int index = 0;
		for(Node child:start.childrenMap.values())
		{
			summary.add(child.word);
			//traverse(child,visited,summary,start.edgeWeights.get(index++));
			traverse(child,visited,summary,start.edgeWeights.get(child.id));
			summary.remove(0);
		}


	}


	private void traverse(Node node, Set<String> visited, List<String> summary, Double weight) {

		if(weight>150 ||summary.size()>70)
			return;

		if(summary.size()>30 && summary.size()<70 && weight>100 )
		{
			//printSummary(summary,weight);
			String sum = "";
			for(int i=0;i<summary.size()-1;i++)
				sum+=" " + summary.get(i);
			summaries.add(new Summary(sum,weight));
			return;
		}
		if("E".equals(node.id)||node.childrenMap.isEmpty())
			return;

		//if(!stopWords.contains(node.word))
		visited.add(node.id);

		int index = 0;
		for(Node child:node.childrenMap.values())
		{
			if(visited.contains(child.id))
				continue;
			summary.add(child.word);

			/*if("E".equals(child.id)||child.children.isEmpty())
				printSummary(summary,weight);
			else*/
			traverse(child,visited,summary,weight + node.edgeWeights.get(child.id));
			summary.remove(summary.size()-1);
		}

		visited.remove(node.id);

	}


	private void printSummary(String summary, Double weight) 
	{
		try
		{
			String filename= "summary.txt";
			FileWriter fw = new FileWriter(filename,true); //the true will append the new data
			//System.out.println(weight +sum);
			fw.write(weight +" "+summary+"\n\n");
			fw.close();
		}catch(Exception e)
		{

		}

	}

	private void printSummary(List<String> summary, Double weight) {
		try
		{
			String filename= "summary.txt";
			FileWriter fw = new FileWriter(filename,true); //the true will append the new data

			String sum = "";
			for(int i=0;i<summary.size()-1;i++)
				sum+=" " + summary.get(i);
			//System.out.println(weight +sum);
			fw.write(weight +sum+"\n");
			fw.close();
		}catch(Exception e)
		{

		}

	}


	public static void main(String args[]) throws IOException
	{
		loadStopWords();

		Gson gson = new Gson();
		
		//BufferedReader br = new BufferedReader( new FileReader("/Users/niloygupta/git/bioasq/src/main/resources/input/3b-dev.json"));
		BufferedReader br = new BufferedReader( new FileReader("/Users/niloygupta/Documents/Computational Data Science/Capstone/BioASQ-SampleDataB.json"));
		//BufferedReader br = new BufferedReader( new FileReader("/Users/niloygupta/Documents/Computational Data Science/Capstone/BioASQ-trainingDataset3b.json"));

		WordGraph graph = new WordGraph();

		Questions questions = gson.fromJson(br, Questions.class);
		String filename= "summary.txt";
		FileWriter fw = new FileWriter(filename); //the true will append the new data
		
		fw.write("===================\n");
		fw.close();
		double avgRouge = 0.0;
		for(Question question:questions.getQuestions())
		{
			//if(!"summary".equals(question.getType()))
				//continue;

			System.out.println("==========="+question.getId()+"========");
			System.out.println(question.getBody());
			System.out.println();
			System.out.println(question.getIdeal_answer());
			System.out.println();
			List<String> sentences = new ArrayList<String>();
			/*
			String summary = "";
			
			for(Snippet snippet:question.getSnippets())
				summary+=(snippet.getText()) + " ";
			double score = graph.computeRouge2(question.getIdeal_answer(),summary);
			avgRouge+= score;
			graph.printSummary(summary, graph.computeRouge2(question.getIdeal_answer(),summary));*/
			
			
			String summary = "";
			for(Snippet snippet:question.getSnippets())
				sentences.add(snippet.getText());
			graph.getCompressedSentence(sentences);
			for(int i=0;i<15 && i<graph.summaries.size();i++)
			{
				summary+=graph.summaries.get(i).summary;
				//double score =  graph.computeRouge2(question.getIdeal_answer(),graph.summaries.get(i).summary);
				//double score = graph.computeRouge2(question.getIdeal_answer(),summary);
				//avgRouge+= score;
				
				//graph.printSummary(graph.summaries.get(i).summary, graph.computeRouge2(question.getIdeal_answer(),graph.summaries.get(i).summary));
				//graph.printSummary(summary, graph.computeRouge2(question.getIdeal_answer(),summary));
			}
			
			double score = graph.computeRouge2(question.getIdeal_answer().get(0),summary);
			avgRouge+= score;
			graph.printSummary(summary, graph.computeRouge2(question.getIdeal_answer().get(0),summary));
				
			//break;	
		}
		System.out.println("Avg Rouge2: "+avgRouge/questions.getQuestions().size() );
		

	}

	public Double computeRouge1(String ideal,String summary)
	{
		String[] tokens = ideal.split(" ");
		double matches = 0;
		for(int i=0;i<tokens.length;i++)
		{
			String unigram = tokens[i];
			int index = summary.indexOf(unigram);
			if(index>=0)
				matches+=1.0;
		}
		
		return matches/(tokens.length);
	}
	
	public Double computeRouge2(String ideal,String summary)
	{
		String[] tokens = ideal.split(" ");
		double matches = 0;
		for(int i=1;i<tokens.length;i++)
		{
			String bigram = tokens[i-1]+" "+tokens[i];
			int index = summary.indexOf(bigram);
			if(index>=0)
				matches+=1.0;
		}
		
		return matches/(tokens.length-1);
	}
	
	public Double computeRouge4SU(String ideal,String summary)
	{
		ideal = ideal.toLowerCase();
		summary = summary.toLowerCase();
		
		String[] tokens = ideal.split(" ");
		double matches = 0;
		for(int i=1;i<tokens.length;i++)
		{
			//String bigram = tokens[i-1]+" "+tokens[i];
			int index1 = summary.indexOf(tokens[i-1]);
			int index2 = summary.indexOf(tokens[i]);
			if(index1>=0 && index2>=1 &&Math.abs(index2-index1)<=4)
				matches+=1.0;
		}
		
		return matches/(tokens.length-1);
	}

	private static void loadStopWords() {
		BufferedReader br = null;

		try {

			String sCurrentLine;

			br = new BufferedReader(new FileReader("/Users/niloygupta/Documents/Computational Data Science/Capstone/stopwords.txt"));

			while ((sCurrentLine = br.readLine()) != null) {
				stopWords.add(sCurrentLine);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

	}
}
