package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Version;

import play.db.ebean.Model;

import com.avaje.ebean.Expr;

import controllers.Application.ResultsVM;

@Entity
public class Results extends Model{

	@Id
	public Long id;
	public Date eventDate;
	public Date eventTime;
	public String meeting;
	public String venue;
	@Lob
	public String results;
	
	@Version
    public java.util.Date version;
	
	public static Finder<Long,Results> find = new Finder<>(Long.class,Results.class);

	public static Results findByDateTimeVenue(Date eventDate, Date eventTime, String meeting) {
		return find.where()
				.eq("meeting", meeting)
				.eq("eventDate", eventDate)
				.eq("eventTime", eventTime).findUnique();
		
	}

	public static List<Results> findByEventDate(Date d) {
		return find.where().eq("eventDate", d).findList();
	}

	
}
