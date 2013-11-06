package crawler;

//////////////////////////////////////////////////////////////////////////////////////////////
//	Course:	17630 - Computer Science Principles for Practicing Engineers
//	Project:The Web Crawler
//	Author:	Anthony J. Lattanze
//	Version:1.0 - July 1, 2008
//
//	Purpose:This file containts the classes for a very basic web crawler that illustrates the
//			basic concepts of URL parsing, "robot safe", page processing, and fundamental
//		    web crawling. Note that this program should not incorperate malicious elements and
//			ALL STUDENTS MUST IMPLEMENT THE ROBOT SAFE PROTOCOL to respect the crawling wishes
//			of site administrators.
//
//	Compilation: javac CrawlerEngine.java
//	Usage: java CrawlerEngine <starting URL> [number of pages]
//		   The web crawler will start the crawl at <starting URL> and will download up to
//		   [number of pages]. The [number of pages] argument is optional.
//
//	Internal Methods:
//
//		cboolean Initialize( String[] s );
//		RobotSafe(URL u);
//		AddNewUrl(URL u, String s);
//		LoadPage(URL u);
//		Crawl(String[] s );
//////////////////////////////////////////////////////////////////////////////////////////////
import gui.Canvas;
import java.util.*;
import java.net.*;
import java.io.*;

public class CrawlerEngine extends Thread {
    // Global Variables

    private SearchT st = new SearchT();
    public String results = new String();
    public static final int MAX_PAGES = 20;		// Default maxiumum pages
    public static final boolean DEBUG = false;		// This can be used to enable or disable
    // debug messages
    public static final String DISALLOW = "Disallow:";					// String used in RobotSafe method to
    // determine if page crawling is disallowed
    public static final int MAX_FILE_SIZE = 20000;	// Max size of file
    // Global Data Structures
    Vector newURLs;			// This is a list of URLs to be searched
    Hashtable knownURLs;		// This table contains the list of known URLs
    int maxPages;		        // This is the maximum number of pages to crawl

    ///////////////////////////////////////////////////////////////////////////
    //	METHOD:: Initialize(String[] argv)
    //	Arguments:
    //		String[] argv - this are the command line arguments passed directly
    //						from main().
    //
    //	Purpose: This method initializes program variables and key data structures
    //			 based on the command line input.
    //
    //	Returns: boolean
    //				true - if the initialize was OK
    //				false - if there was a problem with initialization
    //
    ///////////////////////////////////////////////////////////////////////////
    public boolean Initialize(String[] argv) {
        URL url;
        knownURLs = new Hashtable();
        newURLs = new Vector();

        if (argv.length == 0) {
            System.out.println("\n\nNo starting URL Provided. Correct Usage::");
            System.out.println("\njava CrawlerEngine <starting URL> [number of pages]");
            System.out.println("\nAfter you hit enter, the web crawler will start the crawl at");
            System.out.println("<starting URL> and search for URLs in these pages and crawl");
            System.out.println("those pages and so on up to [number of pages].Note that the");
            System.out.println("[number of pages] argument is optional and the default is set at 20");

            return false;

        } // if

        try {
            url = new URL(argv[0]);

        } catch (MalformedURLException e) {

            System.out.println("\n\nInvalid starting URL " + argv[0]);
            System.out.println("\n\nValid URLs start with 'http://www...' and so on. You may also");
            System.out.println("specify the number of pages to search as an optional second argument.");

            return false;

        } // try

        knownURLs.put(url, new Integer(1));
        newURLs.addElement(url);

        if (argv.length > 1) {

            System.out.println("argv[1] = " + argv[1]);
            int iPages = Integer.parseInt(argv[1]);
            System.out.println("iPages = " + Integer.parseInt(argv[1]));

            maxPages = iPages;

        } else {

            maxPages = MAX_PAGES;

        } // if

        System.out.println("Starting crawl with initial URL:: " + url.toString());
        System.out.println("Maximum number of pages::" + maxPages);

        //Set the proxy and port - important for firewalls

        Properties props = new Properties(System.getProperties());
        props.put("http.proxySet", "true");
        props.put("http.proxyHost", "webcache-cup");
        props.put("http.proxyPort", "8080");

        Properties newprops = new Properties(props);
        System.setProperties(newprops);

        return true;

    } // Initialize method

    //////////////////////////////////////////////////////////////////////////
    //	METHOD:: RobotSafe(URL url)
    //	Arguments:
    //		URL url - this is the current url to process
    //
    //	Purpose: This method checks that the robot exclusion protocol does not
    //			 prohibit downloading and parsing the URL. This method will check
    // 			 the /robots.txt file to for instructions about the site's Robot
    // 			 Exclusion Protocol/Policy. Before crawling a site, this method is
    //			 invoked to check the robots.txt to see if the "Disallow:" string is
    //			 is present. Any "Disallow:" is assumed to apply to this crawler and
    //			 will stop the crawling process.on the site.
    //
    //	Returns: boolean
    //				false:	crawling is disallowed
    //				true:	crawling is permitted.
    //
    ///////////////////////////////////////////////////////////////////////////
    public boolean RobotSafe(URL url) {
        // We start this method by establishing  the complete URL of the robots.txt file.
        // More information about the robot.txt file and the standards governing "robot safe"
        // can be found at "http://www.robotstxt.org/"

        String strCommands;
        String strHost = url.getHost();
        String strRobot = "http://" + strHost + "/robots.txt";
        URL urlRobot;

        try {
            urlRobot = new URL(strRobot);

        } catch (MalformedURLException e) {

            // Something is wrong with the host - to be safe we mark it as unsafe
            // or assume that this side "disallows" crawling

            return false;

        } // try

        if (DEBUG) {
            System.out.println("Checking robot protocol:" + urlRobot.toString());
        }

        try {
            try (InputStream urlRobotStream = urlRobot.openStream()) {
                byte b[] = new byte[1000];
                int numRead = urlRobotStream.read(b);
                strCommands = new String(b, 0, numRead);

                while (numRead != -1) {
                    numRead = urlRobotStream.read(b);

                    if (numRead != -1) {
                        String newCommands = new String(b, 0, numRead);
                        strCommands += newCommands;
                    } // if

                }
            }

        } catch (IOException e) {
            // This means there is no robots.txt file. We assume it is OK to
            // crawl the site and continue the search.

            return true;
        } // try

        if (DEBUG) {
            System.out.println(strCommands);
        }

        // OK we found a robots.txt file and read it into the strCommands data structure.
        // We will assume that this robots.txt applies to us just to be on the safe side.
        // Next we will parse the file and search for the "Disallow:" string.

        String strURL = url.getFile();
        int index = 0;

        while ((index = strCommands.indexOf(DISALLOW, index)) != -1) {
            index += DISALLOW.length();
            String strPath = strCommands.substring(index);
            StringTokenizer st = new StringTokenizer(strPath);

            if (!st.hasMoreTokens()) {
                break;

            } // if

            String strBadPath = st.nextToken();

            if (strURL.indexOf(strBadPath) == 0) {
                return false;

            } // if

        } // while

        return true;

    } // RobotSafe

    //////////////////////////////////////////////////////////////////////////
    //	METHOD:: AddNewUrl(URL oldURL, String newUrlString)
    //	Arguments:
    //		URL oldURL - this is the current url that is being searched for URLs
    //		String newUrlString - this is a new URL found in the oldURL page.
    //
    // 		Note that URLs can be either absolute or relative.
    //
    //	Purpose: This method adds new URLs that are found at the current (oldURL)
    // 			 to the queue of URLs to crawl. This method will only add URLs that
    //			 end in htm or html.
    //
    //	Returns: void
    //
    ///////////////////////////////////////////////////////////////////////////
    public void AddNewUrl(URL oldURL, String newUrlString) {
        URL url;

        if (DEBUG) {
            System.out.println("URL String " + newUrlString);
        }

        try {
            url = new URL(oldURL, newUrlString);

            if (!knownURLs.containsKey(url)) {
                String filename = url.getFile();
                int iSuffix = filename.lastIndexOf("htm");

                if ((iSuffix == filename.length() - 3) || (iSuffix == filename.length() - 4)) {
                    knownURLs.put(url, new Integer(1));
                    newURLs.addElement(url);
                    store(url);

                    System.out.println("Found new URL " + url.toString());

                } // if
            } // if

        } catch (MalformedURLException e) {
        } // try

    }

    public void store(URL url) {
        BufferedReader in;
        String line;
        String[] l;

        String temp;
        try {
            //out = new fileWriter("test.txt");
            in = new BufferedReader(new InputStreamReader(url.openStream()));
            while ((line = in.readLine()) != null) {
                l = line.split(" ");
                for (int i = 0; i < l.length; i++) {
                    temp = l[i].trim();
                    if (!(temp.equals(""))) {
                        temp = temp.replace("\\W$", " ");
                        st.addString(temp, url);
                    }
                }
            }
            in.close();
        } catch (IOException e) {
        }

    }// AddNewUrl Method

    //////////////////////////////////////////////////////////////////////////
    //	METHOD:: LoadPage(URL url)
    //	Arguments:
    //		URL url - this the URL to the page to download
    //
    //
    //	Purpose: This method adds new URLs that are found at the current (oldURL)
    // 			 to the queue of URLs to crawl. This method will only add URLs that
    //			 end in htm or html.
    //
    //	Returns: void
    //
    ///////////////////////////////////////////////////////////////////////////
    public String LoadPage(URL url) {
        try {
            // We first try to open the file pointed to by "url"

            URLConnection urlConnection = url.openConnection();
            System.out.println("Downloading URL::" + url.toString());
            urlConnection.setAllowUserInteraction(false);
            InputStream urlStream = url.openStream();

            // OK, now we will read in the entire file or page pointed by "url."
            // There is a maximum file size that can be read in that is established
            // by MAX_FILE_SIZE.

            byte b[] = new byte[1000];
            int numRead = urlStream.read(b);
            String content = new String(b, 0, numRead);

            while ((numRead != -1) && (content.length() < MAX_FILE_SIZE)) {
                numRead = urlStream.read(b);

                if (numRead != -1) {

                    String newContent = new String(b, 0, numRead);
                    content += newContent;

                } // if

            } // while

            return content;

        } catch (IOException e) {

            System.out.println("ERROR: couldn't open URL ");
            return "";

        } // try

    }

    // LoadPage method
    //////////////////////////////////////////////////////////////////////////
    //	METHOD:: ProcessPage(URL url, String page)
    //	Arguments:
    //		URL url - this the URL to the page to download
    //		String page - this is the page that was down loaded from "url."
    //
    //	Purpose: This method will parse through the page looking for valid URLs.
    //			 A valid URL is defined as "<a href=" ...  and ends with a close
    //			 angle bracket, preceded by a close quote. In some cases it may be
    // 			 possibly preceded by a hatch mark indicated a fragment.
    //
    //	Returns: void
    //
    ///////////////////////////////////////////////////////////////////////////
    public void ProcessPage(URL url, String page) {
        String lcPage = page.toLowerCase(); 	// Convert all text in the page to lower case.
        int index = 0; 							// Character position in the page.
        int iEndAngle, ihref, iURL,
                iCloseQuote, iHatchMark, iEnd;		// Key token characters for parsing

        while ((index = lcPage.indexOf("<a", index)) != -1) {
            iEndAngle = lcPage.indexOf(">", index);
            ihref = lcPage.indexOf("href", index);
            if (ihref != -1) {
                iURL = lcPage.indexOf("\"", ihref) + 1;

                if ((iURL != -1) && (iEndAngle != -1) && (iURL < iEndAngle)) {
                    iCloseQuote = lcPage.indexOf("\"", iURL);
                    iHatchMark = lcPage.indexOf("#", iURL);

                    if ((iCloseQuote != -1) && (iCloseQuote < iEndAngle)) {
                        iEnd = iCloseQuote;

                        if ((iHatchMark != -1) && (iHatchMark < iCloseQuote)) {
                            iEnd = iHatchMark;
                        }

                        String newUrlString = page.substring(iURL, iEnd);
                        AddNewUrl(url, newUrlString);

                    } // if
                } // if
            } // if

            index = iEndAngle;

        } // while

    } // ProcessPage method

    //////////////////////////////////////////////////////////////////////////
    //	METHOD:: Crawl(String[] argv)
    //	Arguments:
    //		String[] argv - this are the command line arguments passed directly
    //						from main().
    //
    //	Purpose: This method orchastrates the work of crawling utilizing the
    //			 above methods. This method will start with the base URL and
    //			 continue to pop off new URLs as they are added and crawl each
    //			 in turn.
    //
    //	Returns: void
    //
    ///////////////////////////////////////////////////////////////////////////
    public void Crawl() {
        String[] argv = {"http://textfiles.com", "2"};
        if (Initialize(argv)) {
            for (int i = 0; i < maxPages; i++) {
                URL url = (URL) newURLs.elementAt(0);
                newURLs.removeElementAt(0);

                if (DEBUG) {

                    System.out.println("Searching " + url.toString());


                }

                if (RobotSafe(url)) {
                    String page = LoadPage(url);

                    if (DEBUG) {
                        System.out.println(page);

                    }

                    if (page.length() != 0) {
                        ProcessPage(url, page);
                    }

                    if (newURLs.isEmpty()) {
                        break;
                    }

                } else {

                    System.out.println("URL Disallowed::" + url.toString());


                } // if

            } // for

            HashMap cashe = st.getHashMap();
            Set<String> keyset = cashe.keySet();
            try {
                File file = new File("cache.txt");
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();

                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                for (String key : keyset) {
                    bw.write(key + "###" + cashe.get(key) + "\n");
                    System.out.println(key + "###" + cashe.get(key));
                    results += key + " : " + cashe.get(key) + "\n";
                }

                bw.close();

                System.out.println("Done");

            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("THE in " + cashe.get("the"));
            System.out.println("Search complete.");



        } // if

    }

    public void run() {
        Crawl();
    }

    public HashMap getCache() {
        return st.getHashMap();
    }
    // Crawl
    //////////////////////////////////////////////////////////////////////////
    //	METHOD:: main(String[] argv)
    //	Arguments:
    //		String[] argv - these are the command line arguments entered by
    //						the user.
    //
    //	Purpose: The main instantiates a CrawlerEngine and starts the operation
    //			 by calling the Crawl() method.
    //
    //	Returns: void
    //
    ///////////////////////////////////////////////////////////////////////////
//    public static void main2(String[] argv) {
//        CrawlerEngine wc = new CrawlerEngine();
//        wc.Crawl(argv);
//    } // main
} // WebCrawler