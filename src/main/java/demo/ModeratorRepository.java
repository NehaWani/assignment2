package demo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import beans.Moderator;

/**
 * Title: CMPE-273 Assignment 2.
 * Statement: Create a polling application using REST APIs. Use mongodb to persist data.
 * Created by: Neha Wani
 * Created on:  03-25-2015
 * Description: This interface is a moderator repository interface which consists of custom methods to access the moderators collection in mongodb.
 * @author neh
 *
 */
@RepositoryRestResource(collectionResourceRel = "moderators", path = "moderators")
public interface ModeratorRepository extends MongoRepository<Moderator, String> {

	Moderator findById(@Param("id") Integer id);
}
