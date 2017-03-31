package kr.neolab.sdk.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import kr.neolab.sdk.pen.IPenAdt;
import kr.neolab.sdk.pen.PenCtrl;
import kr.neolab.sdk.pen.penmsg.PenMsg;
import kr.neolab.sdk.pen.penmsg.PenMsgType;
import kr.neolab.sdk.util.NLog;

/**
 * The type Bt duplicate remove broadcaster receiver.
 */
public class BTDuplicateRemoveBroadcasterReceiver extends BroadcastReceiver{

	/**
	 * The constant ACTION_BT_REQ_CONNECT.
	 */
	public static final String ACTION_BT_REQ_CONNECT = "kr.neolab.sdk.connection.reqconnect";
	/**
	 * The constant EXTRA_BT_CONNECT_PACKAGENAME.
	 */
	public static final String EXTRA_BT_CONNECT_PACKAGENAME = "connect_packagename";
	/**
	 * The constant ACTION_BT_RESPONSE_CONNECTED.
	 */
	public static final String ACTION_BT_RESPONSE_CONNECTED = "kr.neolab.sdk.connection.connected";
	
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		
		if(arg1.getAction() != null && arg1.getAction().equals(ACTION_BT_REQ_CONNECT))
		{
			String connectPackageName = arg1.getStringExtra(EXTRA_BT_CONNECT_PACKAGENAME);
			int penStatus = PenCtrl.getInstance().getPenStatus();
			if(connectPackageName != null && connectPackageName.length() > 0 && !connectPackageName.equals(arg0.getPackageName()) && (penStatus == IPenAdt.CONN_STATUS_AUTHORIZED || penStatus == IPenAdt.CONN_STATUS_ESTABLISHED))
			{
//				
		    	Intent i = new Intent(BTDuplicateRemoveBroadcasterReceiver.ACTION_BT_RESPONSE_CONNECTED);
		    	i.setPackage(connectPackageName);
		    	i.putExtra(BTDuplicateRemoveBroadcasterReceiver.EXTRA_BT_CONNECT_PACKAGENAME, arg0.getPackageName());
		    	arg0.sendBroadcast(i);
			}
			NLog.d("BTDuplicateRemoveBroadcasterReceiver connecting_packagename="+arg1.getStringExtra(EXTRA_BT_CONNECT_PACKAGENAME)+ ";connected_packagename"+arg0.getPackageName()+",penStatus = "+penStatus);
			
		}
		else if(arg1.getAction() != null && arg1.getAction().equals(ACTION_BT_RESPONSE_CONNECTED))
		{
			String connectedPackageName = arg1.getStringExtra(EXTRA_BT_CONNECT_PACKAGENAME);
			
			if(PenCtrl.getInstance().getListener() != null && !connectedPackageName.equals(arg0.getPackageName()))
			{
				String connected_packagename = arg1.getStringExtra(EXTRA_BT_CONNECT_PACKAGENAME);
				PenMsg msg = new PenMsg( PenMsgType.PEN_CONNECTION_FAILURE_BTDUPLICATE, "packageName", connected_packagename );
				PenCtrl.getInstance().getListener().onReceiveMessage(msg);
			}

		}
		
	}

}
