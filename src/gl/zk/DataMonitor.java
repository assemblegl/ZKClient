package gl.zk;

import gl.global.Context;
import gl.global.MyObject;

import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class DataMonitor extends MyObject implements Watcher { //
	private MyMonitor mymonitor;
    private Watcher chainedWatcher;
    private DataMonitorListener listener;
    private MyZK myzk;
    private Context context;
    boolean dead;
    byte prevData[];

    public DataMonitor(Context context,Watcher chainedWatcher,DataMonitorListener listener) {        
        super.init(this.getClass());
    	this.chainedWatcher = chainedWatcher;
        this.listener = listener;
        this.mymonitor = new MyMonitor();
        
        if(mymonitor.init(context) == false){
        	dead = true;
        	listener.closing(-1);
        }                   
    }

    @SuppressWarnings("incomplete-switch")
	public void process(WatchedEvent event) {
    	printInfo("process in,type:"+event.getType());
        String path = event.getPath();
        if (event.getType() == Event.EventType.None) {
            // We are are being told that the state of the
            // connection has changed
            switch (event.getState()) {
            case SyncConnected:
                // In this particular example we don't need to do anything
                // here - watches are automatically re-registered with 
                // server and any watches triggered while the client was 
                // disconnected will be delivered (in order of course)
                break;
            case Expired:
                // It's all over
                //dead = true;
                //listener.closing(KeeperException.Code.SessionExpired);
                break;
            }
        } else {
        	mymonitor.Process(path);      	
        }
                
        //Watcher continue
        if (chainedWatcher != null) {
            chainedWatcher.process(event);
        }
    }
    
}