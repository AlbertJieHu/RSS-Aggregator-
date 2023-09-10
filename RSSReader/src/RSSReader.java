import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.xmltree.XMLTree;
import components.xmltree.XMLTree1;

/**
 * Program to convert an XML RSS (version 2.0) feed from a given URL into the
 * corresponding HTML output file.
 *
 * @author Albert Hu
 *
 */
public final class RSSReader {

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private RSSReader() {
    }

    /**
     * Outputs the "opening" tags in the generated HTML file. These are the
     * expected elements generated by this method:
     *
     * <html> <head> <title>the channel tag title as the page title</title>
     * </head> <body>
     * <h1>the page title inside a link to the <channel> link</h1>
     * <p>
     * the channel description
     * </p>
     * <table border="1">
     * <tr>
     * <th>Date</th>
     * <th>Source</th>
     * <th>News</th>
     * </tr>
     *
     * @param channel
     *            the channel element XMLTree
     * @param out
     *            the output stream
     * @updates out.content
     * @requires [the root of channel is a <channel> tag] and out.is_open
     * @ensures out.content = #out.content * [the HTML "opening" tags]
     */
    private static void outputHeader(XMLTree channel, SimpleWriter out) {
        assert channel != null : "Violation of: channel is not null";
        assert out != null : "Violation of: out is not null";
        assert channel.isTag() && channel.label().equals("channel") : ""
                + "Violation of: the label root of channel is a <channel> tag";
        assert out.isOpen() : "Violation of: out.is_open";

        /*
         * Sets the title of the page using the xml tree. If no title is
         * present, it will be set to "Empty Title"
         */
        if (channel.child(getChildElement(channel, "title"))
                .numberOfChildren() > 0) {
            out.println("<html><head><title>"
                    + channel.child(getChildElement(channel, "title")).child(0)
                    + "</title></head><body>");
        } else {
            out.println("<html><head><title>Empty Title</title></head></body>");
        }

        /*
         * Creates a header for the page and embedded within is a link to the
         * original webpage
         */
        out.println("<h1><a href=\""
                + channel.child(getChildElement(channel, "link")).child(0)
                + "\">"
                + channel.child(getChildElement(channel, "title")).child(0)
                + "</a></h1>");

        /*
         * Creates a description under the header. If there is no description,
         * it will say "No Description".
         */
        if (channel.child(getChildElement(channel, "description"))
                .numberOfChildren() > 0) {
            out.println("<p>");
            out.println(channel.child(getChildElement(channel, "description"))
                    .child(0));
            out.println("</p>");
        } else {
            out.println("<p>");
            out.println("No Description");
            out.println("</p>");
        }

        /*
         * Creates the header for the table
         */
        out.println("<table border = \"1\">");
        out.println("<tr>");
        out.println("<th>Date</th>");
        out.println("<th>Source</th>");
        out.println("<th>News</th>");
        out.println("</tr>");

    }

    /**
     * Outputs the "closing" tags in the generated HTML file. These are the
     * expected elements generated by this method:
     *
     * </table>
     * </body> </html>
     *
     * @param out
     *            the output stream
     * @updates out.contents
     * @requires out.is_open
     * @ensures out.content = #out.content * [the HTML "closing" tags]
     */
    private static void outputFooter(SimpleWriter out) {
        assert out != null : "Violation of: out is not null";
        assert out.isOpen() : "Violation of: out.is_open";

        /*
         * Closing tags for table, body, and html
         */
        out.println("</table>");
        out.println("</body> </html>");
    }

    /**
     * Finds the first occurrence of the given tag among the children of the
     * given {@code XMLTree} and return its index; returns -1 if not found.
     *
     * @param xml
     *            the {@code XMLTree} to search
     * @param tag
     *            the tag to look for
     * @return the index of the first child of type tag of the {@code XMLTree}
     *         or -1 if not found
     * @requires [the label of the root of xml is a tag]
     * @ensures <pre>
     * getChildElement =
     *  [the index of the first child of type tag of the {@code XMLTree} or
     *   -1 if not found]
     * </pre>
     */
    private static int getChildElement(XMLTree xml, String tag) {
        assert xml != null : "Violation of: xml is not null";
        assert tag != null : "Violation of: tag is not null";
        assert xml.isTag() : "Violation of: the label root of xml is a tag";

        /*
         * Traverses through the children of an xml tree using a for loop. If
         * the label of the child is equal to the tag being searched, the
         * variable index will be updated to the current index. Because the for
         * loop goes backwards, index's final update will be the first
         * occurrence of the tag being searched. The index is then returned.
         */
        int index = -1;
        for (int i = xml.numberOfChildren() - 1; i >= 0; i--) {
            if (xml.child(i).isTag()) {
                if (xml.child(i).label().equals(tag)) {
                    index = i;
                }
            }
        }
        return index;
    }

    /**
     * Processes one news item and outputs one table row. The row contains three
     * elements: the publication date, the source, and the title (or
     * description) of the item.
     *
     * @param item
     *            the news item
     * @param out
     *            the output stream
     * @updates out.content
     * @requires [the label of the root of item is an <item> tag] and
     *           out.is_open
     * @ensures <pre>
     * out.content = #out.content *
     *   [an HTML table row with publication date, source, and title of news item]
     * </pre>
     */
    private static void processItem(XMLTree item, SimpleWriter out) {
        assert item != null : "Violation of: item is not null";
        assert out != null : "Violation of: out is not null";
        assert item.isTag() && item.label().equals("item") : ""
                + "Violation of: the label root of item is an <item> tag";
        assert out.isOpen() : "Violation of: out.is_open";

        /*
         * Variables to hold xml data
         */
        String date = "No date available";
        String source = "No source available";
        String sourceURL = null;
        String title = "No title available";
        String link = null;

        /*
         * Traverses through the children of item using a while loop. Searches
         * for a tag and if the tag has a child, it will update their respective
         * variables.
         */
        int i = 0;
        while (i < item.numberOfChildren()) {
            //Searches for the publication date of the item
            if (item.child(i).label().equals("pubDate")
                    && item.child(i).numberOfChildren() > 0) {
                date = item.child(i).child(0).label();
            }
            //Searches for the source of the item and it's url
            if (item.child(i).label().equals("source")
                    && item.child(i).numberOfChildren() > 0) {
                source = item.child(i).child(0).label();
                sourceURL = item.child(i).attributeValue("url");
            }
            //Searches for the title or description of the item
            if (item.child(i).label().equals("title")
                    && item.child(i).numberOfChildren() > 0) {
                title = item.child(i).child(0).label();
            } else if (item.child(i).label().equals("description")
                    && item.child(i).numberOfChildren() > 0) {
                title = item.child(i).child(0).label();
            }
            //Searches for the link of the item
            if (item.child(i).label().equals("link")
                    && item.child(i).numberOfChildren() > 0) {
                link = item.child(i).child(0).label();
            }
            i++;
        }

        /*
         * Creates the table row that will be displaying the information
         * gathered from the xml tree.
         */

        //Shows the date of the item
        out.println("<tr>");
        out.println("<td>" + date + "</td>");

        /*
         * If the source and it's URL exist, they will be shown. Otherwise, it
         * will show "No source available"
         */
        if (sourceURL != null) {
            out.println("<td><a href=\"" + sourceURL + "\">" + source
                    + "</a></td>");
        } else {
            out.println("<td>" + source + "</td>");
        }

        /*
         * If the link exists, it will be embedded into the title. Otherwise,
         * only the title will be shown. If there is no title, it will show
         * "No title available"
         */
        if (link != null) {
            out.println("<td><a href=\"" + link + "\">" + title + "</a></td>");
        } else {
            out.println("<td>" + title + "</td>");
        }
        out.println("</tr>");

    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments; unused here
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();

        //Asks for URL and sets it to the String url
        out.print("Enter the URL of an RSS 2.0 news feed: ");
        String url = in.nextLine();

        //Asks for the name of the html file
        out.print("Enter the .html Output File: ");
        SimpleWriter outputFile = new SimpleWriter1L(in.nextLine());

        //Initialize the xml tree
        XMLTree xml = new XMLTree1(url);

        //Extracts the channel element
        if (xml.label().equals("rss")
                && xml.attributeValue("version").equals("2.0")) {
            XMLTree channel = xml.child(0);
            outputHeader(channel, outputFile);
            /*
             * Goes through every item within the channel using a while loop
             * Calls processItem at each new item to create a new row of data
             * Creates a whole table consisting of all items
             */
            int count = 0;
            while (count < channel.numberOfChildren()) {
                if (channel.child(count).label().equals("item")) {
                    processItem(channel.child(count), outputFile);
                }
                count++;
            }
            outputFooter(outputFile);
        } else {
            out.println("Not Valid RSS 2.0");
        }

        in.close();
        out.close();
        outputFile.close();
    }
}