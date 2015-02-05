package controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import models.Bet;
import models.Event;
import models.Results;
import models.User;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import viewmodel.BetVM;
import viewmodel.EventVM;
import views.html.index;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;

import controllers.Application.ResultsVM.Rank;

public class Application extends Controller {
  
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
    			SimpleDateFormat dt = new SimpleDateFormat("yyyymmdd");
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
        			SimpleDateFormat dt = new SimpleDateFormat("yyyymmdd");
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
    	//System.out.println(nList.getLength());
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
    			
    			return ok(Json.toJson(new ErrorResponse(Error.E200.getCode(),Error.E200.getMessage())));
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
    
    public static Result getResults() throws ParseException{
    	org.jsoup.nodes.Document doc = null;
		try {
			doc = Jsoup.connect("http://betfred.chromaagency.com/results/").get();
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
    	return ok(Json.toJson(results));
    }
    
}
