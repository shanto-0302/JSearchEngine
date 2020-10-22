package Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Date;
import java.util.Iterator;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;

public class LuceneFileSystemToRamDemo {

	public static final String FILES_TO_INDEX_DIRECTORY = "filesToIndex";
	public static final String INDEX_DIRECTORY = "indexDirectory";

	public static final String FIELD_PATH = "path";
	public static final String FIELD_CONTENTS = "contents";

	public static Directory fileSystemDirectory = null;
	public static Directory memoryDirectory = null;

	public static void main(String[] args) throws Exception {
		createFileSystemIndex();
		memoryDirectory = new RAMDirectory(fileSystemDirectory);
		doSearches(fileSystemDirectory);
		//doSearches(memoryDirectory);
	}

	public static void doSearches(Directory directory) throws IOException, ParseException {
		long start = new Date().getTime();
		searchIndex(directory, "mushrooms");
		searchIndex(directory, "steak");
		searchIndex(directory, "steak AND cheese");
		searchIndex(directory, "steak and cheese");
		searchIndex(directory, "bacon OR cheese");
		long end = new Date().getTime();
		System.out.println("TOTAL SEARCH TIME (using " + directory.getClass().getSimpleName() + ") in milliseconds:"
				+ (end - start));
	}

	public static void createFileSystemIndex() throws CorruptIndexException, LockObtainFailedException, IOException {
		Analyzer analyzer = new StandardAnalyzer();
		boolean recreateIndexIfExists = true;
		fileSystemDirectory = FSDirectory.getDirectory(INDEX_DIRECTORY);
		IndexWriter indexWriter = new IndexWriter(fileSystemDirectory, analyzer, recreateIndexIfExists);

		File dir = new File(FILES_TO_INDEX_DIRECTORY);
		File[] files = dir.listFiles();
		for (File file : files) {
			Document document = new Document();

			String path = file.getCanonicalPath();
			document.add(new Field(FIELD_PATH, path, Field.Store.YES, Field.Index.UN_TOKENIZED));

			Reader reader = new FileReader(file);
			document.add(new Field(FIELD_CONTENTS, reader));

			indexWriter.addDocument(document);
		}
		indexWriter.optimize();
		indexWriter.close();
	}

	public static void searchIndex(Directory directory, String searchString) throws IOException, ParseException {
		System.out.println("Searching for '" + searchString + "'");
		IndexSearcher indexSearcher = new IndexSearcher(directory);

		Analyzer analyzer = new StandardAnalyzer();
		QueryParser queryParser = new QueryParser(FIELD_CONTENTS, analyzer);
		Query query = queryParser.parse(searchString);
		Hits hits = indexSearcher.search(query);
		System.out.println("Number of hits: " + hits.length());

		Iterator<Hit> it = hits.iterator();
		while (it.hasNext()) {
			Hit hit = it.next();
			Document document = hit.getDocument();
			String path = document.get(FIELD_PATH);
			System.out.println("Hit: " + path);
		}

	}

}
