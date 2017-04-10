package com.sanislo.andras.boostkeyboard;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class BoostIME extends InputMethodService implements KeyboardView.OnKeyboardActionListener {
    private static final String TAG = BoostIME.class.getSimpleName();
    private RelativeLayout mKeyboardRootView;
    private KeyboardView mKeyboardView;
    private Keyboard mKeyboard;

    private static final String AUTHORITY = "com.sanislo.andras.boostkeyboard";
    private static final String MIME_TYPE_GIF = "image/gif";
    private static final String MIME_TYPE_PNG = "image/png";
    private static final String MIME_TYPE_WEBP = "image/webp";
    private boolean caps = false;

    @Override
    public void onInitializeInterface() {
        super.onInitializeInterface();
        Log.d(TAG, "onInitializeInterface: ");
    }

    @Override
    public void onBindInput() {
        super.onBindInput();
        Log.d(TAG, "onBindInput: ");
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        Log.d(TAG, "onStartInput: ");
    }

    @Override
    public View onCreateInputView() {
        Log.d(TAG, "onCreateInputView: ");
        mKeyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.layout_keyboard, null);
        //mKeyboardRootView = (RelativeLayout) getLayoutInflater().inflate(R.layout.layout_keyboard, null);
        //mKeyboardView = (KeyboardView) mKeyboardRootView.findViewById(R.id.keyboard);
        mKeyboard = new Keyboard(this, R.xml.qwerty);
        mKeyboardView.setKeyboard(mKeyboard);
        mKeyboardView.setOnKeyboardActionListener(this);
        return mKeyboardView;
    }

    @Override
    public boolean onEvaluateInputViewShown() {
        return super.onEvaluateInputViewShown();
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        Log.d(TAG, "onStartInputView: ");
    }

    @Override
    public void onStartCandidatesView(EditorInfo info, boolean restarting) {
        super.onStartCandidatesView(info, restarting);
        Log.d(TAG, "onStartCandidatesView: ");
    }

    @Override
    public void onPress(int primaryCode) {

    }

    @Override
    public void onRelease(int primaryCode) {

    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection ic = getCurrentInputConnection();
        Log.d(TAG, "onKey: primaryCode: " + primaryCode + " keyCodes: " + keyCodes);
        //TODO play sound on key tap
        //playClick(primaryCode);
        switch (primaryCode) {
            case Keyboard.KEYCODE_DELETE :
                ic.deleteSurroundingText(1, 0);
                break;
            case Keyboard.KEYCODE_SHIFT:
                caps = !caps;
                mKeyboard.setShifted(caps);
                mKeyboardView.invalidateAllKeys();
                break;
            case Keyboard.KEYCODE_DONE:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
            case 113:
                ic.commitText("\ud83d\ude01", 1);
                break;
            default:
                char code = (char) primaryCode;
                if (Character.isLetter(code) && caps){
                    code = Character.toUpperCase(code);
                }
                ic.commitText(String.valueOf(code),1);
        }
    }

    @Override
    public void onText(CharSequence text) {

    }

    @Override
    public void swipeLeft() {
        makeToast("swipeLeft");
    }

    @Override
    public void swipeRight() {
        makeToast("swipeRight");
    }

    @Override
    public void swipeDown() {
        makeToast("swipeDown");
    }

    @Override
    public void swipeUp() {
        makeToast("swipeUp");
    }

    private void makeToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }
}
