package com.webviewapp;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.webviewapp.loaders.AdLoader;

import java.net.MalformedURLException;
import java.net.URL;

import example.com.webviewapp.R;

public class AdActivity extends Activity implements LoaderManager.LoaderCallbacks<String>{

    private static final int LOADER_ID = 1;
    private Bundle bundle;
    private Loader<String> loader;
    private LinearLayout linearLayout;
    private WebView webView;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        linearLayout = (LinearLayout) findViewById(R.id.layout);

        if (progressDialog == null)
            progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        bundle = new Bundle();
        bundle.putString("id", getIMSI());
        loader = getLoaderManager().initLoader(LOADER_ID, bundle, this);
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        Loader<String> loader = null;
        if(id == LOADER_ID){
            loader = new AdLoader(this, args);
            loader.forceLoad();
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        if(isUrl(data))
            loadHtmlContent(data);
        else
            openDialog(data);
    }

    private boolean isUrl(String data) {
        try {
            new URL(data);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    private void openDialog(String data) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(data)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .create();
        dialog.show();
    }

    private void loadHtmlContent(String data) {
        webView = new WebView(this);
        linearLayout.addView(webView);
        webView.setVisibility(View.GONE);
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView webView, int progress) {
                if (progress == 100){
                    webView.setVisibility(View.VISIBLE);
                    progressDialog.hide();
                }
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                finish();
                return false;
             }
         });
        webView.loadUrl(data);
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
    }

    private String getIMSI() {
        return ((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE)).getSubscriberId();
    }
}
