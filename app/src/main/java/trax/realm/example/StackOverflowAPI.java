package trax.realm.example;


import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import trax.realm.example.model.Questions;

/**
 * Created by gabrielnoam on 07/07/2016.
 */

public interface StackOverflowAPI {
    @GET("2.2/questions?order=desc&sort=creation&site=stackoverflow")
    Observable<Questions> loadQuestions(@Query("tagged") String tags);
}