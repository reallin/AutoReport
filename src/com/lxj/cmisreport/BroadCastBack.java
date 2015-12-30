package com.lxj.cmisreport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BroadCastBack extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		Intent i = new Intent(arg0,SetTimeService.class);
		String name = arg1.getStringExtra("name");
		String pass = arg1.getStringExtra("pass");
		i.putExtra("name", name);
		i.putExtra("pass", pass);
		arg0.startService(i);
	}

}
