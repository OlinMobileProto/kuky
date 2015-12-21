package com.example.keenan.kuky.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.keenan.kuky.R;
import com.example.keenan.kuky.api.ApiClient;
import com.example.keenan.kuky.fragments.ProfileFragment;
import com.example.keenan.kuky.helpers.AuthHelper;
import com.example.keenan.kuky.helpers.KuComposeHelper;
import com.example.keenan.kuky.models.KuComposeResponse;
import com.example.keenan.kuky.models.KuRequest;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Ku Write Activity is handles when the floating action button for composing a new Ku
 * It checks for syllables, making sure the Ku is a valid haiku, or at least to our algorithm.
 * Also handles the API calls to post the ku.
 */
public class KuWriteActivity extends AppCompatActivity {

    @Bind(R.id.ku_compose_cancel) ImageButton mComposeCancel;
    @Bind(R.id.ku_compose_done) ImageButton mComposeDone;
    @Bind(R.id.ku_line_one) EditText mKuLineOne;
    @Bind(R.id.ku_line_two) EditText mKuLineTwo;
    @Bind(R.id.ku_line_three) EditText mKuLineThree;

    private static final String TAG = KuWriteActivity.class.getSimpleName();

    private KuComposeHelper mComposeHelper;

    private String line1;
    private String line2;
    private String line3;

    /**
     * The OnCreate sets up the view of the page
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ku_write);
        ButterKnife.bind(this);

        try {
            mComposeHelper = new KuComposeHelper(getBaseContext());
        } catch (IOException err){
            Log.e(TAG, err.toString());
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.drawable.ic_logo_clear);
    }

    /**
     * Button handle for the cancel button, goes back to the Ku View activity
     * @param view the current view of the screen
     */
    @OnClick(R.id.ku_compose_cancel)
    public void onComposeCancelClick(View view) {
        Snackbar.make(view, "Cancel Ku", Snackbar.LENGTH_LONG)
                .setAction("Cancel", null).show();
        Intent intent = new Intent(getApplicationContext(), KuViewActivity.class);
        startActivity(intent);
    }

    /**
     * Button handle for when the done button is clicked, checks if the submitted text is a haiku
     * then makes an API call to post the haiku and updates in the view
     * @param view the current view of the screen
     */
    @OnClick(R.id.ku_compose_done)
    public void onComposeDoneClick(View view) {
        Snackbar.make(view, "Ku Submitted!", Snackbar.LENGTH_LONG)
                .setAction("Submit", null).show();
        line1 = mKuLineOne.getText().toString();
        line2 = mKuLineTwo.getText().toString();
        line3 = mKuLineThree.getText().toString();
        Log.d(TAG, line1 + ' ' + line2 + ' ' + line3);
        try {
            KuComposeHelper helper = new KuComposeHelper(getBaseContext());
            int[] syllables = helper.checkKu(line1, line2, line3);
            Log.wtf(TAG, String.valueOf(syllables[0]) + String.valueOf(syllables[1]) + String.valueOf(syllables[2]));
            // TODO: Make sure check syllable works + get location
            if ((syllables[0] == 5) && (syllables[1] == 7) && (syllables[2] == 5)) {
                Log.d(TAG, "Ku correct");
                String kuContent = line1 + ';' + line2 + ';' + line3;
                String[] creds = AuthHelper.getCreds(getBaseContext());
                if (creds != null) {
                    KuRequest req = new KuRequest(kuContent, Integer.parseInt(creds[2]), -40, 20);
                    Log.d(TAG, req.toString());
                    ApiClient.getKukyApiClient(creds)
                            .postKu(req)
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Subscriber<KuComposeResponse>() {
                                @Override
                                public void onCompleted() {

                                }

                                @Override
                                public void onError(Throwable e) {
                                    Log.e(TAG + ": Retrofit Error - ", e.toString());
                                }

                                @Override
                                public void onNext(KuComposeResponse kuComposeResponse) {
                                    Log.d(TAG, kuComposeResponse.toString());
                                    // TODO: Take user back to previous fragment
                                    ProfileFragment.updateScore(ProfileFragment.KU_COMPOSING_SCORE);
                                }
                            });
                }
            } else {
                Toast.makeText(this, "Your haiku does not meet the requirement", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Log.wtf(TAG, "Could not create Ku Helper");
        }

        new CountDownTimer(1000, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                Intent intent = new Intent(getApplicationContext(), KuViewActivity.class);
                startActivity(intent);
            }
        }.start();

    }

    /**
     * Handle action bar item clicks here. The action bar will
     * automatically handle clicks on the Home/Up button, so long
     * as you specify a parent activity in AndroidManifest.xml.
     * @param item MenuItem object
     * @return boolean true
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        // again, probably remove this if you're going to do nothing with it
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
