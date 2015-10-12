

import java.util.List;

public class Question {
	String body;
	String type;
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<String> getIdeal_answer() {
		return ideal_answer;
	}

	public void setIdeal_answer(List<String> ideal_answer) {
		this.ideal_answer = ideal_answer;
	}

	public List<Snippet> getSnippets() {
		return snippets;
	}

	public void setSnippets(List<Snippet> snippets) {
		this.snippets = snippets;
	}

	String id;
	List<String> ideal_answer;
	
	List<Snippet> snippets;
}
