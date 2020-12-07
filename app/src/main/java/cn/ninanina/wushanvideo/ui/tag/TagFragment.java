package cn.ninanina.wushanvideo.ui.tag;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.adapter.TagAdapter;
import cn.ninanina.wushanvideo.adapter.listener.TagClickListener;
import cn.ninanina.wushanvideo.network.VideoPresenter;

public class TagFragment extends Fragment {
    @BindView(R.id.search)
    EditText search;
    @BindView(R.id.language)
    LinearLayout language;
    @BindView(R.id.language_text)
    TextView languageText;
    @BindView(R.id.suggest)
    RecyclerView suggest;
    @BindView(R.id.index)
    RecyclerView index;
    @BindView(R.id.content)
    RecyclerView content;

    private List<String> indexWords = new ArrayList<>();

    private int page = 0;
    public final int size = 20;
    private boolean isLoading = false;
    private boolean loadingFinished = false;
    private Character c;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tag, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        content.setLayoutManager(new LinearLayoutManager(getContext()));
        suggest.setLayoutManager(new LinearLayoutManager(getContext()));
        initIndex();
        initSearch();

        content.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                //列表中LastVisibleItem为倒数第二行时，加载更多
                if (manager.findLastCompletelyVisibleItemPosition() + 1 >= manager.getItemCount() && !isLoading && !loadingFinished) {
                    page++;
                    VideoPresenter.getInstance().getTags(TagFragment.this);
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        language.setOnClickListener(v -> {
            TagAdapter tagAdapter = (TagAdapter) content.getAdapter();
            tagAdapter.changeLanguage();
            if (tagAdapter.isShowChinese()) languageText.setText(getString(R.string.chinese));
            else languageText.setText(getString(R.string.english));
        });
    }


    private void initIndex() {
        indexWords.add("热门");
        for (int i = 65; i <= 90; i++) {
            indexWords.add(String.valueOf((char) i));
        }
        indexWords.add("#");
        index.setLayoutManager(new LinearLayoutManager(getContext()));
        index.setAdapter(new IndexAdapter());
    }

    private void initSearch() {
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String word = s.toString().trim();
                if (!StringUtils.isEmpty(word) && word.length() >= 2) {
                    suggest.setVisibility(View.VISIBLE);
                    VideoPresenter.getInstance().getTagSuggest(TagFragment.this, word);
                } else {
                    suggest.setVisibility(View.GONE);
                }
            }
        });
        search.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) suggest.setVisibility(View.GONE);
        });
    }

    public int getPage() {
        return page;
    }

    public Character getC() {
        return c;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    public void setLoadingFinished(boolean loadingFinished) {
        this.loadingFinished = loadingFinished;
    }

    public RecyclerView getContent() {
        return content;
    }

    public RecyclerView getSuggest() {
        return suggest;
    }

    class IndexAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        View lastClickedView;

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new IndexHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag_index, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            IndexHolder indexHolder = (IndexHolder) holder;
            indexHolder.text.setText(indexWords.get(position));
            indexHolder.itemView.setOnClickListener(v -> {
                if (lastClickedView != null)
                    lastClickedView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.tag_index_style));
                indexHolder.itemView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.comment_style));
                lastClickedView = indexHolder.itemView;
                content.setAdapter(new TagAdapter(new ArrayList<>(), new TagClickListener(getContext())));
                page = 0;
                loadingFinished = false;
                languageText.setText(getString(R.string.chinese));
                if (position == 0) { //热门

                } else if (position == indexWords.size() - 1) { //数字开头

                } else { //字母
                    c = (char) (position + 96);
                    VideoPresenter.getInstance().getTags(TagFragment.this);
                }
            });
        }

        @Override
        public int getItemCount() {
            return indexWords.size();
        }
    }

    static class IndexHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.text)
        TextView text;

        public IndexHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
