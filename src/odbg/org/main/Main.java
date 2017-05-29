package odbg.org.main;

import java.util.Properties;

import odbg.org.libs.PJDBC;
import odbg.org.libs.PropFile;

public class Main {
	
	public static Properties dataSource = PropFile.getfile("database");
	public static PJDBC pgPool = new PJDBC(dataSource);

	public static void main(String[] args) {
		
		Hilo arr[] = new Hilo[100];
		
		for (int i = 0; i < arr.length; i++) {
			arr[i] = new Hilo(true);
			arr[i].start();
		}
		
	}

}
