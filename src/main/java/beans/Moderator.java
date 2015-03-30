package beans;

import java.util.ArrayList;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Title: CMPE-273 Assignment 2.
 * Statement: Create a polling application using REST APIs. Use mongodb to persist data.
 * Created by: Neha Wani
 * Created on:  03-25-2015
 * Description: This is a bean class for moderator.
 * @author neh
 *
 */
@Document(collection="moderators")
public class Moderator {

	@Id
	private Integer id;
	@NotBlank
	private String name;
	@Email @NotBlank
	private String email;
	@NotBlank
	private String password;
	private String created_at;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getCreated_at() {
		return created_at;
	}
	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}

	public Moderator() {
	
	}
	
	public Moderator(Integer id, String name, String email, String password, String created_at) {
		super();
		this.id = id;
		this.name = name;
		this.email = email;
		this.password = password;
		this.created_at = created_at;
	}
	
/*	public Moderator(Integer id, String name, String email, String password, String created_at, ArrayList<Poll> pollList) {
		super();
		this.id = id;
		this.name = name;
		this.email = email;
		this.password = password;
		this.created_at = created_at;
		this.pollList = pollList;
	}
*/	
}
