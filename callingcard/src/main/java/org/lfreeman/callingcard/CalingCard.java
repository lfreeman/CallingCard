package org.lfreeman.callingcard;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class CalingCard extends Activity {

    private EditText          editTextAccessNumber;
    private SharedPreferences prefs;
    private static final String LOG_TAG = "CalingCard";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        this.prefs = getPreferences(MODE_PRIVATE);
        editTextAccessNumber = (EditText) findViewById(R.id.editTextAccessNumber);
        editTextAccessNumber.setText(prefs.getString("ACCESS_NUMBER", ""));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.caling_card, menu);
        return true;
    }

    public void handleSaveClick(View v) {
        Editor editor = prefs.edit();
        editor.putString("ACCESS_NUMBER", editTextAccessNumber.getText().toString());
        editor.commit();
        finish();
    }

    public void handleClearClick(View v) {
        editTextAccessNumber.setText("");
    }

}
