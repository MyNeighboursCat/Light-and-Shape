// Generated by view binder compiler. Do not edit!
package com.mycompany.lightandshape.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.mycompany.lightandshape.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class HighScoreDialogBinding implements ViewBinding {
  @NonNull
  private final RelativeLayout rootView;

  @NonNull
  public final LinearLayout buttonsLinearLayout;

  @NonNull
  public final Button cancelButton;

  @NonNull
  public final RelativeLayout highScoreDialogRoot;

  @NonNull
  public final EditText hsidEditText;

  @NonNull
  public final Button okButton;

  private HighScoreDialogBinding(@NonNull RelativeLayout rootView,
      @NonNull LinearLayout buttonsLinearLayout, @NonNull Button cancelButton,
      @NonNull RelativeLayout highScoreDialogRoot, @NonNull EditText hsidEditText,
      @NonNull Button okButton) {
    this.rootView = rootView;
    this.buttonsLinearLayout = buttonsLinearLayout;
    this.cancelButton = cancelButton;
    this.highScoreDialogRoot = highScoreDialogRoot;
    this.hsidEditText = hsidEditText;
    this.okButton = okButton;
  }

  @Override
  @NonNull
  public RelativeLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static HighScoreDialogBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static HighScoreDialogBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.high_score_dialog, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static HighScoreDialogBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.buttonsLinearLayout;
      LinearLayout buttonsLinearLayout = ViewBindings.findChildViewById(rootView, id);
      if (buttonsLinearLayout == null) {
        break missingId;
      }

      id = R.id.cancelButton;
      Button cancelButton = ViewBindings.findChildViewById(rootView, id);
      if (cancelButton == null) {
        break missingId;
      }

      RelativeLayout highScoreDialogRoot = (RelativeLayout) rootView;

      id = R.id.hsidEditText;
      EditText hsidEditText = ViewBindings.findChildViewById(rootView, id);
      if (hsidEditText == null) {
        break missingId;
      }

      id = R.id.okButton;
      Button okButton = ViewBindings.findChildViewById(rootView, id);
      if (okButton == null) {
        break missingId;
      }

      return new HighScoreDialogBinding((RelativeLayout) rootView, buttonsLinearLayout,
          cancelButton, highScoreDialogRoot, hsidEditText, okButton);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}