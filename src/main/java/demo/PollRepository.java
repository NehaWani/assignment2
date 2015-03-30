package demo;

import java.util.ArrayList;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import beans.Poll;

/**
 * Title: CMPE-273 Assignment 2.
 * Statement: Create a polling application using REST APIs. Use mongodb to persist data.
 * Created by: Neha Wani
 * Created on:  03-25-2015
 * Description: This interface is a poll repository interface which consists of custom methods to access the polls collection in mongodb
 * @author neh
 *
 */
@RepositoryRestResource(collectionResourceRel = "polls", path = "polls")
public interface PollRepository extends MongoRepository<Poll, String> {

	Poll findById(@Param("id") String id);
	ArrayList<Poll> findByModeratorId(@Param("moderatorId") int moderatorId);
	
}

