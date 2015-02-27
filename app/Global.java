

import java.util.concurrent.TimeUnit;

import akka.actor.ActorSystem;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.libs.Akka;
import scala.concurrent.duration.Duration;

public class Global extends GlobalSettings {
	public static final int CHAR_LEN=200;
	public static final String  APP_ENV_LOCAL = "local";
	public static final String  APP_ENV_VAR = "CURRENT_APPNAME";
	
	@Override
	public void onStart(Application app) {
		
		
		
		ActorSystem getLiveGame = Akka.system();
		getLiveGame.scheduler().schedule(
				Duration.create(1000, TimeUnit.MILLISECONDS),
				Duration.create(5, TimeUnit.MINUTES), new Runnable() {
					public void run() {
						try {
							controllers.Application.getTournamentDetails();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}, getLiveGame.dispatcher());
		
	}
	
	@Override
	public void onStop(Application app) {
		Logger.info("Application shutdown...");
	}
	
}
