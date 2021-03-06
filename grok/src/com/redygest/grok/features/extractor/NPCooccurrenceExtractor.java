/**
 * 
 */
package com.redygest.grok.features.extractor;

import java.util.Arrays;
import java.util.List;

import com.redygest.commons.data.Data;
import com.redygest.commons.data.DataType;
import com.redygest.grok.features.computation.FeatureVectorCollection;
import com.redygest.grok.features.datatype.AttributeType;
import com.redygest.grok.features.datatype.Attributes;
import com.redygest.grok.features.datatype.DataVariable;
import com.redygest.grok.features.datatype.FeatureVector;
import com.redygest.grok.features.datatype.Variable;
import com.redygest.grok.knowledge.Event;
import com.redygest.grok.srl.Senna;
import com.redygest.grok.srl.Verb;

/**
 * @author semanticvoid
 * 
 */
public class NPCooccurrenceExtractor extends AbstractFeatureExtractor {
	
	private static Senna senna = new Senna(config.getSennaPath());

//	private List<SennaVerb> extractVerbs(String text) {
//		List<SennaVerb> verbs = new ArrayList<SennaVerb>();
//		// TODO replace with tokenized tweet
//		String[] lines = text.split("[:;'\"?/><,\\.!@#$%^&()-+=~`{}|]+");
//		for (String line : lines) {
//			if ((line = line.trim()).length() == 0)
//				continue;
//			String allLines = senna.getSennaOutput(line);
//			// System.out.println(allLines);
//			HashMap<String, SennaVerb> verbArgs = senna.parseSennaLines(
//					allLines, line);
//			for (String s : verbArgs.keySet()) {
//				SennaVerb verb = verbArgs.get(s);
//				verbs.add(verb);
//			}
//		}
//		return verbs;
//	}
	
	@Override
	public FeatureVectorCollection extract(List<Data> dataList) {
		FeatureVectorCollection features = new FeatureVectorCollection();
		for(Data t : dataList) {
			features.addGlobalFeatures(extract(t), true);
		}
		return features;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.redygest.grok.features.extractor.IFeatureExtractor#extract(com.redygest
	 * .commons.data.Tweet)
	 */
	@Override
	public FeatureVector extract(Data t) {
		FeatureVector fVector = new FeatureVector();
		List<Verb> verbs = senna.getVerbs((t.getValue(DataType.BODY)));

		for (Verb verb : verbs) {
			String[] args = verb
					.getArgumentToNPs()
					.keySet()
					.toArray(
							new String[verb.getArgumentToNPs().keySet().size()]);
			if (args.length == 0)
				continue;
			Arrays.sort(args);

			String head_arg = args[0];
			for (String headargNP : verb.getArgumentToNPs().get(head_arg)) {
				headargNP = headargNP.trim().toLowerCase();
				// TODO keep counts as well (not supported by the
				// framework
				// at the moment
				Variable var = fVector.getVariable(new DataVariable(headargNP,
						(long) FeatureVectorCollection.GLOBAL_IDENTIFIER));

				for (String arg : args) {
					for (String np : verb.getArgumentToNPs().get(arg)) {
						np = np.toLowerCase().trim();
						if (!np.equalsIgnoreCase(headargNP)) {
							// biRelations.incrementCount(headargNP, np, 1.0);
							if (var == null) {
								var = new DataVariable(headargNP, (long) FeatureVectorCollection.GLOBAL_IDENTIFIER);
							}

							Attributes attrs = var.getVariableAttributes();
							//if (!attrs.containsKey(np)) {
								attrs.put(AttributeType.NPCOOCCURENCE, np);
							//}
						}
					}
				}
				
				fVector.addVariable(var);
			}
		}

		return fVector;
	}

}
