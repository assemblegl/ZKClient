package gl.zk;

import gl.global.Context;
import gl.global.MyNode;
import gl.global.MyObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.data.Stat;

public class MyMonitor extends MyObject implements StatCallback{ //AsyncCallback.ChildrenCallback,
	private MyZK myzk;
	//private String dataNode;
	//private String signDataNode;
	//private String runNameNode;
	//private String signRunNode;
	private Context context;
	private byte[] prevSignData;
	private byte[] prevHostConf;
	private byte[] prevSignRun;
	
	public MyMonitor(){
		super.init(this.getClass());
		//init(context);
	}
	
	public boolean init(Context context){
		this.context = context;
		//this.dataNode = context.getDataNode();
		//this.signDataNode = context.getSignDataNode();
		//this.signRunNode = context.getSignRunNode();
		this.myzk = context.getMyzk();		
		
		prevSignData = myzk.getData(context.getSignDataNode());
		prevHostConf = myzk.getData(context.getHostConfNode());
		prevSignRun = myzk.getData(context.getSignRunNode());
		
		Map<String,MyNode> mapNode = context.getMapPathNode();
		for(String nodename:mapNode.keySet()){
			MyNode mynode = mapNode.get(nodename);
			if(mynode.isClientNeedListenNode()){
				myzk.exists(nodename,true,this,null);
			}
		}
        //check node
        //myzk.exists(signDataNode, true,this,null);
        //myzk.exists(signRunNode, true,this,null);
        return true;
	}	
	
	public void Process(String path){
		// Something has changed on the node, let's find out        
   	 	/*if (path != null && path.equals(signDataNode)){                      
   	 		myzk.exists(signDataNode,true, this,null);
        }
   	 	
   	 	if(path != null && path.equals(signRunNode)){
   	 		myzk.exists(signRunNode,true, this,null);
   	 	}*/
		if(null == path) return;
		
		MyNode mynode = context.getMapPathNode().get(path);
		
		if(null == mynode) {
			printError("warning!get from mapNode is null:node:"+path);
			return;
		}
		
		if(mynode.isClientNeedListenNode()){
			myzk.exists(path,true,this,null);
		}
	}
	
	//children
	/*@SuppressWarnings("deprecation")
	public void processResult(int rc, String path, Object ctx, List<String> nodeList){
    	printInfo("go in child processResult in,rc:"+rc+",path:"+path);
    	boolean exists;
        switch (rc) {
        case Code.Ok:
            exists = true;
            break;
        case Code.NoNode:
            exists = false;
            break;
        case Code.SessionExpired:
        case Code.NoAuth:
            //dead = true;
            //listener.closing(rc);
            return;
        default:
            // Retry errors
           // zk.exists(znode, true, this, null);
            return;
        }
       
        if (exists) {        	
        	if(path.equals(errorNode)){
        		
        	}else if(path.equals(zNode)){
        		
        	}       	            
        }
    	    	
    }*/
	
	//exists
	@SuppressWarnings("deprecation")
	public void processResult(int rc, String path, Object ctx, Stat stat) {
    	System.out.println("processResult in");
        boolean exists;
        switch (rc) {
        case Code.Ok:
            exists = true;
            break;
        case Code.NoNode:
            exists = false;
            break;
        case Code.SessionExpired:
        case Code.NoAuth:
            //dead = true;
            //listener.closing(rc);
            return;
        default:
            // Retry errors
        	/*if(path.equals(signDataNode)){
        		myzk.exists(signDataNode, true, this,null);
        	}else if(path.equals(signRunNode)){
        		myzk.exists(signRunNode, true, this,null);
        	}*/
        	
        	if(null == path) return;   		
    		MyNode mynode = context.getMapPathNode().get(path);    		
    		if(null == mynode) {
    			printError("warning in processResult!get from mapNode is null:node:"+path);
    			return;
    		}
    		
    		if(mynode.isClientNeedListenNode()){
    			myzk.exists(path,true,this,null);
    		}
            return;
        }
       
        if (exists) { 
        	if(path.equals(context.getSignDataNode())){
        		processDataNode();
        	}else if(path.equals(context.getSignRunNode())){
        		processRunNode();
        	}else if(path.equals(context.getHostConfNode())){
        		processConfNode();
        	}
        }        
    }	
	
	private void processConfNode(){
		//get conf content,everytime must be dif
    	byte bcnode[] = myzk.getData(context.getHostConfNode());
    	if(null == bcnode){
    		printError("error in myzk.getData,return is null,hostConfNode:"+context.getHostConfNode());
    		return;
    	}
    	
    	if(!Arrays.equals(prevHostConf,bcnode)) {
    		String newhostPort = new String(bcnode);       	
        	
    		myzk.resetHostConf(newhostPort);
    		
        	prevHostConf = bcnode;
        } 
	}
	
	private void processDataNode(){
		//get file name,everytime must be dif
    	byte bsnode[] = myzk.getData(context.getSignDataNode());
    	if(null == bsnode){
    		printError("error in myzk.getData,return is null,signDataNode:"+context.getSignDataNode());
    		return;
    	}
    	
    	//get file
    	byte bdnode[] = myzk.getData(context.getDataNode());
    	if(null == bdnode){
    		printError("error in myzk.getData,return is null,dataNode:"+context.getDataNode());
    		return;
    	}
    	
    	if(!Arrays.equals(prevSignData,bsnode)) {
    		String datafilename = new String(bsnode);       	
        	try {
                FileOutputStream fos = new FileOutputStream(datafilename);
                fos.write(bdnode);
                fos.close();
                printInfo("save file ok filename:"+datafilename);
            } catch (IOException e) {
                printError(e.getMessage());
            }
        	prevSignData = bsnode;
        } 
	}
	
	
	private void processRunNode(){
		byte brunsign[] = myzk.getData(context.getSignRunNode());
		if(null == brunsign){
    		printError("error in myzk.getData,return is null,SignRunNode:"+context.getSignRunNode());
    		return;
    	}
		
		byte brunfilename[] = myzk.getData(context.getRunNameNode());
		if(null == brunfilename){
    		printError("error in myzk.getData,return is null,RunNameNode:"+context.getRunNameNode());
    		return;
    	}
		
		if(!Arrays.equals(prevSignRun,brunsign)) {
			String runfilename = new String(brunfilename);
			File file = new File(runfilename);
			if(!file.exists()){
				printError("error run file not exists,file:"+runfilename);
				return;
			}
			
			try {
	            printInfo("Starting running file:"+runfilename);
	            Process child = Runtime.getRuntime().exec(runfilename);
	            ByteArrayOutputStream output = new ByteArrayOutputStream();
	            ByteArrayOutputStream errorput = new ByteArrayOutputStream();	            	            	           
	            
	            ByteArrayOutputStream alloutput = new ByteArrayOutputStream();
	            
	            StreamWriter swout = new StreamWriter(child.getInputStream(),output);
	            StreamWriter swerror = new StreamWriter(child.getErrorStream(), errorput);
	            
	            try {
					swout.join();
					swerror.join();
				} catch (InterruptedException e) {
					printError(e.getMessage());
					return;
				}
	            
	            alloutput.write(output.toByteArray());
	            alloutput.write(errorput.toByteArray());
	            alloutput.flush();
	            	            
                myzk.checkNodeAndCreate(context.getRunLogNode()+"/"+new String(brunsign) ,0);              
                
	            myzk.create(context.getRunLogNode()+"/"+new String(brunsign)+"/"+context.getLocalhost(),alloutput.toByteArray(), CreateMode.PERSISTENT);
	            alloutput.close();
	        } catch (IOException e) {
	            printError(e.getMessage());
	        }
			
			prevSignRun = brunsign;
		}		
						
	}
	
	static class StreamWriter extends Thread {
        OutputStream os;
        InputStream is;

        StreamWriter(InputStream is, OutputStream os) {
            this.is = is;
            this.os = os;
            start();
        }

        public void run() {
            byte b[] = new byte[500];
            int rc;
            try {
                while ((rc = is.read(b)) > 0) {
                    os.write(b, 0, rc);
                    
                }
            } catch (IOException e) {
            }

        }
    }	
	
	/*public void exists(byte[] data) {
        if (data == null) {
            if (child != null) {
                System.out.println("Killing process");
                child.destroy();
                try {
                    child.waitFor();
                } catch (InterruptedException e) {
                }
            }
            child = null;
        } else {
            if (child != null) {
                System.out.println("Stopping child");
                child.destroy();
                try {
                    child.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
            try {
                System.out.println("Starting child");
                child = Runtime.getRuntime().exec(exec);
                new StreamWriter(child.getInputStream(), System.out);
                new StreamWriter(child.getErrorStream(), System.err);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
    }*/

}

