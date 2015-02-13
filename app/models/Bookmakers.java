package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
public class Bookmakers extends Model {

	@Id
	public Long id;
	public String name;
	public String odd;
	public String bookmakerId;
	public String oddId;
	
	@ManyToOne
	public Runners runners;
	@Version
    public java.util.Date version;
	
	public static Finder<Long,Bookmakers> find = new Finder<>(Long.class,Bookmakers.class);
	
	public static Bookmakers getBookmakers(String name,String odd,String oddId,String bookmakerId ,Runners runners) {
		return find.where().eq("name", name).eq("odd", odd).eq("oddId", oddId).eq("bookmakerId", bookmakerId).eq("runners", runners).findUnique();
	}
	
	public static List<Bookmakers> getBookmakersByRunnerId(Runners runners) {
		return find.where().eq("runners", runners).findList();
	}
	
}
