package info.snowhow.plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import android.util.Log;

import org.json.JSONObject;

import info.snowhow.plugin.RecorderService;

/**
 * A simple WebSocketServer implementation. Send GPS Data
 */
public class GPSServer extends WebSocketServer {
  private static final String LOG_TAG = "GPSServer";
  protected RecorderService rs;

	public GPSServer( int port, RecorderService rs ) throws UnknownHostException {
		super( new InetSocketAddress( port ) );
    this.rs = rs;
	}

	@Override
	public void onOpen( WebSocket conn, ClientHandshake handshake ) {
		this.sendString("{ \"type\": \"status\", \"msg\": \"connected\" }");
		Log.d(LOG_TAG, conn.getRemoteSocketAddress().getAddress().getHostAddress() + " connected." );
	}

	@Override
	public void onStart() {
		Log.d(LOG_TAG, " GPSServer started." );
	}

	@Override
	public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
		this.sendString("{ \"type\": \"status\", \"msg\": \"disconnected\" }");
	}

	@Override
	public void onMessage( WebSocket conn, String message ) {
    Log.d(LOG_TAG, "got msg from UI: ->"+message+"<-");
    if (message.equals("quit")) {
      rs.stopRecording();
    } else if (message.equals("getTrack")) {
      String track = rs.getTrack().toString();
      Log.d(LOG_TAG, "got track from Recorder "+track+"<-");
      sendString("{ \"type\": \"track\", \"msg\": "+rs.getTrack().toString()+" }");
    } else if (message.equals("getFilename")) {
      sendString("{ \"type\": \"filename\", \"msg\": \""+rs.getTrackFilename()+"\" }");
    }
	}

	@Override
	public void onFragment( WebSocket conn, Framedata fragment ) {
		Log.d(LOG_TAG, "received fragment: " + fragment );
	}

	@Override
	public void onError( WebSocket conn, Exception ex ) {
    Log.d(LOG_TAG, "error in GPSServer");
		ex.printStackTrace();
		if( conn != null ) {
			// some errors like port binding failed may not be assignable to a specific websocket
		}
	}

	/**
	 * Sends <var>text</var> to all currently connected WebSocket clients.
	 * 
	 * @param text
	 *            The String to send across the network.
	 * @throws InterruptedException
	 *             When socket related I/O errors occur.
	 */
	public void sendString( String text ) {
    Log.d(LOG_TAG, "going to send message to connections: "+text);
		Collection<WebSocket> con = connections();
		synchronized ( con ) {
			for( WebSocket c : con ) {
				c.send( text );
        Log.d(LOG_TAG, "done sending string to "+c);
			}
		}
	}
}
