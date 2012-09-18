package website.web.model;

import java.util.ArrayList;
import java.util.List;

public class Person {
	private String name;
	private String uri;
	private String homepage;
	private List<Person> friends;
	private List<Person> couldKnow;
	private List<Person> couldBeIntroduced;
	
	public Person() {
		friends = new ArrayList<Person>();
		couldKnow = new ArrayList<Person>();
		couldBeIntroduced = new ArrayList<Person>();
	}
	
	public String getHomepage() {
		return homepage;
	}
	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}

	public List<Person> getFriends() {
		return friends;
	}

	public void setFriends(List<Person> friends) {
		this.friends = friends;
	}

	public List<Person> getCouldKnow() {
		return couldKnow;
	}

	public void setCouldKnow(List<Person> couldKnow) {
		this.couldKnow = couldKnow;
	}

	public List<Person> getCouldBeIntroduced() {
		return couldBeIntroduced;
	}

	public void setCouldBeIntroduced(List<Person> couldBeIntroduced) {
		this.couldBeIntroduced = couldBeIntroduced;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}

}
