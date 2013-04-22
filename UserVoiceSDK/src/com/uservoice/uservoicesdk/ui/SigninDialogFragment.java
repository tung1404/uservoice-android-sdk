package com.uservoice.uservoicesdk.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.uservoice.uservoicesdk.R;
import com.uservoice.uservoicesdk.Session;
import com.uservoice.uservoicesdk.model.AccessToken;
import com.uservoice.uservoicesdk.model.RequestToken;
import com.uservoice.uservoicesdk.model.User;
import com.uservoice.uservoicesdk.rest.Callback;
import com.uservoice.uservoicesdk.rest.RestResult;

public class SigninDialogFragment extends DialogFragment {
	
	private EditText email;
	private EditText name;
	private EditText password;
	private View passwordFields;
	private Button forgotPassword;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		RequestToken.getRequestToken(new DefaultCallback<RequestToken>(getActivity()) {
			@Override
			public void onModel(RequestToken requestToken) {
				Session.getInstance().setRequestToken(requestToken);
			}
		});
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Sign in");
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.signin_layout, null);
		email = (EditText) view.findViewById(R.id.signin_email);
		name = (EditText) view.findViewById(R.id.signin_name);
		password = (EditText) view.findViewById(R.id.signin_password);
		passwordFields = view.findViewById(R.id.signin_password_fields);
		forgotPassword = (Button) view.findViewById(R.id.signin_forgot_password);
		
		passwordFields.setVisibility(View.GONE);
		
		forgotPassword.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendForgotPassword();
			}
		});
		
		email.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (v == email && hasFocus == false) {
					discoverUser();
				}
			}
		});
		builder.setView(view);
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// nothing to do
			}
		});
		builder.setPositiveButton("Sign in", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				signIn();
			}
		});
		return builder.create();
	}
	
	private void discoverUser() {
		User.discover(email.getText().toString(), new Callback<User>() {
			@Override
			public void onModel(User model) {
				passwordFields.setVisibility(View.VISIBLE);
				name.setVisibility(View.GONE);
				password.requestFocus();
			}
			
			@Override
			public void onError(RestResult error) {
				passwordFields.setVisibility(View.GONE);
				name.setVisibility(View.VISIBLE);
				name.requestFocus();
			}
		});
	}
	
	private void signIn() {
		AccessToken.authorize(email.getText().toString(), password.getText().toString(), new Callback<AccessToken>() {
			@Override
			public void onModel(AccessToken accessToken) {
				Session.getInstance().setAccessToken(accessToken);
				dismiss();
				// run success callback
			}

			@Override
			public void onError(RestResult error) {
				Toast.makeText(getActivity(), "Incorrect email or password", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void sendForgotPassword() {
		User.sendForgotPassword(email.getText().toString(), new DefaultCallback<User>(getActivity()) {
			@Override
			public void onModel(User model) {
				Toast.makeText(getActivity(), "Forgot password email sent", Toast.LENGTH_SHORT).show();
			}
		});
	}
}
