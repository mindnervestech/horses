package models;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Version;

import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
public class Races extends Model {

	@Id
	public Long id;
	public String name;
	public Date dateTime;
	public String tid;
	public String raceid;
	@ManyToOne
	public Tournament tournament;
	@OneToMany(cascade=CascadeType.ALL)
	public List<Runners> runners;
	@OneToMany(cascade=CascadeType.ALL)
	public List<WinResults> winResults;
	@Version
    public java.util.Date version;
	
	public static Finder<Long,Races> find = new Finder<>(Long.class,Races.class);
	
	public static List<Races> getRaceByTId(Tournament tournament) {
		return find.where().eq("tournament", tournament).findList();
	}
	
	public static List<Races> getRaceByTourId(String tid) {
		return find.where().eq("tid", tid).findList();
	}
	
	public static Races getRaceByraceId(String raceid) {
		return find.where().eq("raceid", raceid).findUnique();
	}
	
	public static Races getRaceById(Long id) {
		return find.where().eq("id", id).findUnique();
	}
	
	public static List<Races> getRaceByDate(Date dateTime) {
		return find.where().eq("dateTime", dateTime).findList();
	}
	
}
