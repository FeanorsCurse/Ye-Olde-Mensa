package de.feanor.htmltokenizer;

/**
 * Represents a html element for the SimpleHTMLTokenizer. May be either of type
 * TAG or TEXT.
 * 
 * @author Daniel Süpke
 */
public class Element {

	public static final int TAG = 0, TEXT = 1;

	public final int type;

	public final String content;

	// Not supported yet
	// public Map attributes;

	public Element(int type, String content) {
		if (type == TEXT)
			content = sanitizeHtml(content);

		this.type = type;
		this.content = content;
	}

	/**
	 * Return true if the element is of type TAG.
	 * 
	 * @return True if tAG
	 */
	public boolean isTag() {
		return type == TAG;
	}

	/**
	 * Return true if the element is of type TEXT
	 * 
	 * @return True if TEXT
	 */
	public boolean isText() {
		return type == TEXT;
	}

	/**
	 * Replaces html entities for Umlaute with correct Umlauten. Learn to use
	 * utf-8, stupid web site developers!!
	 * 
	 * @param content
	 *            String to sanitize
	 * @return Sanitized string
	 */
	private String sanitizeHtml(String content) {
		content = content.replace("&auml;", "ä");
		content = content.replace("&Auml;", "Ä");
		content = content.replace("&uuml;", "ü");
		content = content.replace("&Uuml;", "Ü");
		content = content.replace("&ouml;", "ö");
		content = content.replace("&Ouml;", "Ö");

		return content;
	}
}
