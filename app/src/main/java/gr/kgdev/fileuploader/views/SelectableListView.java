package gr.kgdev.fileuploader.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;

import gr.kgdev.fileuploader.R;

public class SelectableListView extends ListView {

    private ArrayAdapter<String> adapter;
    private String selectedItem;

    public SelectableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public SelectableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SelectableListView(Context context) {
        super(context);
        init(context);
    }

    public void init(Context context) {
        adapter = new ArrayAdapter<String>(context,
                R.layout.list_item, new ArrayList<>());
        setAdapter(adapter);

        this.setOnItemClickListener((parent, view, position, id) -> {
            for (int a = 0; a < parent.getChildCount(); a++)
                parent.getChildAt(a).setBackgroundColor(Color.TRANSPARENT);

            if (position != -1) {
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
                selectedItem = adapter.getItem(position);
            }
        });
    }

    public String getSelectedItem() {
        return selectedItem;
    }

    @Override
    public ArrayAdapter<String> getAdapter() {
        return adapter;
    }
}
