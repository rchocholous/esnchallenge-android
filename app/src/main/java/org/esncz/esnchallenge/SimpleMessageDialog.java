package org.esncz.esnchallenge;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * @author chochy
 * Date: 2019-01-21
 */
public class SimpleMessageDialog extends DialogFragment {

    private static String KEY_MESSAGE = "MESSAGE";
    private String message;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMessage("Message")
                .setPositiveButton("Ok", null);

        if(savedInstanceState != null) {
            message = savedInstanceState.getString(KEY_MESSAGE);
            builder.setMessage(message);
        }

        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();

        AlertDialog dialog=((AlertDialog)getDialog());
        dialog.setMessage(message);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle stateToSave) {
        super.onSaveInstanceState(stateToSave);
        stateToSave.putString(KEY_MESSAGE, message);
    }

    public void setResponseDetails(String message) {
        this.message = message.replace("\\n", "\n");
    }

}
