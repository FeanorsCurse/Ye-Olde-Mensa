package de.feanor.htmltokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Quick'n'dirty implementation of a HTML tokenizer (/parser). See
 * parse(BufferedReader) comment for hack :P.
 * 
 * @author Daniel Süpke
 * 
 */
public class SimpleHTMLTokenizer {
	private List<Token> tokenList = new ArrayList<Token>();

	private ListIterator<Token> tokens;

	public static final int TAG = 0, TEXT = 1;

	public SimpleHTMLTokenizer(URL url, String encoding) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				url.openStream(), encoding));
		parse(br);
		tokens = tokenList.listIterator();
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

				// Remove whitespace at end
				if (buf[size - 1] == ' ')
					size--;

				tokenList.add(new Token(new String(buf, 0, size).toLowerCase(),
						TAG));

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

					tokenList.add(new Token(new String(buf, 0, size), TEXT));
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
	public String nextTag() {
		Token token;

		while (tokens.hasNext()) {
			if ((token = tokens.next()).isTag())
				return token.content;
		}

		return null;
	}

	/**
	 * Returns the next text element. All tags inbetween are skipped.
	 * 
	 * @return Next text element
	 */
	public String nextText() {
		Token token;

		while (tokens.hasNext()) {
			if ((token = tokens.next()).isText())
				return sanitizeHTML(token.content);
		}

		return null;
	}

	/**
	 * Pushes back the last element, so it can be read again with nextTag
	 * ornextElement
	 */
	public void pushBack() {
		tokens.previous();
	}

	/**
	 * Replacing html-entities with actual chars. The Mensa OL Studentenwerk's
	 * web site is really messed up.
	 */
	private String sanitizeHTML(String element) {
		element = element.replace("&auml;", "ä");
		element = element.replace("&Auml;", "Ä");
		element = element.replace("&uuml;", "ü");
		element = element.replace("&Uuml;", "Ü");
		element = element.replace("&ouml;", "ö");
		element = element.replace("&Ouml;", "Ö");

		return element;
	}

	private class Token {
		String content;
		int type;

		public Token(String content, int type) {
			this.content = content;
			this.type = type;
		}

		public boolean isTag() {
			return type == TAG;
		}

		public boolean isText() {
			return type == TEXT;
		}
	}
}