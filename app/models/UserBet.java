package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
public class UserBet extends Model {

	@Id
	public Long id;
	@ManyToOne
	public User user;
	public String betId;
	public Date time;
	public Date date;
	public String venue;
	
	
	public static Finder<Long,UserBet> find = new Finder<>(Long.class,UserBet.class);
	
	public static UserBet getByUserAndBetId(User user,String betId) {
		return find.where().eq("user", user).eq("betId", betId).findUnique();
	}
	
	public static List<UserBet> getUserBetsByEvent(Date eventDate,Date eventTime,String venue) {
		return find.where().eq("date", eventDate).eq("time", eventTime).eq("venue", venue).findList();
	}
}
