package models;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Version;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import java.sql.Timestamp;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
public class Event extends Model {

	@Id
	public Long id;
	public String name;
	public String eventId;
	public Date eventDate;
	public Date eventTime;
	public String meeting;
	public String venue;
	public Date betStartDate;
	public Date betStartTime;
	public String betName;
	public String betTypeId;
	@Version
       public java.util.Date version;
	
	@OneToMany(cascade=CascadeType.ALL)
	public List<Bet> bet;
	
	public static Finder<Long,Event> find = new Finder<>(Long.class,Event.class);

	public static List<SqlRow> getVenues(Date date) {
		 SqlQuery sqlQuery = Ebean.createSqlQuery("select distinct venue from event where event_date = :date");
		 sqlQuery.setParameter("date", date);
		 List<SqlRow> list = sqlQuery.findList();
		 return list;
	}
	
	public static Event findByEventId(String eventId) {
		return find.where().eq("eventId", eventId).findUnique();
	}
	
	public static List<Event> getEventByDateAndVenue(String venue, Date date) {
		return find.where().and(Expr.eq("venue", venue), Expr.eq("eventDate", date)).findList();
	}
	
}
