import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;
import javax.swing.border.SoftBevelBorder;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

public class WebCrawler {
    private static final String URL_PATTERN_REGEX = "^(https?|ftp|file):[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
    private static final Pattern URL_PATTERN = Pattern.compile(URL_PATTERN_REGEX);

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the starting URL");
        String inputURL = sc.next();
        if (!URL_PATTERN.matcher(inputURL).matches()) {
            throw new IllegalArgumentException(String.format("%s is not a valid URL", inputURL));
        }

        System.out.println("Enter the maximum depth you want to continue crawling");
        int maxLevel = sc.nextInt();
        if (maxLevel <= 0) {
            throw new IllegalArgumentException("maxLevel should be greater than 0");
        }

        Set<String> seedURLs = new HashSet<>();
        Set<String> childLinks = new HashSet<>();
        seedURLs.add(inputURL);
        int currentLevel = 1;

        while (currentLevel <= maxLevel) {
            for (String link : seedURLs) {
                String content = getWebPageContentAsString(link);
                if (content == null) {
                    continue; 
                }
                
                List<String> cLinks = getAllLinksInString(content);
                for (String childLink : cLinks) {
                    if (!seedURLs.contains(childLink)) {
                        childLinks.add(childLink);
                    }
                }
            }
            printOutput(currentLevel, childLinks);
            seedURLs.clear();
            seedURLs.addAll(childLinks);
            childLinks.clear();
            currentLevel++;
        }
        System.out.println("*************************************End of program***************");
    }

    private static void printOutput(int currentLevel, Set<String> links) {
        System.out.println(String.format("================Start: Depth=%s=============", currentLevel));
        int index = 1;
        for (String link : links) {
            System.out.println(index + ".> " + link);
            index++;
        }
        System.out.println(String.format("============END:Depth=%s============", currentLevel));
    }
// Error occurs in this method for connecting server
    private static  String getWebPageContentAsString(String urlLink) throws Exception {
    URL url = new URL(urlLink);
    String result=null;
    try {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setConnectTimeout(1000);
        urlConnection.setReadTimeout(1000);
        InputStream is=urlConnection.getInputStream();
        InputStreamReader isr=new InputStreamReader(is);
        
        int numCharsRead;
        char[] charArray=new char[1024];
        StringBuffer sb=new StringBuffer();
        while ((numCharsRead = isr.read(charArray))> 0) {
            sb.append(charArray,0,numCharsRead);
        }
    result=sb.toString();
    } catch (IOException e) {
        System.out.println("Error fetching content from URL: " );
        e.printStackTrace(); 
    }
    return result;
}


    private static List<String> getAllLinksInString(String content) throws IOException {
        Reader reader = new StringReader(content);
        HTMLEditorKit.Parser parser = new ParserDelegator();
        final List<String> links = new ArrayList<String>();

        parser.parse(reader, new HTMLEditorKit.ParserCallback() {
            public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
                if (t == HTML.Tag.A) {
                    Object link = a.getAttribute(HTML.Attribute.HREF);
                    if (link != null) {
                        String linkValue = String.valueOf(link);
                        if (linkValue.startsWith("http") || linkValue.startsWith("www") || linkValue.startsWith("www")) {
                            links.add(linkValue);
                        }
                    }
                }
            }
        }, true);
        
        reader.close();
        return links;
    }
}
