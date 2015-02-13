package models;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Version;

import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
public class Tournament extends Model {

	@Id
	public Long id;
	public String name;
	public Date date;
	public String tournamentId;
	@OneToMany(cascade=CascadeType.ALL)
	public List<Races> races;
	@Version
    public java.util.Date version;
	
	public static Finder<Long,Tournament> find = new Finder<>(Long.class,Tournament.class);
	
	public static Tournament getTournament(String name,Date date,String id) {
		return find.where().eq("name", name).eq("date", date).eq("tournamentId", id).findUnique();
	}
	
	public static List<Tournament> getTournamentByDate(Date date) {
		return find.where().eq("date", date).findList();
	}
	
	public static List<Tournament> getTournamentById(String tournamentId) {
		return find.where().eq("tournamentId", tournamentId).findList();
	}
}
