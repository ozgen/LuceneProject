package com.classifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.classification.ClassificationResult;
import org.apache.lucene.classification.SimpleNaiveBayesClassifier;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import com.ozgenEray.config.Configuration;

public final class TestClassifier {

	public static String[] CATEGORIES;
	private static int[][] counts;
	private static Map<String, Integer> catindex;

	public static void main(String[] args) throws Exception {
		final File folder = new File(Configuration.SOURCE_DIRECTORY_TO_INDEX);
		listFilesForFolder(folder);
		init();

		final long startTime = System.currentTimeMillis();
		SimpleNaiveBayesClassifier classifier = new SimpleNaiveBayesClassifier();
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(Configuration.INDEX_DIRECTORY)));
		AtomicReader ar = SlowCompositeReaderWrapper.wrap(reader);
		String field = "contents";
		classifier.train(ar, field, "meat", new StandardAnalyzer(Version.LUCENE_4_10_0));
		final int maxdoc = reader.maxDoc();
		for (int i = 0; i < maxdoc; i++) {
			Document doc = ar.document(i);
			String correctAnswer = doc.get("meat");
			final int cai = idx(correctAnswer);
			ClassificationResult<BytesRef> result = classifier.assignClass(doc.get(field));
			String classified = result.getAssignedClass().utf8ToString();
			final int cli = idx(classified);
			counts[cai][cli]++;
		}
		final long endTime = System.currentTimeMillis();
		final int elapse = (int) (endTime - startTime) / 1000;

		// print results
		int fc = 0, tc = 0;
		for (int i = 0; i < CATEGORIES.length; i++) {
			for (int j = 0; j < CATEGORIES.length; j++) {
				System.out.printf(" %3d ", counts[i][j]);
				if (i == j) {
					tc += counts[i][j];
				} else {
					fc += counts[i][j];
				}
			}
			System.out.println();
		}
		float accrate = (float) tc / (float) (tc + fc);
		float errrate = (float) fc / (float) (tc + fc);
		System.out.printf("\n\n*** accuracy rate = %f, error rate = %f; time = %d (sec); %d docs\n", accrate, errrate,
				elapse, maxdoc);

		reader.close();
	}

	static void init() {

		counts = new int[CATEGORIES.length][CATEGORIES.length];
		catindex = new HashMap<String, Integer>();
		for (int i = 0; i < CATEGORIES.length; i++) {
			catindex.put(CATEGORIES[i], i);
		}
	}

	static int idx(String cat) {
		int a = 0;
		if (catindex.get(cat) != null)
			a = catindex.get(cat);
		return a;
	}

	static HashSet<String> category = new HashSet<>();

	static void listFilesForFolder(final File folder) {

		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				listFilesForFolder(fileEntry);
			} else {
				BufferedReader br;
				try {
					br = new BufferedReader(new FileReader(fileEntry));
					String currentLine;
					int c = 0;
					while ((currentLine = br.readLine()) != null) {
						if (c == 1)
							category.add(currentLine);

						c++;

					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
		CATEGORIES = new String[category.size()];
		CATEGORIES = category.toArray(CATEGORIES);
	}

	static void fillCategories() {
		try {
			Files.walk(Paths.get(Configuration.SOURCE_DIRECTORY_TO_INDEX)).forEach(new Consumer<Path>() {

				public void accept(Path filePath) {
					if (Files.isRegularFile(filePath)) {
						try {
							File file = new File(filePath.toString());

						} catch (Exception e) {
							System.err.println(e);
						}
					}
				}
			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}