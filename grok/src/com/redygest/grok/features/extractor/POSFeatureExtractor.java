package com.redygest.grok.features.extractor;

import java.util.ArrayList;
import java.util.List;

import com.redygest.commons.data.Data;
import com.redygest.commons.data.DataType;
import com.redygest.grok.features.computation.Features;
import com.redygest.grok.features.datatype.AttributeType;
import com.redygest.grok.features.datatype.Attributes;
import com.redygest.grok.features.datatype.DataVariable;
import com.redygest.grok.features.datatype.FeatureVector;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.Tree;

public class POSFeatureExtractor extends AbstractFeatureExtractor {

	private static final LexicalizedParser parser = new LexicalizedParser(
			"/Users/semanticvoid/projects/reDygest/grok/data/englishPCFG.ser.gz");

	@Override
	public Features extract(List<Data> dataList) {
		Features features = new Features();
		// TODO use record identifier in the Tweet class
		int recordIdentifier = 0;

		for (Data d : dataList) {
			FeatureVector fVector = new FeatureVector();
			// TODO use the tokenized data
			parser.parse(d.getValue(DataType.BODY));
			Tree t = parser.getBestParse();
			List<String> tags = getPOSTags(t);
			for (String tag : tags) {
				String[] tokens = tag.split("::");
				DataVariable var = new DataVariable(tokens[0],
						(long) recordIdentifier);
				Attributes attrs = var.getVariableAttributes();
				attrs.put(tokens[1], AttributeType.POS);
				fVector.addVariable(var);
			}
			
			features.addGlobalFeatures(fVector, true);
			recordIdentifier++;
		}

		return features;
	}

	private List<String> getPOSTags(Tree tree) {
		Tree root = tree;
		List<String> tags = new ArrayList<String>();
		ArrayList<Tree> queue = new ArrayList<Tree>();
		queue.add(tree);

		while (queue.size() > 0) {
			Tree node = queue.remove(0);

			if (!node.isLeaf() && node.firstChild().isLeaf()) {
				tags.add(node.firstChild() + "::" + node.value());
			}

			List<Tree> children = node.getChildrenAsList();
			for (Tree child : children) {
				queue.add(child);
			}
		}

		return tags;
	}

	@Override
	public FeatureVector extract(Data t) {
		// TODO Auto-generated method stub
		return null;
	}

}
