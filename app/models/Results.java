package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

import play.db.ebean.Model;

@Entity
public class Results extends Model{

	@Id
	public Long id;
	public Date eventDate;
	public Date eventTime;
	public String meeting;
	public String venue;
	public String results;
	
	@Version
    public java.util.Date version;
	
	public static Finder<Long,Results> find = new Finder<>(Long.class,Results.class);
	
}
