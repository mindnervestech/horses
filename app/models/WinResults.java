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
public class WinResults extends Model{

	@Id
	public Long id;
	public String position;
	public String number;
	public String name;
	public String wgt;
	public String jockey;
	public String raceid;
	public String horseid;
	public Boolean flag;
	
	@Version
    public java.util.Date version;
	
	public static Finder<Long,WinResults> find = new Finder<>(Long.class,WinResults.class);

	public static List<WinResults> getresulttById(Long id) {
		return find.where().eq("id", id).findList();
	}
	
	public static List<WinResults> getAllWinResult() {
		return find.where().eq("flag", false).findList();
	}
	
	public static List<WinResults> getresulttByRaceId(String raceid) {
		return find.where().eq("raceid", raceid).orderBy("-version").findList();
	}
	
	public static WinResults getLastResult() {
		return find.where().orderBy("-version").findList().get(0);
	}

	public static WinResults getresulttByRaceIdHorseId(String raceid,
			String horseid) {
		return find.where().eq("raceid", raceid).eq("horseid", horseid).findUnique();
	}
	

}
