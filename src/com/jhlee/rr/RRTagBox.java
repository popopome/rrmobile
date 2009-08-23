package com.jhlee.rr;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.jhlee.rr.RRTagStreamView.RRTagDataProvider;

public class RRTagBox extends RelativeLayout {
	private static String TAG = "RRTagBox";

	private Button mAddBtn;
	private Button mCloseBtn;
	private EditText mNewTagEdit;
	private RRTagStreamView mTagStreamView;
	private RRTagDataProvider mTagDataProvider;

	public RRTagBox(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		buildLayout();
	}

	public RRTagBox(Context context, AttributeSet attrs) {
		super(context, attrs);
		buildLayout();
	}

	public RRTagBox(Context context) {
		super(context);
		buildLayout();
	}

	private void buildLayout() {
		String infService = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater li;
		li = (LayoutInflater) getContext().getSystemService(infService);
		li.inflate(R.layout.rr_tagbox, this, true);

		mAddBtn = (Button) findViewById(R.id.tag_add);
		mCloseBtn = (Button) findViewById(R.id.tag_box_close);
		mNewTagEdit = (EditText) findViewById(R.id.tag_edit);
		mTagStreamView = (RRTagStreamView) findViewById(R.id.tag_stream_view);

		/*
		 * TAG ADDITION: Tag addition button is clicked
		 */
		mAddBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String tagString = mNewTagEdit.getText().toString();
				if (tagString.length() == 0)
					return;
				/* Trim and lower case */
				tagString.trim();
				tagString = tagString.toLowerCase();
				if (tagString.length() < 3) {
					/*
					 * If tag string is empty, show guide.
					 */
					Toast.makeText(RRTagBox.this.getContext(),
							"Tag string should be long at least 3 character.", Toast.LENGTH_SHORT)
							.show();
					return;
				}

				/* Add tag with checked status */
				boolean succeeded = mTagDataProvider.addTag(tagString, true);
				if (succeeded == false) {
					Log.e(TAG, "Unable to add tag:" + tagString);
					return;
				}
				mTagStreamView.refreshTags();
				/* Scroll to newly added tag */
				mTagStreamView.scrollToTag(tagString);

				/* Clear tag addition view */
				mNewTagEdit.setText("");
			}
		});

		/*
		 * CLOSE BUTTON If close button is clicked, then make view.
		 */
		mCloseBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				RRTagBox.this.setVisibility(GONE);
			}
		});

	}

	public void setTagProvider(RRTagDataProvider provider) {
		mTagDataProvider = provider;
		mTagStreamView.setTagProvider(provider);
	}

	/*
	 * Refresh tags
	 */
	public void refreshTags() {
		mTagStreamView.refreshTags();
	}

	public String getActiveTag() {
		return mTagStreamView.getActiveTag();
	}

	public void scrollToTag(String tag) {
		mTagStreamView.scrollToTag(tag);
	}

	/*
	 * Set tag item state changing listener
	 */
	public void setOnTagItemStateChangeListener(
			RRTagStreamView.OnTagItemStateChangeListener listener) {
		mTagStreamView.setOnTagItemStateChangeListener(listener);
	}
}
