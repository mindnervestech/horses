package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.sql.Timestamp;
import javax.persistence.Version;

import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
public class Bet extends Model {

	@Id
	public Long id;
	public String name;
	public String shortName;
	public String betId;
	public String price;
	public float priceDecimal;
	public float priceUs;
	public String activePriceTypes;
	@ManyToOne
	public Event event;
	@Version
        public java.util.Date version
	
	
	public static Finder<Long,Bet> find = new Finder<>(Long.class,Bet.class);
	
	public static Bet findByBetId(String betId) {
		return find.where().eq("betId", betId).findUnique();
	}
	
	public static List<Bet> getByEventId(Event event) {
		return find.where().eq("event", event).findList();
	}
}
