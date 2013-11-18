package org.lfreeman.callingcard;

import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.CallLog;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber.CountryCodeSource;

public class OutgoingCallInterceptor extends BroadcastReceiver {
    private static final String LOG_TAG = "OutgoingCallInterceptor";
    private PhoneNumberUtil     phoneUtil;

    public OutgoingCallInterceptor() {
        phoneUtil = PhoneNumberUtil.getInstance();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String oldNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        Log.d(LOG_TAG, oldNumber);

        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String regionCode = tm.getSimCountryIso().toUpperCase(Locale.US);

        PhoneNumber number = null;
        try {
            number = phoneUtil.parse(oldNumber, regionCode);
        } catch (NumberParseException e) {
            return;
        }

        if (isInternationalCall(number, regionCode)) {
            String tempNumber = phoneUtil.formatOutOfCountryCallingNumber(number, regionCode).replaceAll("\\D", "");
            Log.d(LOG_TAG, tempNumber);

            SharedPreferences prefs = context.getSharedPreferences("CalingCard", Context.MODE_PRIVATE);
            String accessNumber = prefs.getString("ACCESS_NUMBER", "");

            if (accessNumber.isEmpty()) {
                return;
            }

            // use ,, for pause
            String newNumber = String.format("%s,,%s", accessNumber, tempNumber);
            Log.d(LOG_TAG, newNumber);
            this.setResultData(newNumber);
            this.insertCallLog(context.getContentResolver(), oldNumber);
        }
    }

    private boolean isInternationalCall(PhoneNumber number, String regionCode) {
        if (number.getCountryCodeSource() == CountryCodeSource.FROM_NUMBER_WITH_PLUS_SIGN) {
            if (number.getCountryCode() != phoneUtil.getCountryCodeForRegion(regionCode)) {
                return true;
            }
        }
        return false;
    }

    private void insertCallLog(ContentResolver contentResolver, String number) {
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
