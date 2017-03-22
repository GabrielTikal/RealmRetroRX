package trax.realm.example;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import trax.realm.example.adapters.QuestionAdapter;
import trax.realm.example.adapters.RealmModelAdapter;
import trax.realm.example.adapters.RealmRecyclerViewAdapter;
import trax.realm.example.model.Question;



/**
 * Created by gabrielnoam on 21/03/2017.
 */
public class ActivityRealmRetroRx extends AppCompatActivity {

    private static final String TAG = "ActivityRealmRetroRx";

    private RecyclerView recyclerView;
    private StackOverflowAPI api;
    private ProgressDialog progressDialog;
    private RealmRecyclerViewAdapter<Question> adapter;

    private Realm realm;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_realm_retro_rx);

        //set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Search in StackOverflow");
        api = createStackOverflowAPI();
        setupRecycler();
    }

    private void setupRecycler() {
        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        recyclerView.setAdapter(adapter);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager since the cards are vertically scrollable
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        // create an empty adapter and add it to the recycler view
        adapter = new QuestionAdapter(this);
        recyclerView.setAdapter(adapter);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Search in Stackoverflow");
        final EditText searchEditText = new EditText(this);
        searchEditText.setHint("Key Word");
        searchEditText.setText("android");
        builder.setView(searchEditText);
        builder.setPositiveButton("Search", (dialog, which)-> {
                    String keyWord = searchEditText.getText().toString();
                    search(keyWord);
                }
        );

        builder.setNegativeButton("Cancel", null);

        builder.create().show();

        return true;
    }


    private void search(final String keyWord)
    {
        progressDialog.setMessage("Search "+keyWord);
        progressDialog.show();
        setProgressBarIndeterminateVisibility(true);

        Log.d(TAG, "search: '"+keyWord+"'");

        api.loadQuestions(keyWord).subscribeOn(Schedulers.newThread()).
                map(stackOverflowQuestions->
                {
                    Log.d(TAG, "search: map: "+stackOverflowQuestions.getItems()+" items");

                    realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    realm.clear(Question.class);
                    realm.copyToRealm(stackOverflowQuestions.getItems());
                    realm.commitTransaction();

                    return stackOverflowQuestions.getItems();
                }).
                observeOn(AndroidSchedulers.mainThread()). // REALLY REALLY IMPORTANT!!! WILL NOT WORK WITHOUT
                subscribe(new Subscriber<List<Question>>() {
                    @Override
                    public void onCompleted() {
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "search: onError: "+e.getMessage());

                        progressDialog.dismiss();
                        e.printStackTrace();
                        Toast.makeText(ActivityRealmRetroRx.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onNext(List<Question> results) {

                        Log.d(TAG, "search: onNext: "+results.size()+" items");
                        for(Question question : results) {
                            Log.d(TAG, "search: onNext: " + question.toString());
                        }

                        realm = Realm.getDefaultInstance();

                        Observable<RealmResults<Question>> questionsObservable =
                                realm.where(Question.class).findAll().asObservable();
                        realm.refresh() ;

                        questionsObservable.subscribe(new Action1<RealmResults<Question>>() {
                            @Override
                            public void call(RealmResults<Question> questions) {
                                if(questions.isLoaded()) {

                                    RealmModelAdapter<Question> realmAdapter =
                                            new RealmModelAdapter<>(ActivityRealmRetroRx.this, questions, true);

                                    adapter.setRealmAdapter(realmAdapter);
                                    adapter.notifyDataSetChanged();

                                    questions.addChangeListener(() ->
                                    {
                                        Log.d(TAG, "***** call: a mechaie");
                                        adapter.notifyDataSetChanged();
                                    });
                                }
                            }
                        });

                    }
                });
    }


    private StackOverflowAPI createStackOverflowAPI() {


        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getDeclaringClass().equals(RealmObject.class);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.stackexchange.com")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();


        return retrofit.create(StackOverflowAPI.class);
    }


    @Override
    protected void onStop() {
        super.onStop();
        progressDialog.dismiss();
    }
}