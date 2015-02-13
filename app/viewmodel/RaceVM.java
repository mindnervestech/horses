package viewmodel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Version;

import models.Races;
import models.Runners;
import models.Tournament;


public class RaceVM {

	public Long id;
	public String name;
	public Date dateTime;
	public String tournamentId;
	public String tournamentName;
	public List<WinResultsVM> winResultsVMs = new ArrayList<WinResultsVM>();
}
