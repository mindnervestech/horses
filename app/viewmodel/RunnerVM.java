package viewmodel;

import java.util.ArrayList;
import java.util.List;

import models.Races;
import viewmodel.Scores.Tournament.Race.Odds.Horse.Bookmakers;


public class RunnerVM {

	public Long id;
	public String number;
	public String name;
	public String jockey; 
	public String horseId;
	public List<BookmakersVM> bookmakersVM = new ArrayList<BookmakersVM>();
	public String wgt;
	public String trainer;
	public String age;
	
}
