package odbg.org.libs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * 
 * @author orlando
 *	
 *	@version 1.2
 */

public class PropFile {
	
	public static Properties getfile( String url ) {
		FileInputStream propFile = null;
		Properties prop = null;
		try {
			StringBuilder str = new StringBuilder();
			str.append(url).append(".properties");
			
			propFile = new FileInputStream(str.toString());
			prop = new Properties();
			prop.load(propFile);
			propFile.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return prop;
	}
	
}
