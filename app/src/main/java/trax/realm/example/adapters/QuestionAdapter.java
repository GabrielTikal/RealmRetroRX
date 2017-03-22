package trax.realm.example.adapters;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

import rx.functions.Action1;
import trax.realm.example.R;
import trax.realm.example.model.Question;

/**
 * Created by Gabriel on 3/20/2017.
 */

public class QuestionAdapter  extends RealmRecyclerViewAdapter<Question> {
    final Context context;
    private Realm realm;
    private LayoutInflater inflater;

    public QuestionAdapter(Context context) {

        this.context = context;
    }

    // create new views (invoked by the layout manager)
    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflate a new card view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question, parent, false);
        return new CardViewHolder(view);
    }

    // replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {

        realm = Realm.getDefaultInstance();

        // get the article
        final Question question = getItem(position);
        // cast the generic view holder to our specific one
        final CardViewHolder holder = (CardViewHolder) viewHolder;

        // set the title and the snippet
        holder.textTitle.setText(question.getTitle());
        //holder.textAuthor.setText(question.getAuthor());
        holder.textDescription.setText(question.getLink());

        // load the background image
//        if (question.getImageUrl() != null) {
//            Glide.with(context)
//                    .load(question.getImageUrl().replace("https", "http"))
//                    .asBitmap()
//                    .fitCenter()
//                    .into(holder.imageBackground);
//        }

        //remove single match from realm
        holder.card.setOnLongClickListener(v-> {

                RealmResults<Question> results = realm.where(Question.class).findAll();

                // Get the book title to show it in toast message
                Question b = results.get(position);
                String title = b.getTitle();

                // All changes to data must happen in a transaction
                realm.beginTransaction();

                // remove single match
                results.remove(position);
                realm.commitTransaction();

                if (results.size() == 0) {
                //    Prefs.with(context).setPreLoad(false);
                }

                // ======================================================================================//
                // TODO: Gabriel: I wish this will happen with reactive by itself cause the change in table
                //notifyDataSetChanged();
                // ======================================================================================//

                Toast.makeText(context, title + " is removed from Realm", Toast.LENGTH_SHORT).show();
                return false;
            }
        );

        //update single match from realm
        holder.card.setOnClickListener(v -> {

                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View content = inflater.inflate(R.layout.edit_item, null);
                final EditText editTitle = (EditText) content.findViewById(R.id.title);
                final EditText editAuthor = (EditText) content.findViewById(R.id.author);
                final EditText editDescription = (EditText) content.findViewById(R.id.description);

                editTitle.setText(question.getTitle());
//                editAuthor.setText(question.getAuthor());
                editDescription.setText(question.getLink());

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(content)
                        .setTitle("Edit Book")
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {

                                RealmResults<Question> results = realm.where(Question.class).findAll();

                                realm.beginTransaction();

                                Question q = question;//results.get(position);
                                //q.setAuthor(editAuthor.getText().toString());
                                q.setTitle(editTitle.getText().toString());
                                //q.setImageUrl(editThumbnail.getText().toString());
                                q.asObservable().subscribe(new Action1<RealmObject>() {
                                    @Override
                                    public void call(RealmObject realmObject) {

                                    }
                                });
                                realm.commitTransaction();

                                notifyDataSetChanged();
                            }
                        )
                        .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                                dialog.dismiss();
                            }
                        );
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        );
    }

    // return the size of your data set (invoked by the layout manager)
    public int getItemCount() {

        if (getRealmAdapter() != null) {
            return getRealmAdapter().getCount();
        }
        return 0;
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {

        public CardView card;
        public TextView textTitle;
        public TextView textAuthor;
        public TextView textDescription;
        public ImageView imageBackground;

        public CardViewHolder(View itemView) {
            // standard view holder pattern with Butterknife view injection
            super(itemView);

            card = (CardView) itemView.findViewById(R.id.card_books);
            textTitle = (TextView) itemView.findViewById(R.id.text_books_title);
            textAuthor = (TextView) itemView.findViewById(R.id.text_books_author);
            textDescription = (TextView) itemView.findViewById(R.id.text_books_description);
            imageBackground = (ImageView) itemView.findViewById(R.id.image_background);
        }
    }
}
