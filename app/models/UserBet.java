package models;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
public class UserBet extends Model {

	@Id
	public Long id;
	@ManyToOne
	public User user;
	public String raceId;
	public String betName;
	
	public static Finder<Long,UserBet> find = new Finder<>(Long.class,UserBet.class);
	
	public static UserBet getByUserAndBetId(User user,String raceId) {
		return find.where().eq("user", user).eq("raceId", raceId).findUnique();
	}
	
	public static List<UserBet> getUserBetsByUser(User user) {
		return find.where().eq("user", user).findList();
	}
	
	public static List<UserBet> getUserBetByRaceId(String raceId) {
		return find.where().eq("raceId", raceId).findList();
	}
}
