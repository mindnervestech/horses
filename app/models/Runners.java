package models;

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
public class Runners extends Model {

	@Id
	public Long id;
	public String number;
	public String name;
	public String jockey; 
	public String horseId;
	public String raceid;
	
	@OneToMany(cascade=CascadeType.ALL)
	public List<Bookmakers> bookmakers;
	
	@ManyToOne
	public Races races;
	
	
	public static Finder<Long,Runners> find = new Finder<>(Long.class,Runners.class);
	
	public static Runners getByHorseId(String id) {
		return find.where().eq("horseId", id).findUnique();
	}
	
	public static List<Runners> getByRunnerById(String raceid) {
		return find.where().eq("raceid", raceid).findList();
	}
	
}
