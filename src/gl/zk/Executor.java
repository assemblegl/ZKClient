package gl.zk;

import gl.global.Context;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

public class Executor implements Watcher, Runnable,DataMonitorListener
{   
    private DataMonitor dm;       
    private Context context;
    
    /**
     * 
     * @param hostPort
     * @param zNode
     * @param zPersisNode
     * @param errorNode
     */
    public Executor(String hostPort,String confNode) { 
    	context = new Context(); //hostPort,this,confNode
    	if(context.clientInit(hostPort,this,confNode) == false) return;
        dm = new DataMonitor(context,null,this);
        run();
    }

    public void process(WatchedEvent event) {
        dm.process(event);
    }

    public void run() {
        try {
            synchronized (this) {
                while (!dm.dead) {
                    wait();
                }
            }
        } catch (InterruptedException e) {
        }
    }
    
    public void closing(int rc) {
        synchronized (this) {
            notifyAll();
        }
    }
    
    

}