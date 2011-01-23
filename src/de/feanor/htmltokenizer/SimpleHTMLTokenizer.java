package de.feanor.htmltokenizer;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import static de.feanor.htmltokenizer.Element.TAG;
import static de.feanor.htmltokenizer.Element.TEXT;

/**
 * Quick'n'dirty implementation of a HTML elementizer (/parser). See
 * parse(BufferedReader) comment for hack :P.
 * 
 * @author Daniel Süpke
 * 
 */
public class SimpleHTMLTokenizer {
	private List<Element> elementList = new ArrayList<Element>();
	public static final int TIMEOUT = 15;
	private ListIterator<Element> elements;

	public SimpleHTMLTokenizer(URL url, String encoding) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				url.openStream(), encoding));
		parse(br);
		elements = elementList.listIterator();
	}
	
	public SimpleHTMLTokenizer(String urlString, String encoding, String date) throws IOException {
		
		     URL url = new URL(urlString);
		     URLConnection conn = url.openConnection();
		     HttpURLConnection httpConn = (HttpURLConnection)conn;
		     
		     httpConn.setRequestMethod("GET");
		     httpConn.setDoOutput(true); 
		     httpConn.setDoInput(true);
		     httpConn.connect(); 
		     
		     BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		     String line, name, value;
		     String dataset = URLEncoder.encode("ctl00%24ctl00%24_PlaceHolderLeftNavBarTop%24SearchBox%24S5F437507_InputKeywords", "UTF-8")+"=";
		     dataset += "&" + URLEncoder.encode("__EVENTTARGET", "UTF-8") + "=" + URLEncoder.encode("ctl00$ctl00$_PlaceHolderCalendarNavigator$PlaceHolderCalendarNavigator$MensaKalender", "UTF-8");
		     dataset += "&" + URLEncoder.encode("__EVENTARGUMENT", "UTF-8") + "=" + URLEncoder.encode(date, "UTF-8");
		     while ((line = br.readLine()) != null) {
		         if(line.matches("^<input.*")){
		        	 name = line.split(" ")[2].split("=")[1]; 
		        	 name = name.substring(1, name.length()-1);
		        	 value = line.split(" ")[4].split("=")[1];
		        	 
		        	 if(line.split(" ")[4].split("=").length > 2){
		        		 //System.out.println(""+line.split(" ")[4].split("=")[2]);
		        		 value += "="+line.split(" ")[4].split("=")[2];
		        	 }
		        	 value = value.substring(1, value.length()-1);
		        	 //System.out.println(name +": "+ value);
		        	 dataset += "&" + URLEncoder.encode(name, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8");
		        	    	 
		         }
		     }
		     br.close();
		  
		     //System.out.println(dataset);
		     
		  	 url = new URL(urlString); 
		     conn = url.openConnection();
		     
		     conn.setDoOutput(true);
		     OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		     wr.write(dataset);
		     wr.flush();

		     // Get the response
		     br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		     parse(br);
		     wr.close();
		     br.close();
		 
		     elements = elementList.listIterator();
	}

	// TODO: This parser is quite bad. After each tag it always assumes a text,
	// which will be ignored if a tag.
	// Also, the char-copy operations could probably be much faster.
	// However, I found no gpl+up-to-date parser out there, so this is a quick
	// hack.
	private void parse(BufferedReader br) throws IOException {
		char c;
		char[] buf = new char[50000];
		int size = 0;
		int j = 0;
		boolean finished = false;

		if ((c = (char) br.read()) == -1)
			throw new IOException("Empty buffer!");

		do {
			size = 0;
			if (j++ == 300000)
				break;

			// Tag
			if (c == '<') {
				size = 0;

				while ((c = (char) br.read()) != -1 && c != '>') {
					if (c == '\n' || c == '\r' || c == '\t' || c == ' ') {
						c = ' ';
						if (size > 0 && buf[size - 1] != ' ') {
							buf[size++] = c;
						}
					} else {
						buf[size++] = c;
					}
				}

				// Remove attributes
				for (int i = 0; i < size-1; i++) {
					if (buf[i] == ' ')
						size = i;
				}

				elementList.add(new Element(TAG, new String(buf, 0, size)
						.toLowerCase()));

				// TODO: Worst finish condition evar
				finished = new String(buf, 0, size).toLowerCase().equals(
						"/html");
			}

			// Text
			else {
				while ((c = (char) br.read()) != -1 && c != '<') {
					if (c == '\n' || c == '\r' || c == '\t' || c == ' ') {
						c = ' ';
						if (size > 0 && buf[size - 1] != ' ') {
							buf[size++] = c;
						}
					} else {
						buf[size++] = c;
					}
				}
				if (size > 0) {
					// Remove whitespace at end
					if (buf[size - 1] == ' ')
						size--;

					elementList
							.add(new Element(TEXT, new String(buf, 0, size)));
				}
			}
			// TODO: Worst finish condition evar
		} while (!finished);
	}

	/**
	 * Returns the next tag. All text elements inbetween are skipped.
	 * 
	 * @return Next html tag
	 */
	public Element nextTag() {
		Element element;

		while (elements.hasNext()) {
			if ((element = elements.next()).isTag())
				return element;
		}

		return null;
	}

	/**
	 * Returns the next text element. All tags inbetween are skipped.
	 * 
	 * @return Next text element
	 */
	public Element nextText() {
		Element element;

		while (elements.hasNext()) {
			if ((element = elements.next()).isText())
				return element;
		}

		return null;
	}

	/**
	 * Returns the next element, regardless of it being TEXT or TAG.
	 * 
	 * @return Next element
	 */
	public Element nextElement() {
		return elements.next();
	}

	/**
	 * Pushes back the last element, so it can be read again with nextTag
	 * ornextElement
	 */
	public void pushBack() {
		elements.previous();
	}
}