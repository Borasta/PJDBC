package odbg.org.main;

import odbg.org.libs.JDBC;

public class Hilo extends Thread {
	
	JDBC jdbc = null;
	boolean doSleep = false;
	
	public Hilo() {
		this.jdbc = Main.pgPool.getJDBC();
	}
	
	public Hilo( Boolean doSleep) {
		this.doSleep = doSleep;
		this.jdbc = Main.pgPool.getJDBC();
	}
	
	public void run() {
		int time = (int)(Math.random()*5 + 1) * 1000;
		
		System.out.println(this.getName() + " dormira " + (time/1000) + " segundos");
		
		try {
			if(this.doSleep)
				sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		this.jdbc.executeQuery("SELECT 1");
		
		Main.pgPool.returnJDBC(jdbc);
		
		System.out.println(this.getName() + " acaba de finalizar");
	}

}
