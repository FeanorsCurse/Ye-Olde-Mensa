package de.feanor.htmltokenizer;

import static de.feanor.htmltokenizer.Element.TAG;
import static de.feanor.htmltokenizer.Element.TEXT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Quick'n'dirty implementation of a HTML elementizer (/parser). See
 * parse(BufferedReader) comment for hack :P.
 * 
 * @author Daniel Süpke
 */
public class SimpleHTMLTokenizer {
	public static final int TIMEOUT = 15;
	
	private List<Element> elementList = new ArrayList<Element>();

	private ListIterator<Element> elements;

	/**
	 * Constructor will parse the given URL.
	 * 
	 * @param url
	 *            URL of html page to parse
	 * @param encoding
	 *            I.e. "iso 8859-1" or "utf-8" (hopefully the latter)
	 * @throws IOException
	 *             Thrown if there is a problem connecting to the web site
	 */
	public SimpleHTMLTokenizer(URL url, String encoding) throws IOException {
		URLConnection conn = url.openConnection();
		conn.setConnectTimeout(TIMEOUT * 1000);
		conn.setReadTimeout(TIMEOUT * 1000);

		BufferedReader br = new BufferedReader(new InputStreamReader(
				conn.getInputStream(), encoding));
		parse(br);
		elements = elementList.listIterator();
	}

	// TODO: This parser is quite bad. After each tag it always assumes a text,
	// which will be ignored if a tag.
	// Also, the char-copy operations could probably be much faster.
	// However, I found no gpl+up-to-date parser out there, so this is a quick
	// hack.
	private void parse(BufferedReader br) throws IOException {
		char c;
		char[] buf = new char[50001];
		int size = 0;
		int j = 0;
		boolean finished = false;

		if ((c = (char) br.read()) == -1)
			throw new IOException("Empty buffer!");

		do {
			size = 0;
			if (j++ == 50000) throw new RuntimeException("Error in html parser");

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
				for (int i = 0; i < size - 1; i++) {
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