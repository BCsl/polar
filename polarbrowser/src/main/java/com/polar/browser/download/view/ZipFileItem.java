package com.polar.browser.download.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.bean.ZipInfo;
import com.polar.browser.common.ui.CommonCheckBox1;
import com.polar.browser.common.ui.ICustomCheckBox;
import com.polar.browser.download.FileClassifyDetailActivity;
import com.polar.browser.utils.DateUtils;
import com.polar.browser.utils.DensityUtil;
import com.polar.browser.utils.FileUtils;

/**
 * Created by saifei on 17/1/5.
 */

public class ZipFileItem extends RelativeLayout {
    public ZipFileItem(Context context) {
        this(context, null);
    }

    public ZipFileItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    private CommonCheckBox1 zipfile_checkbox;
    private ImageView zipfile_icon;
    private TextView zipfile_title;
    private TextView zipfile_size;
    private TextView zipfile_download_time;
    private ZipInfo zipfileInfo;


    private void init() {
        inflate(getContext(), R.layout.view_video_audio_list_item, this);
        setPadding(36, DensityUtil.dip2px(getContext(), 8), 0, DensityUtil.dip2px(getContext(), 8));

        zipfile_checkbox = (CommonCheckBox1) findViewById(R.id.video_audio_checkbox);

        zipfile_icon = (ImageView) findViewById(R.id.video_audio_icon);
        int iconWidthHeight = (int) getResources().getDimension(R.dimen.file_list_item_other_iv_width);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(iconWidthHeight, iconWidthHeight);
       /* int marginLeft = (int) getResources().getDimension(R.dimen.file_list_item_cb_marginLeft);
        layoutParams.setMargins(marginLeft, 0, 0, 0);*/
        layoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.video_audio_checkbox);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        zipfile_icon.setLayoutParams(layoutParams);

        zipfile_title = (TextView) findViewById(R.id.video_audio_title);
        zipfile_size = (TextView) findViewById(R.id.video_audio_size);
        zipfile_download_time = (TextView) findViewById(R.id.video_audio_download_time);
        setListeners();

    }

    private void setListeners() {
        zipfile_checkbox.setOnCheckedChangedListener(new ICustomCheckBox.OnCheckChangedListener() {
            @Override
            public void onCheckChanged(View v, boolean isChecked) {
//				mTask.setChecked(isChecked);
                zipfileInfo.isChecked = isChecked;
                if (getContext() instanceof FileClassifyDetailActivity) {
                    ((FileClassifyDetailActivity)getContext()).checkCheckAllButton();
                }

            }
        });
    }

    public void bind(ZipInfo info) {
        this.zipfileInfo = info;


        zipfile_icon.setImageResource(R.drawable.file_icon_zip);
        zipfile_title.setText(info.getName());
        zipfile_size.setText(FileUtils.formatFileSize(info.getSize()));
        zipfile_download_time.setText(DateUtils.formatFileDate(info.getDate()*1000));
        if (info.isEditing()) {
            zipfile_checkbox.setVisibility(View.VISIBLE);
            if (zipfileInfo.isChecked) {
                zipfile_checkbox.setChecked(true);
            } else {
                zipfile_checkbox.setChecked(false);
            }
        } else {
            zipfile_checkbox.setVisibility(View.GONE);
        }

    }
}
