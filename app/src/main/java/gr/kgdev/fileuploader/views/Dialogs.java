package gr.kgdev.fileuploader.views;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.widget.EditText;

public class Dialogs {

    private static ProgressDialog LOADING_DIALOG = null;

    public static void showErrorDialog(Context context, String message) {
        dismissLoadingDialog();
        new AlertDialog.Builder(context)
                .setTitle("Error")
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss()).show();
    }

    public static void showInfoDialog(Context context, String message) {
        dismissLoadingDialog();
        new AlertDialog.Builder(context)
                .setTitle("Info")
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss()).show();
    }

    private static void dismissLoadingDialog() {
        if (LOADING_DIALOG != null && LOADING_DIALOG.isShowing()) {
            LOADING_DIALOG.dismiss();
            LOADING_DIALOG = null;
        }
    }

    public static void showLoadingDialog(Context context, String message) {
        LOADING_DIALOG = new ProgressDialog(context);
        LOADING_DIALOG.setMessage("Please Wait..");
        LOADING_DIALOG.setTitle(message);
        LOADING_DIALOG.setIndeterminate(false);
        LOADING_DIALOG.setCancelable(false);
        LOADING_DIALOG.show();
    }

    public static void showInputDialog(Context context, String message, RunnbaleWithInput<String> onEnter) {
        showInputDialog(context, null, message, onEnter);
    }

    public static void showInputDialog(Context context, String defaultInput, String message, RunnbaleWithInput<String> onEnter) {
        final EditText editText = new EditText(context);
        editText.setHint("Enter Here...");
        editText.setText(defaultInput);

        new AlertDialog.Builder(context)
                .setTitle("Input Dialog")
                .setMessage(message)
                .setView(editText)
                .setPositiveButton("OK", (dialog, whichButton) -> onEnter.run(editText.getText().toString()))
                .setNegativeButton("Cancel", (dialog, whichButton) -> {
                })
                .show();
    }
}
