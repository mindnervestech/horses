package models;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
public class User extends Model {

	@Id
	public Long id;
	public String userName;
	public String password;
	public String email;
	
	public static Finder<Long,User> find = new Finder<>(Long.class,User.class);
	
	public static User findByUserEmail(String email) {
		return find.where().eq("email", email).findUnique();
	}
	
	public static User getUserByUserNameAndPassword(String username,String password) throws NoSuchAlgorithmException {
		return find.where().eq("userName", username).eq("password",User.md5Encryption(password)).findUnique();
	}
	
	public static String md5Encryption(String password) throws NoSuchAlgorithmException { 
		 MessageDigest md = MessageDigest.getInstance("MD5");
	        md.update(password.getBytes());
	 
	        byte byteData[] = md.digest();
	 
	        //convert the byte to hex format
	        StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < byteData.length; i++) {
	         sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
	        }
	 
	        
	        return sb.toString();
	}
}
