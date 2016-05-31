package com.ozgenEray.cosinesimilarity;

import java.io.IOException;
import org.apache.lucene.store.LockObtainFailedException;

/**
 * Main Class
 */
public class Test {

	public static void main(String[] args) throws LockObtainFailedException, IOException {
		getCosineSimilarity();
	}

	public static void getCosineSimilarity() throws LockObtainFailedException, IOException {
		Indexer index = new Indexer();
		index.index();
		VectorGenerator vectorGenerator = new VectorGenerator();
		vectorGenerator.GetAllTerms();
		DocVector[] docVector = vectorGenerator.GetDocumentVectors(); // getting
																		// document
																		// vectors
		for (int i = 0; i < docVector.length; i++) {
			
			for (int j = 0; j < docVector.length; j++) {
				double cosineSimilarity = CosineSimilarity.CosineSimilarity(docVector[i], docVector[j]);
				System.out.println(
						"Cosine Similarity Score between document " + i + " " + j + "  = " + cosineSimilarity);
			}

		}
	}
}