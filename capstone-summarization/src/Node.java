

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Node {
	String id;
	String word;
	Integer freq;
	Map<Integer,List<Integer>> sentencePos;
	//List<Node> children;
	//Set<String> childrenSet;
	Map<String,Node> childrenMap;
	Map<String,Double> edgeWeights;
	//List<Double> edgeWeights;
	Double distSource = Double.MAX_VALUE;
	Node parent;
	String posTag;
	String dependencyLabel;

	public Node(String id)
	{
		this.id = id;
		//this.children = new ArrayList<Node>();
		this.sentencePos = new HashMap<Integer,List<Integer>>();
		//this.edgeWeights = new ArrayList<Double>();
		this.edgeWeights = new HashMap<String,Double>();
		this.freq = 0;
		//this.childrenSet = new HashSet<String>();
		
		this.childrenMap = new HashMap<String,Node>();
	}

	public Node(String id,String word, Integer freq, String posTag,String dependencyLabel)
	{
		this.id = id;
		this.word = word;
		this.freq = freq;
		this.posTag = posTag;
		this.dependencyLabel = dependencyLabel;
		this.sentencePos = new HashMap<Integer,List<Integer>>();
		this.edgeWeights = new HashMap<String,Double>();
		//this.children = new ArrayList<Node>();
		//this.edgeWeights = new ArrayList<Double>();
		//this.childrenSet = new HashSet<String>();
		
		this.childrenMap = new HashMap<String,Node>();
	}

	public void computeWeights()
	{
		for(Node child:childrenMap.values())
		{
			double num = freq + child.freq;
			double denom = 0;
			int update = 0;
			for(Integer sIndex:child.sentencePos.keySet())
			{
				if(!sentencePos.containsKey(sIndex))
					continue;

				if(child.sentencePos.get(sIndex).isEmpty() || sentencePos.get(sIndex).isEmpty())
					continue;
				
				int index = 0;
				while(index<child.sentencePos.get(sIndex).size() && child.sentencePos.get(sIndex).get(index)<sentencePos.get(sIndex).get(0))
					index++;
					

				if(index<child.sentencePos.get(sIndex).size() && child.sentencePos.get(sIndex).get(index)>sentencePos.get(sIndex).get(0))
				{
					denom+=1/(child.sentencePos.get(sIndex).get(index) - sentencePos.get(sIndex).get(0) + 0.0);
					//denom+=(child.sentencePos.get(sIndex).get(index) - sentencePos.get(sIndex).get(0) + 0.0);
					update++;
				}
			}

			if(update==0)
				this.edgeWeights.put(child.id,0.0);
			else
				this.edgeWeights.put(child.id,(num/denom));
		}
	}

	public void incrFreq()
	{
		this.freq++;
	}

	public void addSentencePos(int sentence,int pos)
	{
		if(!this.sentencePos.containsKey(sentence))
			this.sentencePos.put(sentence, new ArrayList<Integer>());

		this.sentencePos.get(sentence).add(pos);
	}
	public void addChild(Node child)
	{
		/*
		if(!childrenSet.contains(child.id))
		{
			this.children.add(child);
			childrenSet.add(child.id);
		}*/
		
		if(!this.childrenMap.containsKey(child.id))
			this.childrenMap.put(child.id,child);
		
		
	}

}
