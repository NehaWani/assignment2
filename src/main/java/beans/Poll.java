package beans;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Title: CMPE-273 Assignment 2.
 * Statement: Create a polling application using REST APIs. Use mongodb to persist data.
 * Created by: Neha Wani
 * Created on:  03-25-2015
 * Description: This is a bean class for poll.
 * @author neh
 *
 */
@Document(collection="polls")
public class Poll {
	
	@Id
	private String id;
	@NotBlank
	private String question;
	@NotBlank
	private String started_at;
	@NotBlank
	private String expired_at;
	@NotBlank
	private String[] choice;
	private int[] results;
	@JsonIgnore
	private int moderatorId;
	@JsonIgnore
	private boolean mailSent;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	public String getStarted_at() {
		return started_at;
	}
	public void setStarted_at(String started_at) {
		this.started_at = started_at;
	}
	public String getExpired_at() {
		return expired_at;
	}
	public void setExpired_at(String expired_at) {
		this.expired_at = expired_at;
	}
	public String[] getChoice() {
		return choice;
	}
	public void setChoice(String[] choice) {
		this.choice = choice;
	}
	public int[] getResults() {
		return results;
	}
	public void setResults(int[] results) {
		this.results = results;
	}
	public int getModeratorId() {
		return moderatorId;
	}
	public void setModeratorId(int moderatorId) {
		this.moderatorId = moderatorId;
	}
	public boolean isMailSent() {
		return mailSent;
	}
	public void setMailSent(boolean mailSent) {
		this.mailSent = mailSent;
	}
	
	public Poll() {
		super();
	}

	public Poll(String id, String question, String started_at, String expired_at, String[] choice, int[] results) {
		super();
		this.id = id;
		this.question = question;
		this.started_at = started_at;
		this.expired_at = expired_at;
		this.choice = choice;
		this.results = results;
	}
	
	public Poll(String id, String question, String started_at,	String expired_at, String[] choice, int moderatorId) {
		super();
		this.id = id;
		this.question = question;
		this.started_at = started_at;
		this.expired_at = expired_at;
		this.choice = choice;
		this.moderatorId = moderatorId;
	}
}
