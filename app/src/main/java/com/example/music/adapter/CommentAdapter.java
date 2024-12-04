package com.example.music.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.R;
import com.example.music.model.CommentModel;
import com.google.firebase.firestore.auth.User;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private Context mContext;
    private List<CommentModel> mListCommentModel;

    public CommentAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setData(List<CommentModel> list){
        this.mListCommentModel=list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment,parent,false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommentModel cmt=mListCommentModel.get(position);

        holder.tvContent.setText(cmt.getContent());
        holder.tvAccount.setText(cmt.getUsername());
        holder.tvTime.setText(cmt.getTimestamp());
    }

    @Override
    public int getItemCount() {
        if(mListCommentModel!=null){
            return mListCommentModel.size();
        }
        return 0;
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder{
        private ImageView avatarComment, btnLikeComment;
        private TextView tvAccount, tvTime, tvContent, tvTextLike;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarComment = itemView.findViewById(R.id.avatarComment);
            tvAccount = itemView.findViewById(R.id.accountComment);
            tvTime = itemView.findViewById(R.id.timeComment);
            tvContent = itemView.findViewById(R.id.bodyComment);
            tvTextLike = itemView.findViewById(R.id.textLikeComment);
            btnLikeComment = itemView.findViewById(R.id.likeComment);

        }
    }
}
