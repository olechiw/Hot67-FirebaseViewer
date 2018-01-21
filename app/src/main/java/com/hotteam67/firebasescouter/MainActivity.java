package com.hotteam67.firebasescouter;

import android.support.v7.app.ActionBar;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;

import com.evrencoskun.tableview.TableView;
import com.hotteam67.firebasescouter.firebase.FirebaseHelper;
import com.hotteam67.firebasescouter.firebase.RawTableHandler;
import com.hotteam67.firebasescouter.tableview.MainTableAdapter;
import com.hotteam67.firebasescouter.tableview.MyTableViewListener;

import java.util.concurrent.Callable;

public class MainActivity extends AppCompatActivity {
    private TableView mTableView;
    private MainTableAdapter mTableAdapter;

    private ProgressDialog mProgressDialog;


    public MainActivity() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ActionBar bar = getSupportActionBar();
        View finalView = getLayoutInflater().inflate(
                R.layout.actionbar_main,
                null);
        finalView.setLayoutParams(new ActionBar.LayoutParams(
                ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT));
        bar.setCustomView(finalView);
        bar.setDisplayShowCustomEnabled(true);

        mTableView = findViewById(R.id.mainTableView);

        // Create TableView Adapter
        mTableAdapter = new MainTableAdapter(this);
        mTableView.setAdapter(mTableAdapter);

        // Create listener
        mTableView.setTableViewListener(new MyTableViewListener(mTableView));


        final FirebaseHelper model = new FirebaseHelper(
                    "https://hot-67-scouting.firebaseio.com/",
                    "testevent1");

        showProgressDialog();
        // Null child to get all raw data
        model.Download(new Callable() {
            @Override
            public Object call() throws Exception {

                RawTableHandler tmpTableHandler = new RawTableHandler(model.getResult());
                mTableAdapter.setAllItems(
                        tmpTableHandler.GetColumns(),
                        tmpTableHandler.GetRowHeaders(),
                        tmpTableHandler.GetCells());
                hideProgressDialog();
                return null;
            }
        }, getAssets());
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Getting data, please wait...");
            mProgressDialog.setCancelable(false);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {

        if ((mProgressDialog != null) && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
        mProgressDialog = null;
    }
}
