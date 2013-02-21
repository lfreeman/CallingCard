package org.lfreeman.callingcard;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.CallLog;

public class OutgoingCallInterceptor extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final String oldNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        String newNumber;

        if (oldNumber.startsWith("+")) {
            SharedPreferences prefs = context.getSharedPreferences("CalingCard",
                    Context.MODE_PRIVATE);
            String accessNumber = prefs.getString("ACCESS_NUMBER", "");
            String intPrefix = prefs.getString("INT_PREFIX", "");
            
            if(accessNumber.isEmpty() && intPrefix.isEmpty()){
                return;
            }
            
            //use ,, for pause
            newNumber = String.format("%s,,%s%s", accessNumber, intPrefix, oldNumber.substring(1));
            this.setResultData(newNumber);
            this.insertCallLog(context.getContentResolver(), oldNumber);
        }
    }

    public void insertCallLog(ContentResolver contentResolver, String number) {
        ContentValues values = new ContentValues();
        values.put(CallLog.Calls.NUMBER, number);
        values.put(CallLog.Calls.DATE, System.currentTimeMillis());
        values.put(CallLog.Calls.DURATION, 0);
        values.put(CallLog.Calls.TYPE, CallLog.Calls.OUTGOING_TYPE);
        values.put(CallLog.Calls.NEW, 1);
        values.put(CallLog.Calls.CACHED_NAME, "");
        values.put(CallLog.Calls.CACHED_NUMBER_TYPE, 0);
        values.put(CallLog.Calls.CACHED_NUMBER_LABEL, "");
        contentResolver.insert(CallLog.Calls.CONTENT_URI, values);
    }

}
