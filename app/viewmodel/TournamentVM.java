package viewmodel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.Races;


public class TournamentVM {

	public Long id;
	public String name;
	public Date date;
	public String tournamentId;
	public List<Races> races;
    public java.util.Date version;
    public List<RaceVM> allRaces = new ArrayList<RaceVM>();
}
