package com.polar.browser.loginassistant.login;

import android.app.Fragment;
import android.os.Parcel;
import android.support.annotation.Nullable;

import com.facebook.accountkit.ui.BaseUIManager;
import com.facebook.accountkit.ui.ButtonType;
import com.facebook.accountkit.ui.LoginFlowState;
import com.facebook.accountkit.ui.LoginType;
import com.facebook.accountkit.ui.TextPosition;

/**
 * Created by FKQ on 2017/4/4.
 */

public class FbAdvancedUIManager extends BaseUIManager {
    public static final String LOGIN_TYPE_EXTRA = "loginType";
    public static final String SWITCH_LOGIN_TYPE_EVENT = "switch-login-type";

    private final ButtonType confirmButton;
    private final ButtonType entryButton;
    private final TextPosition textPosition;

    public FbAdvancedUIManager(
            final ButtonType confirmButton,
            final ButtonType entryButton,
            final LoginType loginType,
            final TextPosition textPosition,
            final int themeResourceId) {
        super(loginType, themeResourceId);
        this.confirmButton = confirmButton;
        this.entryButton = entryButton;
        this.textPosition = textPosition;
    }

    private FbAdvancedUIManager(final Parcel source) {
        super(source);
        String s = source.readString();
        final ButtonType confirmButton = s == null ? null : ButtonType.valueOf(s);
        s = source.readString();
        final ButtonType entryButton = s == null ? null : ButtonType.valueOf(s);
        s = source.readString();
        final TextPosition textPosition = s == null ? null : TextPosition.valueOf(s);
        this.confirmButton = confirmButton;
        this.entryButton = entryButton;
        this.textPosition = textPosition;
    }

    @Override
    @Nullable
    public Fragment getBodyFragment(final LoginFlowState state) {
        int iconResourceId = 0;
        boolean showProgressSpinner = false;
        switch (state) {
            case SENDING_CODE:
                showProgressSpinner = true;
                break;
            case SENT_CODE:
                switch (loginType) {
                    case EMAIL:
//                        iconResourceId = R.drawable.reverb_email;
                        break;
                    case PHONE:
//                        iconResourceId = R.drawable.reverb_progress_complete;
                        break;
                }
                break;
            case EMAIL_VERIFY:
//                iconResourceId = R.drawable.reverb_email_sent;
                break;
            case VERIFYING_CODE:
            case CONFIRM_INSTANT_VERIFICATION_LOGIN:
                showProgressSpinner = true;
                break;
            case VERIFIED:
//                iconResourceId = R.drawable.reverb_progress_complete;
                break;
            case ERROR:
//                iconResourceId = R.drawable.reverb_error;
                break;
            case PHONE_NUMBER_INPUT:
            case EMAIL_INPUT:
            case CODE_INPUT:
            case CONFIRM_ACCOUNT_VERIFIED:
            case RESEND:
            case NONE:
            default:
                return null;
        }
//        final ReverbBodyFragment fragment = new ReverbBodyFragment();
//        fragment.setIconResourceId(iconResourceId);
//        fragment.setShowProgressSpinner(showProgressSpinner);
//        return fragment;

//        final FbLoginHeadFragment fragment = new FbLoginHeadFragment();
//        return fragment;
        return super.getBodyFragment(state);
    }

    @Override
    @Nullable
    public ButtonType getButtonType(final LoginFlowState state) {
        switch (state) {
            case PHONE_NUMBER_INPUT:
            case EMAIL_INPUT:
                return entryButton;
            case CODE_INPUT:
            case CONFIRM_ACCOUNT_VERIFIED:
                return confirmButton;
            default:
                return null;
        }
    }

    @Override
    @Nullable
    public Fragment getFooterFragment(final LoginFlowState state) {
        final int progress;
        switch (state) {
            case PHONE_NUMBER_INPUT:
            case EMAIL_INPUT:
                progress = 1;
                break;
            case SENDING_CODE:
            case SENT_CODE:
                progress = 2;
                break;
            case CODE_INPUT:
            case EMAIL_VERIFY:
            case CONFIRM_ACCOUNT_VERIFIED:
                progress = 3;
                break;
            case VERIFYING_CODE:
            case CONFIRM_INSTANT_VERIFICATION_LOGIN:
                progress = 4;
                break;
            case VERIFIED:
                progress = 5;
                break;
            case RESEND:
            case ERROR:
            case NONE:
            default:
                return null;
        }
//        final ReverbFooterFragment fragment = new ReverbFooterFragment();
//        if (progress == 1) {
//            fragment.setLoginType(loginType);
//            fragment.setOnSwitchLoginTypeListener(
//                    new ReverbFooterFragment.OnSwitchLoginTypeListener() {
//                        @Override
//                        public void onSwitchLoginType() {
//                            if (listener == null) {
//                                return;
//                            }
//
//                            listener.onCancel();
//
//                            final Activity activity = fragment.getActivity();
//                            if (activity == null) {
//                                return;
//                            }
//                            final Context applicationContext = activity.getApplicationContext();
//                            final LoginType newLoginType;
//                            switch (loginType) {
//                                case EMAIL:
//                                    newLoginType = LoginType.PHONE;
//                                    break;
//                                case PHONE:
//                                    newLoginType = LoginType.EMAIL;
//                                    break;
//                                default:
//                                    return;
//                            }
//                            LocalBroadcastManager
//                                    .getInstance(applicationContext)
//                                    .sendBroadcast(new Intent(SWITCH_LOGIN_TYPE_EVENT)
//                                            .putExtra(LOGIN_TYPE_EXTRA, newLoginType.name()));
//                        }
//                    });
//        }
//        if (getThemeId() == R.style.AppLoginTheme_Reverb_A) {
//            fragment.setProgressType(ReverbFooterFragment.ProgressType.BAR);
//        } else if (getThemeId() == R.style.AppLoginTheme_Reverb_B
//                || getThemeId() == R.style.AppLoginTheme_Reverb_C) {
//            fragment.setProgressType(ReverbFooterFragment.ProgressType.DOTS);
//        }
//        fragment.setProgress(progress);
//        return fragment;
//        final FbLoginHeadFragment fragment = new FbLoginHeadFragment();
        return super.getFooterFragment(state);
    }

    @Override
    @Nullable
    public Fragment getHeaderFragment(final LoginFlowState state) {
//        if (state == LoginFlowState.ERROR) {
//            return null;
//        } else {
//            return SpaceFragment.create(
//                    R.styleable.Theme_AccountKitSample_Style_reverb_content_margin_top);
//        }

        final FbLoginHeadFragment fragment = new FbLoginHeadFragment();
        return fragment;
    }

    @Override
    @Nullable
    public TextPosition getTextPosition(final LoginFlowState state) {
        return textPosition == null ? TextPosition.ABOVE_BODY : textPosition;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(confirmButton != null ? confirmButton.name() : null);
        dest.writeString(entryButton != null ? entryButton.name() : null);
        dest.writeString(textPosition != null ? textPosition.name() : null);
    }

    public static final Creator<FbAdvancedUIManager> CREATOR
            = new Creator<FbAdvancedUIManager>() {
        @Override
        public FbAdvancedUIManager createFromParcel(final Parcel source) {
            return new FbAdvancedUIManager(source);
        }

        @Override
        public FbAdvancedUIManager[] newArray(final int size) {
            return new FbAdvancedUIManager[size];
        }
    };
}
