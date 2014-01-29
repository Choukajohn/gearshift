package org.sugr.gearshift;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import org.sugr.gearshift.service.DataServiceManager;
import org.sugr.gearshift.service.DataServiceManagerInterface;

public class LocationDialogHelper {
    private FragmentActivity activity;

    public LocationDialogHelper(FragmentActivity activity) {
        if (!(activity instanceof TransmissionSessionInterface)
            || !(activity instanceof DataServiceManagerInterface)) {
            throw new IllegalArgumentException("Invalid activity instance");
        }

        this.activity = activity;
    }

    public AlertDialog showDialog(int layout, int title,
                                  DialogInterface.OnClickListener cancelListener,
                                  DialogInterface.OnClickListener okListener) {
        LayoutInflater inflater = activity.getLayoutInflater();

        final TransmissionSession session = ((TransmissionSessionInterface) activity).getSession();
        final DataServiceManager manager = ((DataServiceManagerInterface) activity).getDataServiceManager();
        if (session == null || manager == null) {
            return null;
        }

        final View view = inflater.inflate(layout, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity)
            .setCancelable(false)
            .setView(view)
            .setTitle(title)
            .setNegativeButton(android.R.string.cancel, cancelListener)
            .setPositiveButton(android.R.string.ok, okListener);

        final TransmissionProfileDirectoryAdapter adapter =
            new TransmissionProfileDirectoryAdapter(
                activity, android.R.layout.simple_spinner_item);


        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.addAll(session.getDownloadDirectories());
        adapter.sort();
        adapter.add(activity.getString(R.string.spinner_custom_directory));

        final Spinner location = (Spinner) view.findViewById(R.id.location_choice);
        final LinearLayout container = (LinearLayout) view.findViewById(R.id.location_container);
        final int duration = activity.getResources().getInteger(android.R.integer.config_shortAnimTime);
        final Runnable swapLocationSpinner = new Runnable() {
            @Override public void run() {
                container.setAlpha(0f);
                container.setVisibility(View.VISIBLE);
                container.animate().alpha(1f).setDuration(duration);

                location.animate().alpha(0f).setDuration(duration).setListener(new AnimatorListenerAdapter() {
                    @Override public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        location.setVisibility(View.GONE);
                        location.animate().setListener(null).cancel();
                        if (location.getSelectedItemPosition() != adapter.getCount() - 1) {
                            ((EditText) view.findViewById(R.id.location_entry)).setText((String) location.getSelectedItem());
                        }
                        container.requestFocus();
                    }
                });
            }
        };
        location.setAdapter(adapter);
        location.setOnLongClickListener(new View.OnLongClickListener() {
            @Override public boolean onLongClick(final View v) {
                swapLocationSpinner.run();
                return true;
            }
        });
        location.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (adapter.getCount() == i + 1) {
                    swapLocationSpinner.run();
                }
            }
            @Override public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        TransmissionProfile profile = ((TransmissionSessionInterface) activity).getProfile();
        if (profile != null && profile.getLastDownloadDirectory() != null) {
            int position = adapter.getPosition(profile.getLastDownloadDirectory());

            if (position > -1) {
                location.setSelection(position);
            }
        }

        View collapse = view.findViewById(R.id.location_collapse);
        collapse.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                location.setAlpha(0f);
                location.setVisibility(View.VISIBLE);
                location.animate().alpha(1f).setDuration(duration);

                container.animate().alpha(0f).setDuration(duration).setListener(new AnimatorListenerAdapter() {
                    @Override public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        container.setVisibility(View.GONE);
                        container.animate().setListener(null).cancel();
                    }
                });
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
        return dialog;
    }
}