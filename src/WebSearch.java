import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.server.ExportException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Collections;
import org.jsoup.nodes.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


// You should call this code as follows:
//
//   java WebSearch directoryName searchStrategyName
//   (or jview, in J++)
//
//   where <directoryName> is the name of corresponding intranet
//   and <searchStrategyName> is one of {breadth, depth, best, beam}.

// The PARTIAL code below contains code for fetching and parsing
// the simple web pages we're using, as well as the fragments of
// a solution.  BE SURE TO READ ALL THE COMMENTS.

// Feel free to alter or discard whatever code you wish;
// the only requirement is that your main class be called WebSearch
// and that it accept the two arguments described above
// (if you wish you can add additional OPTIONAL arguments, but they
// should default to the values "hardwired" in below).

public class WebSearch 
{
	static LinkedList<SearchNode> OPEN; // Feel free to choose your own data structures for searching,
	static HashSet<String> CLOSED;      // and be sure to read documentation about them.
    static LinkedList<SearchNode> fifo = new LinkedList<SearchNode>();


    static final boolean DEBUGGING = false; // When set, report what's happening.
	// WARNING: lots of info is printed.

	static int beamWidth = 20; // If searchStrategy = "beam",
	// limit the size of OPEN to this value.
	// The setSize() method in the Vector
	// class can be used to accomplish this.


	static final String START_NODE     = "page1.html";

	static String START_NODE_WEB = "";

	// A web page is a goal node if it includes 
	// the following string.
	static final String GOAL_PATTERN   = "QUERY1 QUERY2 QUERY3 QUERY4";

	public static void main(String args[])
	{ 
		if (args.length != 2)
		{
			System.out.println("You must provide the directoryName and searchStrategyName.  Please try again.");
		}
		else
		{
			String directoryName = args[0]; // Read the search strategy to use.
			String searchStrategyName = args[1]; // Read the search strategy to use.

			if (searchStrategyName.equalsIgnoreCase("breadth") ||
					searchStrategyName.equalsIgnoreCase("depth")   ||
					searchStrategyName.equalsIgnoreCase("best")    ||
					searchStrategyName.equalsIgnoreCase("beam"))

			{
				performSearch(START_NODE, directoryName, searchStrategyName);
			}
			else if(searchStrategyName.equalsIgnoreCase("web")){
				try {
					Document doc = Jsoup.connect("https://en.wikipedia.org/wiki/Computer").get();
					System.out.println(doc.title());
					Elements newsHeadlines = doc.select("#mp-itn b a");
					System.out.println(doc.location());
					START_NODE_WEB = doc.location();
					performWebSearch(START_NODE_WEB, searchStrategyName);
				}catch(Exception e){

				}

			}
			else
			{
				System.out.println("The valid search strategies are:");
				System.out.println("  BREADTH DEPTH BEST BEAM WEB");
			}
		}

		Utilities.waitHere("Press ENTER to exit.");
	}

	static void performSearch(String startNode, String directoryName, String searchStrategy)
	{
		int nodesVisited = 0;

		OPEN   = new LinkedList<SearchNode>();
		CLOSED = new HashSet<String>();

		OPEN.add(new SearchNode(startNode));

		while (!OPEN.isEmpty())
		{
			SearchNode currentNode = pop(OPEN);
			String currentURL = currentNode.getNodeName();

			nodesVisited++;

			// Go and fetch the contents of this file.
			String contents = Utilities.getFileContents(directoryName
					+ File.separator
					+ currentURL);

			if (isaGoalNode(contents))
			{
                System.out.println(nodesVisited);


				// Report the solution path found
				// (You might also wish to write a method that
				// counts the solution-path's length, and then print that
				// number here.)
				currentNode.reportSolutionPath();
				break;
			}

			// Remember this node was visited.
			CLOSED.add(currentURL);

			addNewChildrenToOPEN(currentNode, contents, searchStrategy);

			// Provide a status report.
			if (DEBUGGING) System.out.println("Nodes visited = " + nodesVisited
					+ " |OPEN| = " + OPEN.size());
		}

		System.out.println(" Visited " + nodesVisited + " nodes, starting @" +
				" " + directoryName + File.separator + startNode +
				", using: " + searchStrategy + " search.");
	}

	static void performWebSearch(String startNode, String searchStrategy)
	{
		int nodesVisited = 0;

		OPEN   = new LinkedList<SearchNode>();
		CLOSED = new HashSet<String>();

		OPEN.add(new SearchNode(startNode));

		while (!OPEN.isEmpty())
		{
			SearchNode currentNode = pop(OPEN);
			String currentURL = currentNode.getNodeName();
			System.out.println("AAAAAAA");


			nodesVisited++;

			// Go and fetch the contents of this file.
			String contents = fetchContents(startNode);



			if (isaWebGoalNode(contents))
			{
				System.out.println(nodesVisited);

				currentNode.reportSolutionPath();
				break;
			}

			// Remember this node was visited.
			CLOSED.add(currentURL);

			addNewChildrenToOPEN(currentNode, contents, "breadth");

			// Provide a status report.
			if (DEBUGGING) System.out.println("Nodes visited = " + nodesVisited
					+ " |OPEN| = " + OPEN.size());
		}

//		System.out.println(" Visited " + nodesVisited + " nodes, starting @" +
//				" " + directoryName + File.separator + startNode +
//				", using: " + searchStrategy + " search.");
	}

	// This method reads the page's contents and
	// collects the 'children' nodes (ie, the hyperlinks on this page).
	// The parent node is also passed in so that 'backpointers' can be
	// created (in order to later extract solution paths).
	static void addNewChildrenToOPEN(SearchNode parent, String contents, String searchStrategy)
	{
        // StringTokenizer's are a nice class built into Java.
		// Be sure to read about them in some Java documentation.
		// They are useful when one wants to break up a string into words (tokens).
		StringTokenizer st = new StringTokenizer(contents);
		if(searchStrategy.equalsIgnoreCase("breadth")){
			st = new StringTokenizer(contents, "<a");
		}
//		System.out.println("contents = " + contents);
//		System.out.println("tokens = " + st.countTokens());
		while (st.hasMoreTokens())
		{
			String token = st.nextToken();

			// Look for the hyperlinks on the current page.

			// (Lots a print statments and error checks are in here,
			// both as a form of documentation and as a debugging tool should you
			// create your own intranets.)

			// At the start of some hypertext?  Otherwise, ignore this token.
			if (token.equalsIgnoreCase("<A"))
			{
				String hyperlink; // The name of the child node.

				if (DEBUGGING) System.out.println("Encountered a HYPERLINK");

				// Read: HREF = page#.html >

				token = st.nextToken();
				if (!token.equalsIgnoreCase("HREF"))
				{
					System.out.println("Expecting 'HREF' and got: " + token);
				}

				token = st.nextToken();
				if (!token.equalsIgnoreCase("="))
				{
					System.out.println("Expecting '=' and got: " + token);
				}

				// Now we should be at the name of file being linked to.
				hyperlink = st.nextToken();
				if (!hyperlink.startsWith("page"))
				{
					System.out.println("Expecting 'page#.html' and got: " + hyperlink);
				}

				token = st.nextToken();
				if (!token.equalsIgnoreCase(">"))
				{
					System.out.println("Expecting '>' and got: " + token);
				}

				if (DEBUGGING) System.out.println(" - found a link to " + hyperlink);

				//////////////////////////////////////////////////////////////////////
				// Have collected a child node; now have to decide what to do with it.
				//////////////////////////////////////////////////////////////////////

				if (alreadyInOpen(hyperlink))
				{ // If already in OPEN, we'll ignore this hyperlink
					// (Be sure to read the "Technical Note" below.)
					if (DEBUGGING) System.out.println(" - this node is in the OPEN list.");
				}
				else if (CLOSED.contains(hyperlink))
				{ // If already in CLOSED, we'll also ignore this hyperlink.
					if (DEBUGGING) System.out.println(" - this node is in the CLOSED list.");
				}
				else 
				{ // Collect the hypertext if this is a previously unvisited node.
					// (This is only needed for HEURISTIC SEARCH, but collect in
					// all cases for simplicity.)
					String hypertext = ""; // The text associated with this hyperlink.

					do
					{
						token = st.nextToken();
						if (!token.equalsIgnoreCase("</A>")) hypertext += " " + token;
					}
					while (!token.equalsIgnoreCase("</A>"));

					if (DEBUGGING) System.out.println("   with hypertext: " + hypertext);
					if(searchStrategy.equalsIgnoreCase("breadth")){

						SearchNode node = new SearchNode(hyperlink);

						node.generatePath(parent);
						System.out.println("AAAAAAA");

						OPEN.add(node); //this gives correct nodes visited

                    }
					else if(searchStrategy.equalsIgnoreCase("depth")){

						SearchNode node = new SearchNode(hyperlink);

						node.generatePath(parent);
//						System.out.println(node.getNodePath());

						OPEN.addFirst(node); //this gives correct nodes visited

					}
					else if(searchStrategy.equalsIgnoreCase("best")){
						SearchNode node = new SearchNode(hyperlink);
						int bonus = 0;

						node.generatePath(parent);

						bonus = count(hypertext,"QUERY");

						node.sethScore(bonus*10 + hypertext.length());
//						OPEN.clear();
						OPEN.add(node);
						Collections.sort(OPEN); //not sorting correctly since it goes to 18 first
//						for(SearchNode tempnode: OPEN){
//							System.out.print(tempnode.getNodeName() + " ");
//							System.out.print(tempnode.gethScore()+ " ");
//						}
//						System.out.println(node.getNodeName());
//						System.out.println(node.gethScore());


//						System.out.println("hyperlink = "+ hyperlink);
					}
					else if(searchStrategy.equalsIgnoreCase("beam")){
						SearchNode node = new SearchNode(hyperlink);
						int bonus = 0;

						node.generatePath(parent);

						bonus = count(hypertext,"QUERY");

						node.sethScore(bonus*10 + hypertext.length());

						OPEN.add(node);

						Collections.sort(OPEN); //not sorting correctly since it goes to 18 first
						if(OPEN.size() > beamWidth){
							OPEN.removeLast();
							System.out.println("size = " + OPEN.size());

						}
					}
					//////////////////////////////////////////////////////////////////////
					// At this point, you have a new child (hyperlink) and you have to
					// insert it into OPEN according to the search strategy being used.
					// Your heuristic function for best-first search should accept as 
					// arguments both "hypertext" (ie, the text associated with this 
					// hyperlink) and "contents" (ie, the full text of the current page).
					//////////////////////////////////////////////////////////////////////

					// Technical note: in best-first search,
					// if a page contains TWO (or more) links to the SAME page,
					// it is acceptable if only the FIRST one is inserted into OPEN,
					// rather than the better-scoring one.  For simplicity, once a node
					// has been placed in OPEN or CLOSED, we won't worry about the
					// possibility of later finding of higher score for it.
					// Since we are scoring the hypertext POINTING to a page,
					// rather than the web page itself, we are likely to get
					// different scores for given web page.  Ideally, we'd
					// take this into account when sorting OPEN, but you are
					// NOT required to do so (though you certainly are welcome
					// to handle this issue).

					// HINT: read about the insertElementAt() and addElement()
					// methods in the Vector class.
				}
			}

		}
	}

	public static int count(String str, String target) {
		return (str.length() - str.replace(target, "").length()) / target.length();
	}
	public static String fetchContents(String startNode) {
		try {
			Document doc = Jsoup.connect(startNode).get();
			Elements link = doc.select("a");

//			String links = link.attr("href");
//			links = links.replace("a", "FISH");
			System.out.println(link.toString());
			return link.toString();
		}catch(Exception e){

		}

		return null;
	}

	// A GOAL is a page that contains the goalPattern set above.
	static boolean isaGoalNode(String contents)
	{
		return (contents != null && contents.indexOf(GOAL_PATTERN) >= 0);
	}

	static boolean isaWebGoalNode(String contents)
	{
		return (contents != null && contents.indexOf("Deep Learning - Wikipedia") >= 0);
	}

	// Is this hyperlink already in the OPEN list?
	// This isn't a very efficient way to do a lookup,
	// but its fast enough for this homework.
	// Also, this for-loop structure can be
	// be adapted for use when inserting nodes into OPEN
	// according to their heuristic score.
	static boolean alreadyInOpen(String hyperlink)
	{
		int length = OPEN.size();

		for(int i = 0; i < length; i++)
		{
			SearchNode node = OPEN.get(i);
			String oldHyperlink = node.getNodeName();

			if (hyperlink.equalsIgnoreCase(oldHyperlink)) return true;  // Found it.
		}

		return false;  // Not in OPEN.    
	}

	// You can use this to remove the first element from OPEN.
	static SearchNode pop(LinkedList<SearchNode> list)
	{
		SearchNode result = list.removeFirst();




		return result;
	}
}

/////////////////////////////////////////////////////////////////////////////////

// You'll need to design a Search node data structure.

// Note that the above code assumes there is a method called getHvalue()
// that returns (as a double) the heuristic value associated with a search node,
// a method called getNodeName() that returns (as a String)
// the name of the file (eg, "page7.html") associated with this node, and
// a (void) method called reportSolutionPath() that prints the path
// from the start node to the current node represented by the SearchNode instance.
class SearchNode implements Comparable<SearchNode>
{
	final String nodeName;
	private String path; //path = start node plus parent path + name
	private int pathLength;
	private int hScore;


	public SearchNode(String name) {
		nodeName = name;
		hScore = 0;
		path = "";
	}

	public void sethScore(int score){
		hScore = score;
	}

	public int gethScore(){
		return hScore;
	}




	@Override
	public int compareTo(SearchNode node) {
		int comparedLength = node.hScore;
		if (this.hScore > comparedLength) {
			return -1;
		} else if (this.hScore == comparedLength) {
			return 0;
		} else {
			return 1;
		}
	}


	public void generatePath(SearchNode parent) {
		if(parent.getNodePath().isEmpty()){
			path = "page1.html->" + nodeName;
			pathLength=1;
		}else{
			path =  parent.getNodePath() + "->" + nodeName;
			pathLength = parent.pathLength + 1;
		}
	}

	public String getNodePath(){

		return path;
	}
	public void reportSolutionPath() {
		System.out.println("SOLUTION = " + path);
		System.out.println("path length = " + pathLength);
	}

	public String getNodeName() {
		return nodeName;
	}




}

/////////////////////////////////////////////////////////////////////////////////

// Some 'helper' functions follow.  You needn't understand their internal details.
// Feel free to move this to a separate Java file if you wish.
class Utilities
{
	// In J++, the console window can close up before you read it,
	// so this method can be used to wait until you're ready to proceed.
	public static void waitHere(String msg)
	{
		System.out.println("");
		System.out.println(msg);
		try { System.in.read(); } catch(Exception e) {} // Ignore any errors while reading.
	}

	// This method will read the contents of a file, returning it
	// as a string.  (Don't worry if you don't understand how it works.)
	public static synchronized String getFileContents(String fileName)
	{
		File file = new File(fileName);
		String results = null;

		try
		{
			int length = (int)file.length(), bytesRead;
			byte byteArray[] = new byte[length];

			ByteArrayOutputStream bytesBuffer = new ByteArrayOutputStream(length);
			FileInputStream       inputStream = new FileInputStream(file);
			bytesRead = inputStream.read(byteArray);
			bytesBuffer.write(byteArray, 0, bytesRead);
			inputStream.close();

			results = bytesBuffer.toString();
		}
		catch(IOException e)
		{
			System.out.println("Exception in getFileContents(" + fileName + "), msg=" + e);
		}

		return results;
	}
}
