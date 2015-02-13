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
	
	@Version
    public java.util.Date version;
	
	public static Finder<Long,WinResults> find = new Finder<>(Long.class,WinResults.class);

	public static List<WinResults> getresulttById(Long id) {
		return find.where().eq("id", id).findList();
	}
	
	public static List<WinResults> getresulttByRaceId(String raceid) {
		return find.where().eq("raceid", raceid).findList();
	}

}
