package edu.cmu.bioasq.summarization;


import edu.emory.clir.clearnlp.component.AbstractComponent;
import edu.emory.clir.clearnlp.component.mode.dep.DEPConfiguration;
import edu.emory.clir.clearnlp.component.mode.dep.EnglishDEPParser;
import edu.emory.clir.clearnlp.component.mode.pos.EnglishPOSTagger;
import edu.emory.clir.clearnlp.component.utils.NLPUtils;
import edu.emory.clir.clearnlp.util.IOUtils;
import edu.emory.clir.clearnlp.util.lang.TLanguage;

public class BioMedPOSTagger {

	private static BioMedPOSTagger bioMedPOSTagger;
	AbstractComponent morph;
	private AbstractComponent pos;
	private AbstractComponent dep;
	private BioMedPOSTagger()
	{
		
		morph = NLPUtils.getMPAnalyzer(TLanguage.ENGLISH);
		pos =  NLPUtils.getPOSTagger(TLanguage.ENGLISH, "bioinformatics-en-pos.xz");
		dep =  NLPUtils.getDEPParser(TLanguage.ENGLISH, "bioinformatics-en-dep.xz",new DEPConfiguration("root"));
		//tagger = (EnglishPOSTagger) NLPUtils.getPOSTagger(TLanguage.ENGLISH, "bioinformatics-en-pos.xz");
		
	}
	
	public static BioMedPOSTagger getInstance()
	{
		if(bioMedPOSTagger==null)
			bioMedPOSTagger = new BioMedPOSTagger();
		return bioMedPOSTagger;
	}
	
	public AbstractComponent[] getBioinformaticsModels()
	{
		return new AbstractComponent[] { pos, morph, dep };
	}

	public AbstractComponent getTagger() {
		// TODO Auto-generated method stub
		return pos;
	}
	
	
}
