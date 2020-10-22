import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

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


public class UI implements ActionListener{
	
	public static final String FILES_TO_INDEX_DIRECTORY = "filesToIndex";
	public static final String INDEX_DIRECTORY = "indexDirectory";

	public static final String FIELD_PATH = "path";
	public static final String FIELD_CONTENTS = "";

	public static Directory fileSystemDirectory = null;
	public static Directory memoryDirectory = null;
	
	static Hits hits;
	static String path[]=new String[10000];
	static String pathNames[] = new String[10000];

	
	public String textFieldValue, textValue;
	JFrame frame = new JFrame("Loading/Saving Example");
	JPanel toppanel = new JPanel();
	JLabel welcome= new JLabel("Welcome to Lucen Search Engine");
	JTextField textField = new JTextField();
	 JButton browseButton = new JButton("Browse ");
	 JLabel Resultlabel;
	 JLabel ResultItemlabel;
	 JPanel Resultpanel = new JPanel();
	 JPanel mainPanel = new JPanel();
	public void guiCreation() {
		frame.setTitle("Lucen UI");
		frame.setSize(400, 400);
	       textField.setPreferredSize(new Dimension(100,40));
	       toppanel.setBorder(new EmptyBorder(2, 3, 2, 3));
	       
	       browseButton.addActionListener(this);
	       toppanel.setBackground( Color.blue );
	       
	       mainPanel.setLayout(new GridLayout(2, 3));
	       
	       Resultpanel.setBackground(Color.white);
	       
	       mainPanel.add( toppanel );
	       
	       toppanel.add(welcome);
	       toppanel.add(textField);
	       toppanel.add(browseButton);
	       
	       mainPanel.add(Resultpanel);
	       
	       Resultlabel = new JLabel();
	       ResultItemlabel = new JLabel();
		    Resultpanel.add(Resultlabel);
		    Resultpanel.add(ResultItemlabel);
		    
	       frame.add(mainPanel);
		    
	       frame.setLocationRelativeTo(null);
	       frame.setVisible(true);
	       frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		textFieldValue = textField.getText();
		long start = new Date().getTime();
		try {
			createFileSystemIndex();
			searchIndex(fileSystemDirectory, textFieldValue);
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//long end = new Date().getTime();
		show(textFieldValue);
		//System.out.println("TOTAL SEARCH TIME (using " + fileSystemDirectory.getClass().getSimpleName() + ") in milliseconds:"
			//	+ (end - start));
		
		
	//	Resultlabel.setText(arg0);
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
		
		
		IndexSearcher indexSearcher = new IndexSearcher(directory);

		Analyzer analyzer = new StandardAnalyzer();
		QueryParser queryParser = new QueryParser(FIELD_CONTENTS, analyzer);
		Query query = queryParser.parse(searchString);
		hits = indexSearcher.search(query);
		System.out.println("Number of hits: " + hits.length());
		int i=0;
		Iterator<Hit> it = hits.iterator();
		while (it.hasNext()) {
			Hit hit = it.next();
			Document document = hit.getDocument();
			path[i] = document.get(FIELD_PATH);
		
			//pathNames[i++]=path;
			System.out.println("Hit: " + path[i]);
			i++;
		}

	}
	public void show(String searchString) {
		// TODO Auto-generated method stub
		
		int pathNumber=hits.length();
		
		
		if(pathNumber>0)
		{
			for(int i = 0; i<pathNumber; i++) {
				{
					if(!(Arrays.asList(pathNames).contains(path[i])))
						{
							
							pathNames[i]=path[i];
							JLabel l= new JLabel("<html>Searching For " + searchString + "<br>Number of hits: "+ hits.length()+ "<br>" +" Path: " + pathNames[i]);
							System.out.println("shanto" +path );
							Resultpanel.add(l);
						}
						else
							continue;
				}
			}
		}
			
			else 
			{
				JLabel l= new JLabel("<html>Searching For " + searchString + "<br>Number of hits: "+ hits.length()+ "<br>");
				Resultpanel.add(l);
				
			}
		
	}

}
	
