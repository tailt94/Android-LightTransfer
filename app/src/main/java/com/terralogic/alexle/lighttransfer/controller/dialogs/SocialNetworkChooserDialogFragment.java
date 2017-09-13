package com.terralogic.alexle.lighttransfer.controller.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.terralogic.alexle.lighttransfer.R;

/**
 * Created by alex.le on 13-Sep-17.
 */

public class SocialNetworkChooserDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.social_network_chooser_dialog_title)
                .setItems(R.array.social_network_chooser_items, (DialogInterface.OnClickListener)getActivity());
        return builder.create();
    }
}
