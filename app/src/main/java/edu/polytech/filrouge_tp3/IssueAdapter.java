package edu.polytech.filrouge_tp3;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class IssueAdapter extends ArrayAdapter<Issue> {
    private final ClickableIssue<Issue> clickableIssue;
    private final List<Issue> issues;

    public IssueAdapter(Context context, List<Issue> issues, ClickableIssue<Issue> clickableIssue) {
        super(context, R.layout.item_issue, issues);
        this.clickableIssue = clickableIssue;
        this.issues = issues;
    }

    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_issue, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Issue issue = getItem(position);
        if (issue != null) {
            holder.title.setText(issue.title);
            holder.type.setText(issue.priority == null ? "unknown" : issue.priority);
            holder.score.setText("Score: " + issue.score);

            // photo prise si disponible, sinon l'image priorité (terre)
            String photo = issue.getPhoto();
            if (isRealPhoto(photo)) {
                Uri uri = photo.startsWith("/") ? Uri.fromFile(new File(photo)) : Uri.parse(photo);
                Picasso.get().load(uri)
                        .placeholder(resolvePriorityIcon(issue.priority))
                        .fit().centerCrop()
                        .into(holder.image);
            } else {
                holder.image.setImageResource(resolvePriorityIcon(issue.priority));
            }

            // RatingBar = statut de l'incident
            holder.ratingBar.setOnRatingBarChangeListener(null);
            holder.ratingBar.setRating(issue.getStatus() == null ? 0 : issue.getStatus().getRating());
            holder.ratingBar.setOnRatingBarChangeListener((rb, value, fromUser) -> {
                if (fromUser) {
                    clickableIssue.onRatingBarChange(position, value, IssueAdapter.this, issues);
                }
            });
            holder.ratingBar.setOnTouchListener((v, event) -> {
                parent.requestDisallowInterceptTouchEvent(true);
                return false;
            });

            convertView.setOnClickListener(v -> clickableIssue.onClickItem(issues, position));
        }
        return convertView;
    }

    private boolean isRealPhoto(String photo) {
        return photo != null && (photo.startsWith("/")
                || photo.startsWith("content:") || photo.startsWith("file:"));
    }

    private int resolvePriorityIcon(String priority) {
        if (priority == null) {
            return R.drawable.earth2;
        }
        switch (priority.toLowerCase()) {
            case "high":
                return R.drawable.earth3;
            case "medium":
                return R.drawable.earth2;
            case "low":
            default:
                return R.drawable.earth1;
        }
    }

    private static class ViewHolder {
        final TextView title;
        final TextView type;
        final TextView score;
        final ImageView image;
        final RatingBar ratingBar;

        ViewHolder(View view) {
            title = view.findViewById(R.id.titre);
            type = view.findViewById(R.id.type);
            score = view.findViewById(R.id.nbMort);
            image = view.findViewById(R.id.carte);
            ratingBar = view.findViewById(R.id.issueRatingBar);
        }
    }
}
