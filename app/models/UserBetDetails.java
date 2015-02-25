package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
public class UserBetDetails extends Model {

	@Id
	public Long id;
	@ManyToOne
	public UserBet userBet;
	public String horseId;
	
	public static Finder<Long,UserBetDetails> find = new Finder<>(Long.class,UserBetDetails.class);
	
	public static List<UserBetDetails> getByUserAndBetId( UserBet userBet) {
		return find.where().eq("userBet", userBet).findList();
	}
	
	/*public static List<UserBetDetails> getUserBetsByUser(User user) {
		return find.where().eq("user", user).findList();
	}
	
	public static List<UserBetDetails> getUserBetByRaceId(String raceId) {
		return find.where().eq("raceId", raceId).findList();
	}*/
}
