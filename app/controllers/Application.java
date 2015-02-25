package controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import models.Bet;
import models.Bookmakers;
import models.Event;
import models.Races;
import models.Results;
import models.Runners;
import models.Tournament;
import models.User;
import models.UserBet;
import models.UserBetDetails;
import models.WinResults;


import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import play.Play;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import viewmodel.BetVM;
import viewmodel.BookmakersVM;
import viewmodel.EventVM;
import viewmodel.RaceVM;
import viewmodel.RunnerVM;
import viewmodel.TournamentVM;
import viewmodel.WinResultsVM;
import viewmodel.saveBetVM;
import views.html.index;
import viewmodel.Scores;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;
import com.feth.play.module.mail.Mailer;
import com.feth.play.module.mail.Mailer.Mail.Body;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;

import controllers.Application.ResultsVM.Rank;

public class Application extends Controller {
  
	public static final String lCertificate = Play.application().configuration().getString("certificate_loc");
	
    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }
  
    public static Result readXMLFile() {
    	System.out.println("called.................");
    	try {
    	
    	URL url = new URL("http://xml.betfred.com/horse-racing-uk.xml");
    	BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    	Document doc = dBuilder.parse(url.openStream());
    	 
     
    	NodeList nList = doc.getElementsByTagName("event");
     
    	for (int temp = 0; temp < nList.getLength(); temp++) {
     
    		Node nNode = nList.item(temp);
    		
    		Event event = new Event();
    		List<Bet> betList = new ArrayList<>();
    		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
    		     
    			Element eElement = (Element) nNode;
    			
    			event.name = eElement.getAttribute("name");
    			event.eventId = eElement.getAttribute("eventid");
    			String str = eElement.getAttribute("date");
    			SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd");
    			Calendar cal = Calendar.getInstance();
    			Date date = dt.parse(str);
    			cal.setTime(date);
    			event.eventDate = cal.getTime();
    			int hr = Integer.parseInt(eElement.getAttribute("time").substring(0, 2));
    			int mnt = Integer.parseInt(eElement.getAttribute("time").substring(2, 4));
    			cal.set(cal.HOUR_OF_DAY,hr);
    			cal.set(cal.MINUTE, mnt);
    			event.eventTime = cal.getTime();
    			event.meeting = eElement.getAttribute("meeting");
    			event.venue = eElement.getAttribute("venue");
    		}
    		NodeList lst = nNode.getChildNodes();
    		for(int i = 0; i< lst.getLength(); i++) {
    			Node node = lst.item(i);
    			if (node.getNodeType() == Node.ELEMENT_NODE) {
    				Element eElement = (Element) node;
    				String str2 = eElement.getAttribute("bet-start-date");
        			SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd");
        			Date date2 = dt.parse(str2);
        			Calendar cal2 = Calendar.getInstance();
        			cal2.setTime(date2);
        			event.betStartDate = cal2.getTime();
        			cal2.set(cal2.HOUR_OF_DAY,Integer.parseInt(eElement.getAttribute("bet-start-time").substring(0, 2)));
        			cal2.set(cal2.MINUTE, Integer.parseInt(eElement.getAttribute("bet-start-time").substring(2, 4)));
        			event.betStartTime = cal2.getTime();
        			event.betName = eElement.getAttribute("name");
        			event.betTypeId =  eElement.getAttribute("bettypeid");
    			}
    			NodeList chlist = node.getChildNodes();
    			
    			for(int j = 0; j<chlist.getLength(); j++) {
    				Node childNode = chlist.item(j);
    				Bet bet = new Bet();
    				if (childNode.getNodeType() == Node.ELEMENT_NODE) {
    				    try {	
                                        Element eElement = (Element) childNode;
    					bet.name = eElement.getAttribute("name");
    					bet.shortName = eElement.getAttribute("short-name");
    					bet.betId = eElement.getAttribute("id");
    					bet.price = eElement.getAttribute("price");
    					if(eElement.hasAttribute("priceDecimal")) {
    						bet.priceDecimal = Float.parseFloat(eElement.getAttribute("priceDecimal"));
    					}
    					if(eElement.hasAttribute("priceUS")) {
    						bet.priceUs = Float.parseFloat(eElement.getAttribute("priceUS"));
    					}
    					if(eElement.hasAttribute("active-price-types")) {
    						bet.activePriceTypes = eElement.getAttribute("active-price-types");
    					}
    					Bet betObject = Bet.findByBetId(bet.betId);
    					if(betObject != null) {
    						betObject.shortName = bet.shortName;
    						betObject.price = bet.price;
    						betObject.priceDecimal = bet.priceDecimal;
    						betObject.priceUs = bet.priceUs;
    						betObject.activePriceTypes = bet.activePriceTypes;
    						Ebean.update(betObject);
    					} else {
    						bet.event = event;
        					betList.add(bet);
    					}
                                     } catch(Exception e) {
                                         e.printStackTrace();
                                     }
    				}
    			}
    		}
    		event.bet = betList;
    		Event eventObject = Event.findByEventId(event.eventId);
    		if(eventObject != null) {
    			eventObject.name = event.name;
    			eventObject.eventId = event.eventId;
    			eventObject.eventDate = event.eventDate;
    			eventObject.eventTime = event.eventTime;
    			eventObject.meeting = event.meeting;
    			eventObject.venue = event.venue;
    			eventObject.betStartDate = event.betStartDate;
    			eventObject.betStartTime = event.betStartTime;
    			eventObject.betName = event.betName;
    			eventObject.betTypeId = event.betTypeId;
    			eventObject.bet = event.bet;
    			Ebean.update(eventObject);
    		} else {
    			Ebean.save(event);
    		}
    	}
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    	return ok();
    }
    
    public static Result getVenuesByDate(String date) {
    	SimpleDateFormat dt = new SimpleDateFormat("yyyy-mm-dd");
    	List<String> venueList = new ArrayList<>();
    	try {
			Date eventDate = dt.parse(date);
			List<SqlRow> list = Event.getVenues(eventDate);
			for(SqlRow row: list) {
				venueList.add(row.getString("venue"));
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
    	return ok(Json.toJson(venueList));
    }
    
    public static Result getGameByVenueAndDate(String venue,String date) {
    	SimpleDateFormat dt = new SimpleDateFormat("yyyy-mm-dd");
    	SimpleDateFormat time = new SimpleDateFormat("hh:mm");
    	List<EventVM> vmList = new ArrayList<>();
    	List<Event> list = new ArrayList<>();
    	try {
			Date eventDate = dt.parse(date);
			list = Event.getEventByDateAndVenue(venue, eventDate);
			for(Event evt : list) {
				EventVM vm = new EventVM();
				vm.id = evt.id;
				vm.name = evt.name;
				vm.eventId = evt.eventId;
				vm.venue = evt.venue;
				vm.meeting = evt.meeting;
				vm.betName = evt.betName;
				vm.betTypeId = evt.betTypeId;
				vm.eventDate = dt.format(evt.eventDate);
				vm.eventTime = time.format(evt.eventTime);
				vm.betStartDate = dt.format(evt.betStartDate);
				vm.betStartTime = time.format(evt.betStartTime);
				vmList.add(vm);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
    	return ok(Json.toJson(vmList));
    }
    
    public static Result getHorsesForGameID(String eventId) {
    	Event event = Event.findByEventId(eventId);
    	List<Bet> list = Bet.getByEventId(event);
    	List<BetVM> vmList = new ArrayList<>();
    	for(Bet bet : list) {
    		BetVM vm = new BetVM();
    		vm.id = bet.id;
    		vm.activePriceTypes = bet.activePriceTypes;
    		vm.name = bet.name;
    		vm.shortName = bet.shortName;
    		vm.price = bet.price;
    		vm.priceDecimal = bet.priceDecimal;
    		vm.priceUs = bet.priceUs;
    		vm.betId = bet.betId;
    		vmList.add(vm);
    	}
    	return ok(Json.toJson(vmList));
    }
    
    public static Result register() {
    	try{
    		Form<RegisterForm> form = DynamicForm.form(RegisterForm.class).bindFromRequest();
    		RegisterForm rForm = form.get();
    		rForm.firstName = form.data().get("firstName");
    		rForm.lastName = form.data().get("lastName");
    		rForm.email = form.data().get("email");
    		rForm.password = form.data().get("password");
    		if(     rForm.firstName.isEmpty()||
    				rForm.firstName==null||
					rForm.lastName.isEmpty()||
    				rForm.lastName==null||
    				rForm.password.isEmpty()||
    	    		rForm.password==null||
    				rForm.email==null||
    				rForm.email.isEmpty()) {
    			return ok(Json.toJson(new ErrorResponse(Error.E202.getCode(), Error.E202.getMessage())));
    		} else {
    			
    			if(User.findByUserEmail(rForm.email)!=null) {
    				return ok(Json.toJson(new ErrorResponse(Error.E210.getCode(), Error.E210.getMessage())));
    			} 
    			
    			User userObj = new User();
    			userObj.firstName = rForm.firstName;
    			userObj.lastName = rForm.lastName;
    			userObj.password = User.md5Encryption(rForm.password);
    			userObj.email = rForm.email;
    			userObj.save();
    			
    			return ok(Json.toJson(new ErrorResponse(Error.E204.getCode(), Error.E204.getMessage())));
    		}
    	} catch(Exception e) {
    		return ok(Json.toJson(new ErrorResponse("500",e.getMessage())));
    	}
    }
    
    
    public static Result saveDeviceToken() {
    	
    	Map<String, String> map = new HashMap<>();
    	
    	JsonNode json = request().body().asJson();
        String email = json.path("email").asText();
    	String token = json.path("token").asText();
    	
    	User user = User.findByUserEmail(email);
    	if(user != null){
    		user.idevice = token;
    		user.update();
    	}else{
    		map.put("201", "User Does Not Exit! ");
    		return ok(Json.toJson(map));
    	}
    	map.put("200", "IDEVICE Token save successfully! ");
		return ok(Json.toJson(map));
    }
    
    public static class RegisterForm {
    	public String firstName;
    	public String lastName;
    	public String password;
    	public String email;
    }
    
    public static class ErrorResponse {
    	public String code;
    	public String message;
    	public ErrorResponse(String code,String message) {
    		this.code = code;
    		this.message = message;
    	}
    }
    
    public enum Error {
    	E201("201","Login Failed!"),
    	E202("202","Required Field Missing!"),
    	E200("200","Login Successful!"),
    	E203("203","Invalid Country Code"),
    	E204("204","User Registered Successfully!"),
    	E205("205","User is not verified yet"),
    	E206("206","Mobile Number is not Valid Number!"),
    	E207("207","Verification Code is Invalid!"),
    	E208("208","Username, Password does'nt matched with our database!"),
    	E209("209","User Validated Successfully!"),
    	E210("210","User Already Exist!"),
    	E211("211","User Does Not Exist");
    	Error(String code,String message) {
    		this.code = code;
    		this.message = message;
    	}
    	
    	private String code;
    	private String message;
		
    	public String getCode() {
			return code;
		}
		public String getMessage() {
			return message;
		}
    	
    }
    
    public static class LoginForm {
    	public String email;
    	public String password;
    }
    
    public static Result login() {
    	try {
    		Form<LoginForm> form = DynamicForm.form(LoginForm.class).bindFromRequest();
    		String email = form.data().get("email");
    		String password = form.data().get("password");
    		if(email==null || email.isEmpty() || password==null || password.isEmpty()) {
    			return ok(Json.toJson(new ErrorResponse(Error.E202.getCode(), Error.E202.getMessage())));
    		} else {
    			
    			User user = User.getUserByUserNameAndPassword(email, password);
    			if(user == null) {
    				return ok(Json.toJson(new ErrorResponse(Error.E201.getCode(),Error.E201.getMessage())));
    			}
    			Map<String, Object> map = new HashMap<>();
    			map.put("message", Error.E200.getMessage());
    			map.put("email", email);
    			map.put("firstName", user.firstName);
    			map.put("lastName", user.lastName);
    			return ok(Json.toJson(map));
    		}
    	} catch(Exception e) {
    		return ok(Json.toJson(new ErrorResponse("500",e.getMessage())));
    	} 
    }
    
    public static class ResultsVM {
    	public Long id;
    	public String eventDate;
    	public String eventTime;
    	public String version;
    	public String meeting;
    	public String venue;
    	public List<Rank> results = new ArrayList<>();
    	
    	public static class Rank{
    		public int position;
    		public String name;
    		public String odds;
    		public String jockey;
    		public String trainer;
    	}
    }
    
    public static Result getResults() {
    	/*Map<String, String> map = new HashMap<>();
    	List<WinResults> win = WinResults.getAllWinResult();
    	
    	for(WinResults winres: win) {
    		List<UserBet> uBet = UserBet.getUserBetByRaceId(winres.raceid);
    		Races races = Races.getRaceByraceId(winres.raceid);
    		for(UserBet userbet: uBet) {
    			if(userbet.raceId.equals(winres.raceid) && userbet.horseId.equals(winres.horseid)) {
    				String pos = winres.position;
    				if(pos.equals("1")) {
    					final Body body = new Body("You won for bet: "+winres.name+"for date and time: "+winres.version);
				        Mailer.getDefaultMailer().sendMail("Bet result",
				            body, userbet.user.email);
    				}
    				if(pos.equals("2")) {
    					final Body body = new Body("Your position 2 for bet: "+winres.name+"    for date and time: "+winres.version);
				        Mailer.getDefaultMailer().sendMail("Bet result",
				            body, userbet.user.email);
    				}
    				if(pos.equals("3")) {
    					final Body body = new Body("Your position 3 for bet: "+winres.name+"    for date and time: "+winres.version);
				        Mailer.getDefaultMailer().sendMail("Bet result",
				            body, userbet.user.email);
    				}
    			} else {
    				final Body body = new Body("You lose for bet: "+winres.name+"    for date and time: "+winres.version);
			        Mailer.getDefaultMailer().sendMail("Bet result",
			            body, userbet.user.email);
    			}
    			String token = userbet.user.idevice;
    			if(token != null){
    				String msg = "Bet Name :"+winres.name+"Race Name :"+races.name+"Horse Name : "+winres.name;
    				sendPushNotification(token,msg);
    			}
    		}
    		winres.flag = true;
    		winres.update();
    	}
    	
			map.put("200", "USER WON RESULT");*/
			return ok();
		
    }
    
   /* public static Result getResults() throws ParseException{
    	org.jsoup.nodes.Document doc = null;
		try {
			doc = Jsoup.connect("http://betfred.chromaagency.com/results").get();
		} catch (IOException e) {
			e.printStackTrace();
		}
		org.jsoup.nodes.Element main = doc.getElementById("main");
		Elements tables = main.getElementsByClass("table10");
		Date d = new Date();
		SimpleDateFormat dtdf = new SimpleDateFormat("yyyy-MM-dd");
    	SimpleDateFormat timedf = new SimpleDateFormat("HH:mm");
		for(org.jsoup.nodes.Element t:tables){
			Results res = new Results();
			boolean flag = false;
			org.jsoup.nodes.Element row = t.getElementsByTag("tr").first();
			Elements columns = row.children();
			Map<String, Object> mainMap = new HashMap<>();
			List<Map<String,Object>> maps = new ArrayList<>();
			for(org.jsoup.nodes.Element c:columns){
				if(columns.indexOf(c) == 0){
					org.jsoup.nodes.Element time = c.getElementsByTag("strong").first();
					String[] timeVenue = time.text().split(" ", 2);
					if (timeVenue[1].indexOf("Abandoned") == -1){
						flag = true;
					}
					res.venue = timeVenue[1];
					res.meeting = timeVenue[1];
					String[] strArr = timeVenue[0].split("\\.", 2);
					Calendar cal = Calendar.getInstance();
	    			cal.setTime(d);
	    			cal.set(cal.HOUR_OF_DAY,0);
	    			cal.set(cal.MINUTE,0);
	    			cal.set(cal.SECOND,0);
	    			d = cal.getTime();
	    			res.eventDate = cal.getTime();
	    			cal.set(cal.HOUR_OF_DAY, Integer.parseInt(strArr[0]));
	    			cal.set(cal.MINUTE, Integer.parseInt(strArr[1]));
	    			cal.set(cal.SECOND,0);
	    			res.eventTime = cal.getTime();
				}
				if(columns.indexOf(c) == 1){
					Elements ranks = c.getElementsByTag("tr");
					int count = 1;
					for(org.jsoup.nodes.Element r:ranks){
						Map<String, Object> map = new HashMap<>();
						map.put("position", count++);
						org.jsoup.nodes.Element info = r.getElementsByTag("td").get(3);
						Elements data = info.getElementsByTag("li"); 
						for(org.jsoup.nodes.Element i:data){
							if(data.indexOf(i) == 0){
								map.put("name", i.children().first().text());
							}
							if(data.indexOf(i) == 1){
								map.put("odds", i.text());
							}
							if(data.indexOf(i) == 2){
								map.put("trainer", i.children().first().text());
								map.put("jockey", i.children().last().text());
							}
						}
						maps.add(map);
					}
				}
			}
			mainMap.put("results", maps);
			JSONObject obj = new JSONObject(mainMap);
			res.results = obj.toString();
			Results res1 = Results.findByDateTimeVenue(res.eventDate,res.eventTime,res.meeting);
			if(res1 == null && flag == true){
				res.save();
				
				List<UserBet> userBets = UserBet.getUserBetByRaceId(raceId)
		    	
				try {
					obj = new JSONObject(res.results);
					JSONArray array = (JSONArray)obj.get("results");
					
					for(UserBet userbet: userBets) {
						
			    		Bet bet = Bet.findByBetId(userbet.raceId);
			    		
			    		for(int i=0;i<array.length();i++) {
			    			
			    			JSONObject obj2 = (JSONObject)array.get(i);
			    			
			    			if(bet.name.equals(obj2.get("name"))) {
			    				int pos = (Integer)obj2.get("position");
			    				if(pos == 1) {
			    					final Body body = new Body("You won for bet: "+bet.name+"    for date and time: "+bet.event.betStartTime+"    venue :"+bet.event.venue);
							        Mailer.getDefaultMailer().sendMail("Bet result",
							            body, userbet.user.email);
			    				}
			    				if(pos == 2) {
			    					final Body body = new Body("Your position 2 for bet: "+bet.name+"    for date and time: "+bet.event.betStartTime+"    venue :"+bet.event.venue);
							        Mailer.getDefaultMailer().sendMail("Bet result",
							            body, userbet.user.email);
			    				}
			    				if(pos == 3) {
			    					final Body body = new Body("Your position 3 for bet: "+bet.name+"    for date and time: "+bet.event.betStartTime+"    venue :"+bet.event.venue);
							        Mailer.getDefaultMailer().sendMail("Bet result",
							            body, userbet.user.email);
			    				}
			    			} else {
			    				final Body body = new Body("You lose for bet: "+bet.name+"    for date and time: "+bet.event.betStartTime+"    venue :"+bet.event.venue);
						        Mailer.getDefaultMailer().sendMail("Bet result",
						            body, userbet.user.email);
			    			}
			    		}
			    	}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		List<ResultsVM> results = new ArrayList<>();
		List<Results> resList = Results.findByEventDate(d);
		for(Results r:resList){
			ResultsVM vm = new ResultsVM();
			vm.id =r.id;
			vm.eventDate = r.eventDate.toString();
			vm.eventTime = r.eventTime.toString();
			vm.meeting = r.meeting;
			vm.venue = r.venue;
			vm.version = r.version.toString();
			JsonNode node = Json.parse(r.results);
			JsonNode ranking = node.path("results");
			ArrayNode positions = (ArrayNode) ranking;
			for(int i=0;i<positions.size();i++){
				JsonNode horse = positions.get(i);
			    Rank extra = new Rank();
			    extra.position = horse.path("position").asInt();
			    extra.name = horse.path("name").asText();
			    extra.odds = horse.path("odds").asText();
			    extra.jockey = horse.path("jockey").asText();
			    extra.trainer = horse.path("trainer").asText();
			    vm.results.add(extra);
			}
			results.add(vm);
		}
		if(results.size() == 0){
			return ok("No matches today.");
		}
    	return ok(Json.toJson(results));
    }*/
    
    public static Result sendPushNotification(String deviceToken, String msg) {
        System.out.println("sendPushNotification " + lCertificate);
        String password = "";
        ApnsService service =
                APNS.newService()
                .withCert(lCertificate, password)
                .withSandboxDestination()
                .build();
        System.out.println("sendPushNotification");
        String payload = APNS.newPayload().alertBody(msg).build();
        com.notnoop.apns.ApnsNotification notification = service.push(deviceToken, payload);
        System.out.println("Sending notification message!");
        return ok();
    }
        
    public static Result saveBet() {
    	
    	Map<String, String> map = new HashMap<>();
    	
    	JsonNode json = request().body().asJson();
        System.out.println("bets === "+json);
        String email = json.path("email").asText();
    	System.out.println("bet email == "+email);
    	 String betname = json.path("betname").asText();
     	System.out.println("bet email == "+betname);
     	String raceid = json.path("raceid").asText();
     	System.out.println("bet email == "+raceid);
    	JsonNode bets = json.path("bets");
    	 ArrayNode items = (ArrayNode) bets;
    	 System.out.println("bets == "+bets);
        User user = User.findByUserEmail(email);
        UserBet userBet = new UserBet();
		userBet.user = user;
		userBet.raceId = raceid;
		userBet.betName = betname;
		userBet.save();
        for(int i=0;i<items.size();i++){
        	if(user != null) {
	    		UserBetDetails userBetDetails = new UserBetDetails();
	    		JsonNode node = items.get(i);
	    		userBetDetails.userBet = userBet;
	    		userBetDetails.horseId = node.path("horseid").asText();
	    		userBetDetails.save();
	    		
			} else {
				map.put("210", "User Not Exit!");
				return ok(Json.toJson(map));
			}
        	
        }
        user.userBet.add(userBet);
		user.update();
    	map.put("200", "User bet saved successfully!");
    	return ok(Json.toJson(map));
    }
    
    public static class ChangePasswordForm {
    	public String email;
    	public String oldPassword;
    	public String newPassword;
    }
    public static Result changePassword() {
    	Map<String, String> map = new HashMap<>();
    	Form<ChangePasswordForm> form = DynamicForm.form(ChangePasswordForm.class).bindFromRequest();
    	ChangePasswordForm rForm = form.get();
		if(     rForm.email==null||
				rForm.email.isEmpty() ||
				rForm.oldPassword==null ||
				rForm.oldPassword.isEmpty() ||
				rForm.newPassword==null ||
				rForm.newPassword.isEmpty()) {
			return ok(Json.toJson(new ErrorResponse(Error.E202.getCode(), Error.E202.getMessage())));
		} else {
			try {
				
					User user = User.getUserByUserNameAndPassword(rForm.email, rForm.oldPassword);
					if(user != null) {
						user.password = User.md5Encryption(rForm.newPassword);
						user.update();
						final Body body = new Body("your new password is    "+rForm.newPassword);
				        Mailer.getDefaultMailer().sendMail("Password changed",
				            body, user.email);
						map.put("200", "Password changed successfully!");
			    		return ok(Json.toJson(map));
					} else {
						map.put("201", "Invalid user name or password!");
			    		return ok(Json.toJson(map));
					}
					
			} catch(Exception e) {
				map.put("500", e.getMessage());
	    		return ok(Json.toJson(map));
			}
		}
    	
    }
    
    public static Result forgotPassword() {
    	Map<String, String> map = new HashMap<>();
    	Form<ChangePasswordForm> form = DynamicForm.form(ChangePasswordForm.class).bindFromRequest();
    	ChangePasswordForm rForm = form.get();
		if(     rForm.email==null||
				rForm.email.isEmpty()) {
			return ok(Json.toJson(new ErrorResponse(Error.E202.getCode(), Error.E202.getMessage())));
		} else {
			try {
				
					User user = User.findByUserEmail(rForm.email);
					if(user != null) {
						user.password = User.md5Encryption("testing123");
						user.update();
						final Body body = new Body("your new password is  :  "+"testing123");
				        Mailer.getDefaultMailer().sendMail("New Password",
				            body, user.email);
						map.put("200", " Generate New Password !");
			    		return ok(Json.toJson(map));
					} else {
						map.put("201", "Invalid user name!");
			    		return ok(Json.toJson(map));
					}
					
			} catch(Exception e) {
				map.put("500", e.getMessage());
	    		return ok(Json.toJson(map));
			}
		}
    	
    }
    
    
    
    public static Result getTournamentDetails() throws IOException, ParseException {
    	try {
				URL url = new URL("http://www.goalserve.com/getfeed/21321323aa084872a8edfe9a50ed1ac8/racing/uk");
		    	//File file = new File("racing.xml");
		    	//FileUtils.copyURLToFile(url, file);
				JAXBContext jaxbContext = JAXBContext.newInstance(Scores.class);
				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				Scores scores = (Scores) jaxbUnmarshaller.unmarshal(url);
				SimpleDateFormat df1 = new SimpleDateFormat("dd.MM.yyyy");
				for(Scores.Tournament tournament :scores.getTournament()) {
					Tournament t = new Tournament();
					t.name = tournament.getName();
					t.tournamentId = tournament.getId();
					if(!tournament.getDate().equals("")) {
						t.date = df1.parse(tournament.getDate());
					}
					List<Races> races = new ArrayList<>();
					SimpleDateFormat df2 = new SimpleDateFormat("dd.MM.yyyy");
					for(Scores.Tournament.Race race:tournament.getRace()) {
						Races r = new Races();
						r.name = race.getName();
						r.tid = tournament.getId();
						r.raceid = race.getId();
						if(!race.getDatetime().equals("")) {
							r.dateTime = df2.parse(race.getDatetime());
						}
						List<Runners> runner  = new ArrayList<>();
						List<WinResults> winresults  = new ArrayList<>();
						for(Scores.Tournament.Race.Runners.Horse horse:race.getRunners().getHorse()) {
							Runners run = new Runners();
							run.name = horse.getName();
							run.raceid = race.getId();
							run.number = horse.getNumber();
							run.jockey = horse.getJockey();
							run.horseId = horse.getId();
							runner.add(run);
						}
						for(Scores.Tournament.Race.Results.Horse rs:race.getResults().getHorse()) {
							WinResults winres = new WinResults();
							winres.name = rs.getName();
							winres.wgt = rs.getWgt();
							winres.position = rs.getPos();
							winres.jockey = rs.getJockey();
							winres.number = rs.getNumber();
							winres.raceid = race.getId();
							winres.horseid = rs.getId();
							winres.flag = false;
							winresults.add(winres);
						}
						r.runners = runner;
						r.winResults = winresults;
						races.add(r);
					}
						
					t.races = races;
					Tournament tnmt = Tournament.getTournament(t.name, t.date, t.tournamentId);
					if(tnmt == null) {
						t.save();
					}
					
				}
				
				for(Scores.Tournament tournament :scores.getTournament()) {
					Date d = df1.parse(tournament.getDate());
					for(Scores.Tournament.Race race:tournament.getRace()) {
						for(Scores.Tournament.Race.Runners.Horse horse:race.getRunners().getHorse()) {
							Runners horseObj = Runners.getByHorseId(horse.getId(),race.getId());
							List<Bookmakers> bookmakersList = new ArrayList<>();
								for(Scores.Tournament.Race.Odds.Horse hrs : race.getOdds().getHorse()){
									if(horseObj.horseId.equals(hrs.getId())){
									for(Scores.Tournament.Race.Odds.Horse.Bookmakers.Bookmaker obj: hrs.getBookmakers().getBookmaker()) {
										Bookmakers bookmakerObj = new Bookmakers();
										bookmakerObj.name = obj.getName();
										bookmakerObj.odd = obj.getOdd();
										bookmakerObj.bookmakerId = obj.getBookmakerId();
										bookmakerObj.oddId = obj.getOddId();
										Bookmakers bk = Bookmakers.getBookmakers(bookmakerObj.name, bookmakerObj.odd, bookmakerObj.oddId, bookmakerObj.bookmakerId, horseObj);
										if(bk == null){
											bookmakersList.add(bookmakerObj);
										}
									}
										horseObj.bookmakers = bookmakersList;
										horseObj.update();
									}
					  }
								
				 }
			}			
						
		}
				
	  } catch (JAXBException e) {
		e.printStackTrace();
	  }
    	return ok();
    }
    
    
    public static Result getTournament(String date) throws ParseException {
    	System.out.println("Date ---"+date);
    	SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
    	Date tournamentDate = dt.parse(date);
    	Calendar cal = Calendar.getInstance();
    	cal.setTime(tournamentDate);
    	cal.set(Calendar.HOUR_OF_DAY,0);
    	cal.set(Calendar.MINUTE,0);
    	cal.set(Calendar.SECOND,0);
    	cal.set(Calendar.MILLISECOND,0);

    	Date d = cal.getTime();
    	
    	List<TournamentVM> tournamentresults = new ArrayList<>();
    	List<Tournament> tList = Tournament.getTournamentByDate(d);
		for(Tournament tr:tList){
			TournamentVM tvm = new TournamentVM();
			tvm.id = tr.id;
			tvm.name = tr.name;
			tvm.date = tr.date;
			tvm.tournamentId = tr.tournamentId;
			tvm.version = tr.version;
			tournamentresults.add(tvm);
		}
    	
    	return ok(Json.toJson(tournamentresults));
    }
    
    
    public static Result getRacesForTournament(String Id) throws ParseException {
    	/*SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
    	Date tournamentDate = dt.parse(date);
    	Calendar cal = Calendar.getInstance();
    	cal.setTime(tournamentDate);
    	cal.set(Calendar.HOUR_OF_DAY,0);
    	cal.set(Calendar.MINUTE,0);
    	cal.set(Calendar.SECOND,0);
    	cal.set(Calendar.MILLISECOND,0);

    	Date d = cal.getTime();*/
    	List<Tournament> tList = Tournament.getTournamentById(Id);
    	//List<Tournament> tList = Tournament.getTournamentByDate(d);
    	List<RaceVM> raceresults = new ArrayList<>();
		for(Tournament tr:tList){
			List<Races> RaceList = Races.getRaceByTId(tr);
				for(Races rac:RaceList){
				RaceVM raceVM = new RaceVM();
				raceVM.id = rac.id;
				raceVM.name = rac.name;
				raceVM.dateTime = rac.dateTime;
				raceVM.tournamentId = rac.tid;
				raceresults.add(raceVM);
			}
		}
    	
    	
    	return ok(Json.toJson(raceresults));
    }
    
    
    public static Result getRunnersForRaces(String Id) throws ParseException {
    	//Races races = Races.getRaceById(Id);
    	List<Runners> runnerList = Runners.getByRunnerById(Id);
    	List<RunnerVM> runnerresults = new ArrayList<>();
		for(Runners run:runnerList){
			RunnerVM rn = new RunnerVM();
			rn.id = run.id;
			rn.name = run.name;
			rn.jockey = run.jockey;
			rn.horseId = run.horseId;
			List<Bookmakers> bookmakerList = Bookmakers.getBookmakersByRunnerId(run);
				for(Bookmakers bookm:bookmakerList){
				BookmakersVM bookVM = new BookmakersVM();
				bookVM.id = bookm.id;
				bookVM.name = bookm.name;
				bookVM.odd = bookm.odd;
				bookVM.oddId = bookm.oddId;
				bookVM.bookmakerId = bookm.bookmakerId;
				
				rn.bookmakersVM.add(bookVM);
			}
			runnerresults.add(rn);	
		}
		
    	
    	
    	return ok(Json.toJson(runnerresults));
    }
    
    
    
    public static Result getWinResultByDate(String date) throws ParseException {
    	System.out.println("Date ---"+date);
    	SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
    	Date raceDate = dt.parse(date);
    	Calendar cal = Calendar.getInstance();
    	cal.setTime(raceDate);
    	cal.set(Calendar.HOUR_OF_DAY,0);
    	cal.set(Calendar.MINUTE,0);
    	cal.set(Calendar.SECOND,0);
    	cal.set(Calendar.MILLISECOND,0);
    	Date d = cal.getTime();
    	List<TournamentVM> winrs = new ArrayList<>();
    	List<Tournament> tournaments = Tournament.getTournamentByDate(d);
    	for(Tournament trn:tournaments){
    		if(trn.races.size() > 0){
    			TournamentVM tr = new TournamentVM();
    			tr.name = trn.name;
    			tr.tournamentId = trn.tournamentId;
    			for(Races rs:trn.races){
    				List<WinResults> winResults = WinResults.getresulttByRaceId(rs.raceid);
    				if(winResults.size() > 0){	
    					RaceVM rc = new RaceVM();
    					rc.raceId = rs.raceid;
    					rc.name = rs.name;
    					rc.winResultsVMs = new ArrayList<WinResultsVM>();
    					Calendar cal1 = Calendar.getInstance();
    					cal1.setTime(winResults.get(0).version);
    					for(WinResults win:winResults){
    						Calendar cal2 = Calendar.getInstance();
    						cal2.setTime(win.version);
    						if(cal2.get(Calendar.YEAR)==cal1.get(Calendar.YEAR)&& cal2.get(Calendar.DAY_OF_YEAR)==cal1.get(Calendar.DAY_OF_YEAR)){
    							WinResultsVM winResultsVM = new WinResultsVM();
    							winResultsVM.id = win.id;
    							winResultsVM.name = win.name;
    							winResultsVM.jockey = win.jockey;
    							winResultsVM.position = win.position;
    							winResultsVM.number = win.number;
    							winResultsVM.wgt = win.wgt;
    							winResultsVM.raceid = win.raceid;
    							rc.winResultsVMs.add(winResultsVM);
    						}else{
    							break;
    						}
    					}
    					tr.allRaces.add(rc);
    				}
    			}
    			if(tr.allRaces.size()>0) {
    				winrs.add(tr);
    			}
    		}
    	}	
    	return ok(Json.toJson(winrs));
    }
    
    public static Result getWinResultById(String id) throws ParseException {
    	List<WinResultsVM> winrs = new ArrayList<>();
			List<WinResults> winResults = WinResults.getresulttByRaceId(id);
				for(WinResults win:winResults){
				WinResultsVM winResultsVM = new WinResultsVM();
				winResultsVM.id = win.id;
				winResultsVM.name = win.name;
				winResultsVM.jockey = win.jockey;
				winResultsVM.position = win.position;
				winResultsVM.number = win.number;
				winResultsVM.wgt = win.wgt;
				winResultsVM.raceid = win.raceid;
				
				winrs.add(winResultsVM);
			}
    	return ok(Json.toJson(winrs));
    }
    
    
    public static Result BetResultByUser() {
    	
    	JsonNode json = request().body().asJson();
	        String email = json.path("email").asText();
	        User user = User.findByUserEmail(email);
	        List<saveBetVM> winrs = new ArrayList<>();
	        UserBet userBet = UserBet.getUserBetsByUser(user);
	        //for(UserBet ub:userBet){
	        	saveBetVM saveBetVM = new saveBetVM();
	        	saveBetVM.name = userBet.betName;
	        	Races races = Races.getRaceListByraceId(userBet.raceId);
	    		//for(Races rs:races){
					RaceVM rc = new RaceVM();
					rc.raceId = races.raceid;
					rc.name = races.name;
					if(userBet.raceId != null){
						List<UserBetDetails> ued = UserBetDetails.getByUserAndBetId(userBet);
						for(UserBetDetails rs:ued){
						WinResults win = WinResults.getresulttByRaceIdHorseId(userBet.raceId,rs.horseId);
						if(win != null){
						WinResultsVM winResultsVM = new WinResultsVM();
						winResultsVM.id = win.id;
						winResultsVM.name = win.name;
						winResultsVM.jockey = win.jockey;
						winResultsVM.position = win.position;
						winResultsVM.number = win.number;
						winResultsVM.wgt = win.wgt;
						winResultsVM.raceid = win.raceid;
						rc.winResultsVMs.add(winResultsVM);
						}	
	    		}
			}			
				saveBetVM.allRaces.add(rc);
					//winrs.add(rc);			
	        //}
	    		winrs.add(saveBetVM);
	     // }  
	        
    	return ok(Json.toJson(winrs));
    }
    
}
