package nivesuresh.traditionallatinmassfinder;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

/**
 * Created by nivesuresh on 11/24/16.
 */
public class TLMNewsActivity extends AppCompatActivity {
    private WebView webView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_activity);

        webView = (WebView) findViewById(R.id.webView2);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("http://www.leetaurapps.com/LatinMassNews.html");

    }
}
