package gl.main;

import gl.zk.Executor;

import org.apache.log4j.Logger;

public class Main {
	private static Logger logger = Logger.getLogger(Main.class);
	
	public static void main(String[] args) {
		
		//ApplicationContext ctx = new ClassPathXmlApplicationContext("gl/conf/applicationContext.xml");							
		/*Executor executor = (Executor)ctx.getBean("executor");	
		executor.run();					
		System.exit(0);*/

		 /*if (args.length != 2) {
        System.err.println("USAGE: Executor hostPort znode filename program [args ...]");
        System.exit(2);
    	}*/
		//String hostPort = args[0];
		//String znode = args[1];
		
		String hostPort = "59.151.111.57:2181";   	
    	String confNode = "/conf";
        try {
            new Executor(hostPort,confNode);
        } catch (Exception e) {
            e.printStackTrace();
        }
		
	}

}
