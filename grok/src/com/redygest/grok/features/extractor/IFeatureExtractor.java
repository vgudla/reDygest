/**
 * 
 */
package com.redygest.grok.features.extractor;

import java.util.List;

import com.redygest.commons.data.Data;
import com.redygest.grok.features.computation.FeatureVectorCollection;
import com.redygest.grok.features.repository.IFeaturesRepository;

/**
 * Interface for feature extractor
 * @author semanticvoid
 *
 */
public interface IFeatureExtractor {

	public FeatureVectorCollection extract(List<Data> dataList);
		
	public IFeaturesRepository getFeaturesRepository();
	
}
