package demo;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.validation.Valid;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import beans.Moderator;
import beans.Poll;


/**
 * Title: CMPE-273 Assignment 2.
 * Statement: Create a polling application using REST APIs. Use mongodb to persist data.
 * Created by: Neha Wani
 * Created on:  03-25-2015
 * Description: This class is a controller class which consists of all necessary APIs required for polling application.
 * @author neh
 *
 */
@RestController
@RequestMapping("/api/v1")
public class ApplicationController {

	/*
	 * 1. Autowired moderator and poll so as to create collections into mongodb.
	 * 2. moderator counter and poll counter to created unique ids for moderator and poll respectively.
	 */
	@Autowired
	private ModeratorRepository moderatorRespository;
	@Autowired
	private PollRepository pollRepository;
	@Autowired
	KafkaProducer kafkaProducer;
	
	private final AtomicInteger moderator_counter = new AtomicInteger();
	private final AtomicLong poll_counter = new AtomicLong();
	TimeZone timezone = TimeZone.getTimeZone("UTC");
	SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd'T'hh:mm:ss.SSS'T'");
	SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	
	/** This method generates a new moderator.
	 * This is a POST method which is mapped to /moderators, consumes a JSON input and saves an entry in mongodb's 'moderators' collection.
	 * @param mod
	 * @return Moderator
	 * @throws Exception
	 */
	@RequestMapping(value="/moderators", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<Moderator> createModerator(@RequestBody @Valid Moderator mod) throws Exception{
		System.out.println("Creating a moderator...");
		Moderator moderator = new Moderator(moderator_counter.incrementAndGet(), mod.getName(), mod.getEmail(), mod.getPassword(), sdf.format(new Date()));
		System.out.println("Moderator: "+moderator.toString());
		moderatorRespository.save(moderator);
		return new ResponseEntity<Moderator>(moderator, HttpStatus.CREATED);
	}
	
	
	/**This method searches for a moderator.
	 * This is a GET method which is mapped to /moderators/{moderator_id} and returns an entry from mongodb corresponding to that id.
	 * @param mod_id
	 * @param authorizationDetail
	 * @return Moderator
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping(value="/moderators/{moderator_id}", method = RequestMethod.GET, produces = "application/json", headers={"accept=application/json"})
	public @ResponseBody ResponseEntity<Moderator> searchModerator(@PathVariable("moderator_id") Integer mod_id, @RequestHeader(value="Authorization") String authorizationDetail) throws UnsupportedEncodingException{
		System.out.println("Searching a specific moderator");
		boolean authenticationSuccess = checkAuthorizationDetail(authorizationDetail);
		if(authenticationSuccess){
			Moderator mod = moderatorRespository.findById(mod_id);
			return new ResponseEntity<Moderator>(mod, HttpStatus.OK);
		}
		return new ResponseEntity<Moderator>(HttpStatus.BAD_REQUEST);
	}
	
	
	/**This method updates a moderator information.
	 * This is a PUT method which is mapped to /moderators/{moderator id}, consumes a JSON input and updates an entry in mongodb's 'moderators' collection.
	 * @param mod_id
	 * @param moderator
	 * @param authorizationDetail
	 * @return Moderator
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping(value="/moderators/{moderator_id}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
	public ResponseEntity<Moderator> updateModerator(@PathVariable("moderator_id") Integer mod_id, @RequestBody Moderator moderator,@RequestHeader(value="Authorization") String authorizationDetail) throws UnsupportedEncodingException{
		System.out.println("Updating a moderator");
		boolean authenticationSuccess = checkAuthorizationDetail(authorizationDetail);
		if(authenticationSuccess){
			Moderator mod = moderatorRespository.findById(mod_id);
			if(mod !=null){
				if(moderator.getName()!=null){
					mod.setName(moderator.getName());
				}
				if(moderator.getEmail()!=null){
					mod.setEmail(moderator.getEmail());
				}
				if(moderator.getPassword()!=null){
					mod.setPassword(moderator.getPassword());
				}
				moderatorRespository.save(mod);
				return new ResponseEntity<Moderator>(mod, HttpStatus.OK);
			}
		}
		return new ResponseEntity<Moderator>(HttpStatus.BAD_REQUEST);
	}
	
	/** This method generates a new poll.
	 * This is a POST method which is mapped to /moderators/moderator_id/polls, consumes a JSON input and saves an entry in mongodb's 'polls' collection.
	 * Only a moderator can create a new poll.
	 * @param mod_id
	 * @param poll
	 * @param authorizationDetail
	 * @return Poll
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping(value="/moderators/{moderator_id}/polls", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<LinkedHashMap<String, Object>> createPoll(@PathVariable("moderator_id") Integer mod_id, @RequestBody Poll poll, @RequestHeader(value="Authorization") String authorizationDetail) throws UnsupportedEncodingException{
		System.out.println("Creating a poll...");
		boolean authenticationSuccess = checkAuthorizationDetail(authorizationDetail);
		LinkedHashMap<String, Object> pollHashMap = new LinkedHashMap<>();
		if(authenticationSuccess){
			double rn = Math.random();
			String poll_id = Long.toHexString((long) ((rn * 987654) + poll_counter.incrementAndGet()));
			Poll poll_new = new Poll(poll_id, poll.getQuestion(), poll.getStarted_at(), poll.getExpired_at(), poll.getChoice(), mod_id);
			int[] temp = new int[poll.getChoice().length]; 
			poll_new.setResults(temp);
		//	System.out.println("poll repo:-------------- "+pollRepository);
			pollRepository.save(poll_new);
			
			Poll pollTemp = pollRepository.findById(poll_id);
			if(pollTemp!= null){
				pollHashMap.put("id", pollTemp.getId());
				pollHashMap.put("question", pollTemp.getQuestion());
				pollHashMap.put("strated_id", pollTemp.getStarted_at());
				pollHashMap.put("expired_at", pollTemp.getExpired_at());
				pollHashMap.put("choice", pollTemp.getChoice());
				return new ResponseEntity<LinkedHashMap<String, Object>>(pollHashMap, HttpStatus.CREATED);
			}
		}
		return new ResponseEntity<LinkedHashMap<String, Object>>(HttpStatus.BAD_REQUEST);
	}
	
	/** This method searches a poll.
	 * This is a GET method which is mapped to /polls/poll_id, consumes an integer input and searches an entry in mongodb for that specific poll.
	 * This poll will be displayed without the results attribute.
	 * @param poll_id
	 * @return Poll
	 */
	@RequestMapping(value="/polls/{poll_id}", method=RequestMethod.GET, produces = "application/json")
	public ResponseEntity<LinkedHashMap<String, Object>> searchPollWithoutResult(@PathVariable("poll_id") String poll_id){
		System.out.println("Searching a poll without results");
		LinkedHashMap<String, Object> pollHashMap = new LinkedHashMap<>();
		Poll pollTemp = pollRepository.findById(poll_id);
		if(pollTemp != null){
			pollHashMap.put("id", pollTemp.getId());
			pollHashMap.put("question", pollTemp.getQuestion());
			pollHashMap.put("started_at", pollTemp.getStarted_at());
			pollHashMap.put("expired_at", pollTemp.getExpired_at());
			pollHashMap.put("choice", pollTemp.getChoice());
			return new ResponseEntity<LinkedHashMap<String, Object>>(pollHashMap, HttpStatus.OK);
		}
		return null;
	}
	
	/** This method searches a poll.
	 * This is a GET method which is mapped to /moderators/{moderator_id}/polls/poll_id, consumes an integer input and searches an entry in mongodb for that specific poll.
	 * This poll will be displayed with the results attribute
	 * @param mod_id
	 * @param poll_id
	 * @param authorizationDetail
	 * @return Poll
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping(value="/moderators/{moderator_id}/polls/{poll_id}", method=RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Poll> searchPollWithResult(@PathVariable("moderator_id") Integer mod_id ,@PathVariable("poll_id") String poll_id, @RequestHeader(value="Authorization") String authorizationDetail) throws UnsupportedEncodingException{
		System.out.println("Searching a poll with results");
		boolean authenticationSuccess = checkAuthorizationDetail(authorizationDetail);
		if(authenticationSuccess){
			Poll pollTemp = pollRepository.findById(poll_id);
			if(pollTemp != null && pollTemp.getModeratorId() == mod_id){
				return new ResponseEntity<Poll>(pollTemp, HttpStatus.OK);
			}
		}
		return new ResponseEntity<Poll>(HttpStatus.BAD_REQUEST);
	}

	/** This method searches a list of polls.
	 * This is a GET method which is mapped to /moderators/{moderator_id}/polls/poll_id, consumes an integer input and searches for all the relevant polls in mongodb.
	 * @param mod_id
	 * @param authorizationDetail
	 * @return list of polls
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping(value="/moderators/{moderator_id}/polls", method=RequestMethod.GET, produces = "application/json")
	public ResponseEntity<ArrayList<Poll>> listPolls(@PathVariable("moderator_id") Integer mod_id, @RequestHeader(value="Authorization") String authorizationDetail) throws UnsupportedEncodingException{
		System.out.println("Listing all polls for a specific moderator");
		boolean authenticationSuccess = checkAuthorizationDetail(authorizationDetail);
		if(authenticationSuccess){
			ArrayList<Poll> pollList = pollRepository.findByModeratorId(mod_id);
			if(pollList != null){
				return new ResponseEntity<ArrayList<Poll>>(pollList, HttpStatus.OK);
			}
		}
		return new ResponseEntity<ArrayList<Poll>>(HttpStatus.BAD_REQUEST);
	}
	
	/** This method deletes a specific poll
	 * @param mod_id
	 * @param poll_id
	 * @param authorizationDetail
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping(value="/moderators/{moderator_id}/polls/{poll_id}", method=RequestMethod.DELETE)
	public ResponseEntity<String> deletePoll(@PathVariable("moderator_id") Integer mod_id ,@PathVariable("poll_id") String poll_id, @RequestHeader(value="Authorization") String authorizationDetail) throws UnsupportedEncodingException{
		System.out.println("Deleting a poll...");
		boolean authenticationSuccess = checkAuthorizationDetail(authorizationDetail);
		if(authenticationSuccess){
			if(pollRepository.findById(poll_id).getModeratorId() == mod_id){
				pollRepository.delete(poll_id);
				return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
			}
		}
		return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
	}
	
	/** This method records the polling choice for a particular poll question.
	 * @param poll_id
	 * @param choice
	 * @return
	 */
	@RequestMapping(value="/polls/{poll_id}", method=RequestMethod.PUT)
	public ResponseEntity<String> votePoll(@PathVariable("poll_id") String poll_id, @RequestParam("choice") Integer choice){
		System.out.println("Voting on a poll...");
		if(pollRepository.exists(poll_id)){
			Poll pollTemp = pollRepository.findById(poll_id);
			pollTemp.getResults()[choice]++;
			pollRepository.save(pollTemp);
			return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
	}
	
	public boolean checkAuthorizationDetail(String authorizationDetail) throws UnsupportedEncodingException {
		String[] authorizationDetailArray = authorizationDetail.split(" ");
		byte[] decodedString = Base64.decodeBase64(authorizationDetailArray[1]);
		String authorizationString = new String(decodedString, "UTF-8");
		if (authorizationString.indexOf(":") > 0) {
			String[] credentials = authorizationString.split(":");
			String username = credentials[0];
			String password = credentials[1];
			if (username.equals("foo") && password.equals("bar"))
				return true;
		}
		return false;
	}

	
	@Scheduled(fixedRate = 30000)
    public void checkPollExpiration() {
		System.out.println("Checking for poll expiration...");
		List<Poll> expiredPollList = new ArrayList<Poll>();
		expiredPollList = getExpiredPollList();
//		try{
		if(expiredPollList.isEmpty()){
			System.out.println("No polls are expired yet!");
		}
		else{
			for(Poll poll:expiredPollList){
				String pollResult = "";
				for(int i=0;i<poll.getResults().length;i++){
					pollResult += poll.getChoice()[i] + "=" + poll.getResults()[i];
					if(i != pollResult.length()-1){
						pollResult += ", ";
					}
				}
				Moderator moderator = moderatorRespository.findById(poll.getModeratorId());
				if(!poll.isMailSent()){
					String msg = moderator.getEmail() + ":" + "010038847" + ":" + "Poll Result [" + pollResult + "]";
					System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+msg);
					kafkaProducer.sendMessage(msg);
					poll.setMailSent(true);
					pollRepository.save(poll);
					System.out.println("Mail sent...");
				}
				else{
					System.out.println("Email has been sent already!");
				}
			}
	//	}
		}
/*		catch(Exception e){
			e.printStackTrace();
		}
*/    }
	
	public List<Poll> getExpiredPollList(){
		System.out.println("In expired poll list method...");
		ArrayList<Poll> pollList = (ArrayList<Poll>) pollRepository.findAll();
		List<Poll> expiredPollList = new ArrayList<Poll>();
		Date pollDate = null;
		Date currentDate = new Date();
		
			for(Poll currentPoll:pollList){
				String expiredDate = currentPoll.getExpired_at();
				try {
					pollDate = sdf2.parse(expiredDate);
				}
				catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(currentDate.compareTo(pollDate) == 1){
					expiredPollList.add(currentPoll);
				}
			}
		System.out.println("expired poll count: "+expiredPollList.size());
		return expiredPollList;
	}
	
}