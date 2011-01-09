package de.feanor.htmltokenizer;

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

	public boolean isTag() {
		return type == TAG;
	}

	public boolean isText() {
		return type == TEXT;
	}

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
